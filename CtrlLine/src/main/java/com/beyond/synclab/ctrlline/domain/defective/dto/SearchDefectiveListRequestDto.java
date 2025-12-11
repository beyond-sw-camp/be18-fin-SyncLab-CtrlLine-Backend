package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder(toBuilder = true)
public record SearchDefectiveListRequestDto(
    LocalDate fromDate,
    LocalDate toDate,
    String productionPerformanceDocNo,
    String defectiveDocNo,
    String itemCode,
    String itemName,
    String lineCode,
    String lineName,
    String factoryName,
    String factoryCode,
    BigDecimal defectiveQty,
    BigDecimal defectiveRate
) {
}
