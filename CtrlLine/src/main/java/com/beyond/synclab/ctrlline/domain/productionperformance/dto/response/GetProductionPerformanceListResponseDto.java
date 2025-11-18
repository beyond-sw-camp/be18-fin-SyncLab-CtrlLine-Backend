package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductionPerformanceListResponseDto {

    private final String documentNo;
    private final String salesManagerNo;
    private final String productionManagerNo;
    private final String factoryCode;
    private final String lineCode;
    private final String itemCode;
    private final Double performanceQty;
    private final Double performanceResultQty;
    private final Double defectRate;
    private final String remark;
}
