package com.beyond.synclab.ctrlline.domain.productionperformance.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class SearchProductionPerformanceRequestDto {

    private String documentDateFrom;
    private String documentDateTo;
    private String factoryCode;
    private String lineCode;
    private String itemCode;
    private String productionPlanDocumentNo;
    private String defectiveDocumentNo;
    private String lotNo;
    private BigDecimal minTotalQty;
    private BigDecimal maxTotalQty;
    private BigDecimal minPerformanceQty;
    private BigDecimal maxPerformanceQty;
    private BigDecimal minDefectRate;
    private BigDecimal maxDefectRate;
    private String salesManagerNo;
    private String producerManagerNo;
    private String startTimeFrom;
    private String startTimeTo;
    private String endTimeFrom;
    private String endTimeTo;
    private String dueDateFrom;
    private String dueDateTo;
    private String remark;
    private Boolean isDeleted;
}
