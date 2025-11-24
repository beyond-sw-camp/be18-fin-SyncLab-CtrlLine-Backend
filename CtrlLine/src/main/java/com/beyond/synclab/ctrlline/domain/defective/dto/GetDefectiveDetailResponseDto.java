package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetDefectiveDetailResponseDto {
    private String defectiveDocNo;     // 불량 전표번호
    private String factoryName;        // 공장명
    private String lineName;           // 라인명
    private String equipmentName;      // 설비 이름
    private String equipmentCode;      // 설비 코드

    private String defectiveCode;      // 불량 코드
    private String defectiveType;      // 불량 유형
    private String defectiveName;      // 불량 명칭

    private String itemCode;           // 품목 코드
    private String itemName;           // 품목명
    private String itemSpecification;  // 규격
    private String itemUnit;           // 단위

    private BigDecimal totalQty;          // 투입 수량
    private BigDecimal defectiveQty;      // 불량 수량
    private Double defectiveRate;      // 불량률(예: 3.5%)

    private LocalDateTime createdAt;
}
