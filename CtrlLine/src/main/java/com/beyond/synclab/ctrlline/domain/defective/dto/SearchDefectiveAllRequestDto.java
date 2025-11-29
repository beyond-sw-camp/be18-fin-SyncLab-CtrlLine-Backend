package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SearchDefectiveAllRequestDto(
    LocalDate fromDate,
    LocalDate toDate,
    LocalDate dueDate,
    String factoryCode,
    String lineCode,
    Long itemId,
    String productionManagerNo,
    String salesManagerNo,
    String productionPerformanceDocNo
) {
}
