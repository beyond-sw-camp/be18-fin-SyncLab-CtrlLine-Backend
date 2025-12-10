package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class GetProductionPerformanceListResponseDto {

    private final Long id;
    private final String documentNo;

    private final String salesManagerNo;
    private final String salesManagerName;

    private final String productionManagerNo;
    private final String productionManagerName;

    private final String factoryCode;
    private final String factoryName;

    private final String lineCode;
    private final String lineName;

    private final String itemCode;
    private final String itemName;
    private final String itemUnit;

    private final BigDecimal totalQty;
    private final BigDecimal performanceQty;
    private final BigDecimal defectiveQty;
    private final BigDecimal defectRate;

    private final String remark;

    private final Boolean isDeleted;
}
