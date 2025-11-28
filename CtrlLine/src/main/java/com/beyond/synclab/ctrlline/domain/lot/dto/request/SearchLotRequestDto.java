package com.beyond.synclab.ctrlline.domain.lot.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class SearchLotRequestDto {

    private String lotNo;
    private String itemCode;
    private Boolean isDeleted;
    private String factoryCode;
    private String lineCode;
    private String productionManagerNo;

    // 생산실적 기반 검색 조건
    private String performanceDocumentNo;

    // 수량 범위 조건
    private BigDecimal minPerformanceQty;
    private BigDecimal maxPerformanceQty;
    private BigDecimal minDefectiveQty;
    private BigDecimal maxDefectiveQty;
    private BigDecimal minDefectiveRate;
    private BigDecimal maxDefectiveRate;

    // 날짜 검색 (LOT 자체 생성일/수정일)
    private LocalDate createdAtFrom;
    private LocalDate createdAtTo;
    private LocalDate updatedAtFrom;
    private LocalDate updatedAtTo;
}
