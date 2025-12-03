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
    private String factoryName;
    private String lineCode;
    private String lineName;
    private String salesManagerEmpNo;
    private String salesManagerName;
    private String productionManagerEmpNo;
    private String productionManagerName;

    private String lotNo;
    private String defectiveDocumentNo;
    private String productionPlanDocumentNo;

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
