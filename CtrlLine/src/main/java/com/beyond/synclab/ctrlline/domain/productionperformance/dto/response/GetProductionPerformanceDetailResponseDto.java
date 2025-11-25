package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductionPerformanceDetailResponseDto {

    private final String documentNo;
    private final String factoryCode;
    private final String lineCode;
    private final String salesManagerNo;
    private final String productionManagerNo;
    private final String lotNo;
    private final String itemCode;
    private final String itemName;
    private final String itemSpecification;
    private final String itemUnit;
    private final BigDecimal totalQty;
    private final BigDecimal performanceQty;
    private final BigDecimal defectiveRate;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDate dueDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
