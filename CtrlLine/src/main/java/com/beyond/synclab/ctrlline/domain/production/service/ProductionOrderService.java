package com.beyond.synclab.ctrlline.domain.production.service;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
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
    private final Clock clock;

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
        List<ProductionPlans> plans = productionPlanRepository.findAllByStatusAndStartTimeLessThanEqual(
                ProductionPlans.PlanStatus.CONFIRMED, now
        );

        for (ProductionPlans plan : plans) {
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

                plan.markDispatched();
                productionPlanRepository.save(plan);
            } catch (Exception ex) {
                log.error("Failed to dispatch production plan documentNo={}", plan.getDocumentNo(), ex);
                plan.markDispatchFailed();
                productionPlanRepository.save(plan);
            }
        }
    }

    @Transactional
    public void sendLineAck(ProductionPlans plan) {
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
            plan.updateStatus(ProductionPlans.PlanStatus.COMPLETED);
            productionPlanRepository.save(plan);
        } catch (Exception ex) {
            log.error("Failed to send ACK for production plan documentNo={}", plan.getDocumentNo(), ex);
        }
    }

    private Optional<DispatchContext> prepareDispatchContext(ProductionPlans plan) {
        ItemsLines itemLine = plan.getItemLine();
        if (itemLine == null) {
            log.warn("ItemLine not set for production plan documentNo={}", plan.getDocumentNo());
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
        }

        if (itemLine.getItem() == null || !org.springframework.util.StringUtils.hasText(itemLine.getItem().getItemCode())) {
            log.warn("Item code missing for production plan documentNo={} itemLineId={}", plan.getDocumentNo(), itemLine.getId());
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
        }

        if (itemLine.getLine() == null) {
            log.warn("Line entity missing for production plan documentNo={} itemLineId={}", plan.getDocumentNo(), itemLine.getId());
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
        }

        Long lineId = itemLine.getLineId();

        Optional<Lines> lineOptional = lineRepository.findById(lineId);
        if (lineOptional.isEmpty()) {
            log.warn("Line not found for production plan documentNo={}, lineId={}", plan.getDocumentNo(), lineId);
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
        }

        Optional<String> factoryCodeOptional = lineRepository.findFactoryCodeByLineId(lineId);
        if (factoryCodeOptional.isEmpty()) {
            log.warn("Factory code not found for lineId={} documentNo={}", lineId, plan.getDocumentNo());
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
        }

        int quantity = plan.commandQuantity();
        if (quantity <= 0) {
            log.warn("Invalid command quantity for documentNo={}, quantity={}", plan.getDocumentNo(), quantity);
            plan.markDispatchFailed();
            productionPlanRepository.save(plan);
            return Optional.empty();
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
}
