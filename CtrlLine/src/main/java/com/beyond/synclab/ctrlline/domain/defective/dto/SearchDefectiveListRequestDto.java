package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder(toBuilder = true)
public record SearchDefectiveListRequestDto(
    LocalDate fromDate,
    LocalDate toDate,
    String productionPerformanceDocNo
) {
}
