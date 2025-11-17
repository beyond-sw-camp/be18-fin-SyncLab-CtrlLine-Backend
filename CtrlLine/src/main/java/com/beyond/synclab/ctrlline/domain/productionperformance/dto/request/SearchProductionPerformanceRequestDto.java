package com.beyond.synclab.ctrlline.domain.productionperformance.dto.request;

import lombok.*;

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
    private Double minTotalQty;
    private Double maxTotalQty;
    private Double minPerformanceQty;
    private Double maxPerformanceQty;
    private Double minDefectRate;
    private Double maxDefectRate;
    private String salesManagerName;
    private String producerManagerName;
    private String startDate;
    private String endDate;
    private String dueDate;
    private String remark;
    private Boolean isDeleted;
}
