package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder(toBuilder = true)
public record GetAllProductionPlanRequestDto (
    String factoryName,
    String lineName,
    String salesManagerName,
    String itemName,
    String itemCode,
    String productionManagerName,
    LocalDate dueDateFrom,
    LocalDate dueDateTo,
    LocalDateTime startTime,
    LocalDateTime endTime
) {}
