package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SearchProductionPlanCommand(
    PlanStatus status,
    String factoryName,
    String salesManagerName,
    String productionManagerName,
    String itemName,
    LocalDate dueDate,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
