package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetDefectiveAllResponseDto {
    private Long planDefectiveId;
    private String defectiveDocNo;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private Long lineId;
    private String lineCode;
    private String lineName;
    private Long factoryId;
    private String factoryCode;
    private String factoryName;
    private String productionManagerName;
    private String productionManagerNo;
    private String salesManagerName;
    private String salesManagerNo;
    private BigDecimal defectiveTotalQty;
    private BigDecimal defectiveTotalRate;
    private String productionPerformanceDocNo;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
