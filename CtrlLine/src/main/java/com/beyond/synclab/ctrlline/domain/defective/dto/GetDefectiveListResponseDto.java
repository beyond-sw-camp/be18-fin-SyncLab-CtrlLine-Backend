package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class GetDefectiveListResponseDto {
    private Long planDefectiveId;
    private String defectiveDocNo;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long lineId;
    private String lineCode;
    private String lineName;
    private BigDecimal defectiveTotalQty;
    private Double defectiveTotalRate;
    private String productionPerformanceDocNo;
    private LocalDateTime createdAt;
}
