package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private BigDecimal defectiveTotalRate;
    private String productionPerformanceDocNo;
    private LocalDateTime createdAt;
}
