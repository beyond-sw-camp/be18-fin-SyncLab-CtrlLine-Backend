package com.beyond.synclab.ctrlline.domain.productionperformance.dto.request;

import lombok.*;

import java.math.BigDecimal;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAllProductionPerformanceRequestDto {
    private String documentNoStart;
    private String documentNoEnd;

    private String factoryCode;
    private String lineCode;
    private String salesManagerEmpNo;
    private String productionManagerEmpNo;
    private String lotNo;
    private String itemCode;
    private String itemName;
    private String specification;
    private String unit;
    private Boolean isDeleted;

    private String startDateTimeStart;
    private String startDateTimeEnd;
    private String endDateTimeStart;
    private String endDateTimeEnd;

    private BigDecimal minTotalQty;
    private BigDecimal maxTotalQty;
    private BigDecimal minPerformanceQty;
    private BigDecimal maxPerformanceQty;
    private BigDecimal minDefectiveRate;
    private BigDecimal maxDefectiveRate;

    private String dueDateStart;
    private String dueDateEnd;
}
