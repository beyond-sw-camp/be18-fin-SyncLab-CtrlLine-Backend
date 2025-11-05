package com.beyond.synclab.ctrlline.domain.production.service;

import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import com.beyond.synclab.ctrlline.domain.production.entity.Line;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan.PlanStatus;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.production.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import java.time.Clock;
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
    private final Clock clock;

    @Transactional(readOnly = true)
    public ProductionOrderCommandResponse dispatchOrder(String lineCode, ProductionOrderCommandRequest request) {
        MiloProductionOrderRequest miloRequest = new MiloProductionOrderRequest(request.itemCode(), request.qty());
        MiloProductionOrderResponse response = miloProductionOrderClient.dispatchOrder(lineCode, miloRequest);
        return ProductionOrderCommandResponse.from(response);
    }

    @Transactional
    public void dispatchDuePlans() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ProductionPlan> plans = productionPlanRepository.findAllByStatusAndStartAtLessThanEqual(
                PlanStatus.CONFIRMED, now
        );

        for (ProductionPlan plan : plans) {
            try {
                Optional<Line> lineOptional = lineRepository.findById(plan.getLineId());
                if (lineOptional.isEmpty()) {
                    log.warn("Line not found for production plan documentNo={}, lineId={}", plan.getDocumentNo(), plan.getLineId());
                    plan.markDispatchFailed();
                    productionPlanRepository.save(plan);
                    continue;
                }

                Line line = lineOptional.get();
                plan.assignLineCode(line.getLineCode());

                Optional<String> itemCodeOptional = lineRepository.findItemCodeByLineId(plan.getLineId());
                if (itemCodeOptional.isEmpty()) {
                    log.warn("Item code not found for lineId={} documentNo={}", plan.getLineId(), plan.getDocumentNo());
                    plan.markDispatchFailed();
                    productionPlanRepository.save(plan);
                    continue;
                }

                int quantity = plan.commandQuantity();
                if (quantity <= 0) {
                    log.warn("Invalid command quantity for documentNo={}, quantity={}", plan.getDocumentNo(), quantity);
                    plan.markDispatchFailed();
                    productionPlanRepository.save(plan);
                    continue;
                }

                MiloProductionOrderRequest request = new MiloProductionOrderRequest(
                        itemCodeOptional.get(),
                        quantity
                );

                MiloProductionOrderResponse response = miloProductionOrderClient.dispatchOrder(plan.getLineCode(), request);

                plan.markDispatched();
                productionPlanRepository.save(plan);
            } catch (Exception ex) {
                log.error("Failed to dispatch production plan documentNo={}", plan.getDocumentNo(), ex);
                plan.markDispatchFailed();
                productionPlanRepository.save(plan);
            }
        }
    }
}
