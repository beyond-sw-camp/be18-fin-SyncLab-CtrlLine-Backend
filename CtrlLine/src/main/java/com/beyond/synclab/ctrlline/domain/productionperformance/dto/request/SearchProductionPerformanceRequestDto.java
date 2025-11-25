package com.beyond.synclab.ctrlline.domain.productionperformance.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SearchProductionPerformanceRequestDto {

    private String documentNo;
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
    private String salesManagerName;
    private String producerManagerName;
    private String startDate;
    private String endDate;
    private String dueDate;
    private String remark;
    private Boolean isDeleted;
}
