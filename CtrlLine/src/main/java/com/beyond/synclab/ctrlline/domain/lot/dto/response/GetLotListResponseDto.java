package com.beyond.synclab.ctrlline.domain.lot.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class GetLotListResponseDto {

    private final Long lotId;
    private final String lotNo;
    private final String itemCode;
    private final String itemName;

    private final BigDecimal performanceQty;
    private final BigDecimal defectiveQty;
    private final BigDecimal defectiveRate;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isDeleted;
}
