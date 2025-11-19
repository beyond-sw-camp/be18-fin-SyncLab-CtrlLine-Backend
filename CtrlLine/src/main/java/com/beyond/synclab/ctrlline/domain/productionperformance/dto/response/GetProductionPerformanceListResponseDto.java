package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class GetProductionPerformanceListResponseDto {

    private final String documentNo;
    private final String salesManagerNo;
    private final String productionManagerNo;
    private final String factoryCode;
    private final String lineCode;
    private final String itemCode;
    private final BigDecimal totalQty;
    private final BigDecimal performanceQty;
    private final BigDecimal defectRate;
    private final String remark;

//    public static GetProductionPerformanceListResponseDto of(
//            String documentNo,
//            String salesManagerNo,
//            String productionManagerNo,
//            String factoryCode,
//            String lineCode,
//            String itemCode,
//            BigDecimal totalQty,
//            BigDecimal performanceQty,
//            BigDecimal defectRate,
//            String remark
//    ) {
//        return GetProductionPerformanceListResponseDto.builder()
//                .documentNo(documentNo)
//                .salesManagerNo(salesManagerNo)
//                .productionManagerNo(productionManagerNo)
//                .factoryCode(factoryCode)
//                .lineCode(lineCode)
//                .itemCode(itemCode)
//                .totalQty(totalQty)
//                .performanceQty(performanceQty)
//                .defectRate(defectRate)
//                .remark(remark)
//                .build();
//    }
}
