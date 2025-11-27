package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetProductionPlanEndTimeRequestDto {
    private LocalDateTime startTime;
    private BigDecimal plannedQty;
    private String lineCode;
}
