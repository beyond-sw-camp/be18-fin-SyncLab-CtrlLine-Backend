package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record GetProductionPlanScheduleRequestDto(
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    String factoryName,
    String factoryCode,
    String lineName,
    String lineCode
) {
}
