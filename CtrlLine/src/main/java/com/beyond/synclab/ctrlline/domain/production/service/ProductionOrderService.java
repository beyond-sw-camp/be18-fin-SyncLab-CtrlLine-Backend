package com.beyond.synclab.ctrlline.domain.production.service;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.lot.service.LotGeneratorService;
import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.service.PlanDefectiveService;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanDelayService;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanStatusNotificationService;
import com.beyond.synclab.ctrlline.domain.telemetry.service.LineFinalInspectionProgressService;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderService {

    private final ProductionPlanRepository productionPlanRepository;
    private final LineRepository lineRepository;
    private final MiloProductionOrderClient miloProductionOrderClient;
    private final PlanDefectiveService planDefectiveService;
    private final LotGeneratorService lotGeneratorService;
    private final Clock clock;
    private final ProductionPlanStatusNotificationService planStatusNotificationService;
    private final ProductionPlanDelayService productionPlanDelayService;
    private final LineFinalInspectionProgressService lineFinalInspectionProgressService;

    @Transactional(readOnly = true)
    public ProductionOrderCommandResponse dispatchOrder(String factoryCode, String lineCode, ProductionOrderCommandRequest request) {
        MiloProductionOrderRequest miloRequest = new MiloProductionOrderRequest(
                request.action(),
                request.orderNo(),
                request.targetQty(),
                request.itemCode(),
                request.ppm()
        );
        MiloProductionOrderResponse response = miloProductionOrderClient.dispatchOrder(factoryCode, lineCode, miloRequest);
        return ProductionOrderCommandResponse.from(response);
    }

    @Transactional
    public void dispatchDuePlans() {
        LocalDateTime now = LocalDateTime.now(clock);

        // Only expire PENDING plans
        expirePendingPlans(now.plusMinutes(30));

        // CONFIRMED but not expired (MES standard)
        List<ProductionPlans> plans = productionPlanRepository.findAllByStatusAndStartTimeLessThanEqual(
                ProductionPlans.PlanStatus.CONFIRMED, now
        );

        log.debug("Found {} production plans to dispatch at {}", plans.size(), now);

        for (ProductionPlans plan : plans) {
            log.debug("Evaluating production plan documentNo={} status={} startTime={}",
                    plan.getDocumentNo(), plan.getStatus(), plan.getStartTime());
            try {
                Optional<DispatchContext> contextOptional = prepareDispatchContext(plan);
                if (contextOptional.isEmpty()) {
                    continue;
                }

                DispatchContext context = contextOptional.get();

                MiloProductionOrderRequest request = new MiloProductionOrderRequest(
                        context.action(),
                        context.orderNo(),
                        context.quantity(),
                        context.itemCode(),
                        context.ppm()
                );
                log.info("Dispatching production order documentNo={}, factoryCode={}, lineCode={}, itemCode={}, qty={}",
                        context.orderNo(),
                        context.factoryCode(),
                        context.lineCode(),
                        context.itemCode(),
                        context.quantity());

                miloProductionOrderClient.dispatchOrder(
                        context.factoryCode(),
                        context.lineCode(),
                        request
                );

                ProductionPlans.PlanStatus previousStatus = plan.getStatus();
                plan.markDispatched();
                planStatusNotificationService.notifyStatusChange(plan, previousStatus);
                lineFinalInspectionProgressService.initializeProgress(
                        plan,
                        context.factoryCode(),
                        context.lineCode()
                );
                log.info("Production plan documentNo={} marked as RUNNING", plan.getDocumentNo());
                planDefectiveService.createPlanDefective(plan);
                lotGeneratorService.createLot(plan);
                productionPlanRepository.save(plan);
            } catch (Exception ex) {
                log.error("Failed to dispatch production plan documentNo={}", plan.getDocumentNo(), ex);
                markPlanReturned(plan, "Exception while dispatching plan. message=" + ex.getMessage());
            }
        }
    }

    private void expirePendingPlans(LocalDateTime now) {
        List<ProductionPlans> pendingPlans =
            productionPlanRepository.findAllByStatusAndStartTimeLessThanEqual(
                ProductionPlans.PlanStatus.PENDING, now
            );

        if (pendingPlans.isEmpty()) {
            return;
        }

        for (ProductionPlans pendingPlan : pendingPlans) {
            log.info("PENDING 계획 documentNo={} 만료되어 RETURNED 처리", pendingPlan.getDocumentNo());
            ProductionPlans.PlanStatus previousStatus = pendingPlan.getStatus();
            pendingPlan.markDispatchFailed();  // RETURNED
            productionPlanRepository.save(pendingPlan);
            planStatusNotificationService.notifyStatusChange(pendingPlan, previousStatus);
        }
    }

    @Transactional
    public void sendLineAck(ProductionPlans plan, LocalDateTime performanceEndTime) {
        if (plan == null) {
            return;
        }
        try {
            Optional<DispatchContext> contextOptional = prepareDispatchContext(plan);
            if (contextOptional.isEmpty()) {
                return;
            }
            DispatchContext context = contextOptional.get();
            MiloProductionOrderRequest request = new MiloProductionOrderRequest(
                    "ACK",
                    context.orderNo(),
                    context.quantity(),
                    context.itemCode(),
                    context.ppm()
            );
            miloProductionOrderClient.dispatchOrder(
                    context.factoryCode(),
                    context.lineCode(),
                    request
            );

            ProductionPlans.PlanStatus previousStatus = plan.getStatus();
            plan.updateStatus(ProductionPlans.PlanStatus.COMPLETED);
            productionPlanRepository.saveAndFlush(plan);

            planStatusNotificationService.notifyStatusChange(plan, previousStatus);
            lineFinalInspectionProgressService.clearProgress(
                    context.factoryCode(),
                    context.lineCode()
            );

            productionPlanDelayService.applyRealPerformanceDelay(plan, performanceEndTime);

        } catch (Exception ex) {
            log.error("Failed to send ACK for production plan documentNo={}", plan.getDocumentNo(), ex);
        }
    }

    private Optional<DispatchContext> prepareDispatchContext(ProductionPlans plan) {
        ItemsLines itemLine = plan.getItemLine();
        if (itemLine == null) {
            return markPlanReturned(plan, "ItemLine entity missing");
        }

        if (itemLine.getItem() == null || !org.springframework.util.StringUtils.hasText(itemLine.getItem().getItemCode())) {
            return markPlanReturned(plan, "Item code missing itemLineId=" + itemLine.getId());
        }

        if (itemLine.getLine() == null) {
            return markPlanReturned(plan, "Line entity missing itemLineId=" + itemLine.getId());
        }

        Long lineId = itemLine.getLineId();

        Optional<Lines> lineOptional = lineRepository.findById(lineId);
        if (lineOptional.isEmpty()) {
            return markPlanReturned(plan, "Line not found lineId=" + lineId);
        }

        Optional<String> factoryCodeOptional = lineRepository.findFactoryCodeByLineId(lineId);
        if (factoryCodeOptional.isEmpty()) {
            return markPlanReturned(plan, "Factory code not found lineId=" + lineId);
        }

        int quantity = plan.commandQuantity();
        if (quantity <= 0) {
            return markPlanReturned(plan, "Invalid command quantity quantity=" + quantity);
        }

        return Optional.of(new DispatchContext(
                factoryCodeOptional.get(),
                itemLine.getLine().getLineCode(),
                itemLine.getItem().getItemCode(),
                quantity,
                "START",
                plan.getDocumentNo(),
                null
        ));
    }

    @Transactional
    public void detectAndApplyRunningDelays() {
        LocalDateTime now = LocalDateTime.now(clock);

        List<ProductionPlans> runningPlans =
            productionPlanRepository.findAllByStatus(ProductionPlans.PlanStatus.RUNNING);

        for (ProductionPlans plan : runningPlans) {
            LocalDateTime scheduledEnd = plan.getEndTime();

            // 2) endTime 이 이미 미래라면 delay 이미 반영된 것으로 간주
            if (scheduledEnd.isAfter(now)) {
                continue; // 지연 누적 방지
            }

            long delayMinutes = Duration.ofMinutes(10).toMinutes();

            log.info("RUNNING 지연 감지: documentNo={}, delay={}min",
                plan.getDocumentNo(), delayMinutes);

            productionPlanDelayService.applyOngoingDelay(plan, delayMinutes);
        }
    }

    private record DispatchContext(
            String factoryCode,
            String lineCode,
            String itemCode,
            int quantity,
            String action,
            String orderNo,
            Integer ppm
    ) {
    }

    private Optional<DispatchContext> markPlanReturned(ProductionPlans plan, String reason) {
        log.debug("Production plan documentNo={} will be marked RETURNED. reason={}",
                plan.getDocumentNo(), reason);
        ProductionPlans.PlanStatus previousStatus = plan.getStatus();
        plan.markDispatchFailed();
        productionPlanRepository.save(plan);
        planStatusNotificationService.notifyStatusChange(plan, previousStatus);
        return Optional.empty();
    }
}
