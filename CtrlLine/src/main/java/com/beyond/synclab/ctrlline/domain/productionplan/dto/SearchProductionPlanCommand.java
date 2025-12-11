package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchProductionPlanCommand(
    List<PlanStatus> status,
    String factoryName,
    String salesManagerNo,
    String salesManagerName,
    String productionManagerNo,
    String itemCode,
    String factoryCode,
    String productionManagerName,
    String itemName,
    LocalDate dueDateFrom,
    LocalDate dueDateTo,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
