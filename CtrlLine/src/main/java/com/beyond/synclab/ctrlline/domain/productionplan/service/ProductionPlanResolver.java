package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductionPlanResolver {

    private final ProductionPlanRepository productionPlanRepository;

    public Optional<ProductionPlans> resolveLatestPlan(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return Optional.empty();
        }
        Optional<ProductionPlans> runningPlan =
                productionPlanRepository.findFirstByDocumentNoAndStatusOrderByIdDesc(orderNo, PlanStatus.RUNNING);
        if (runningPlan.isPresent()) {
            return runningPlan;
        }
        return productionPlanRepository.findFirstByDocumentNoOrderByIdDesc(orderNo);
    }
}
