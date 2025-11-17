package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductionPerformanceListResponseDto {

    private final String documentNo;
    private final String salesManagerNo;
    private final String productionManagerNo;
    private final String factoryCode;
    private final String lineCode;
    private final String itemCode;
    private final Double performanceQty;
    private final Double performanceResultQty;
    private final Double defectRate;
    private final String remark;

    public static GetProductionPerformanceListResponseDto fromEntity(ProductionPerformances perf) {
        return GetProductionPerformanceListResponseDto.builder()
                .documentNo(perf.getPerformanceDocumentNo())
                .salesManagerNo(perf.getProductionPlan().getSalesManager().getEmpNo())
                .productionManagerNo(perf.getProductionPlan().getProductionManager().getEmpNo())
                .factoryCode(perf.getProductionPlan().getLine().getFactory().getFactoryCode())
                .lineCode(perf.getProductionPlan().getLine().getLineCode())
                .itemCode(perf.getProductionPlan().getItem().getItemCode())
                .performanceQty(perf.getTotalQty())
                .performanceResultQty(perf.getPerformanceQty())
                .defectRate(perf.getPerformanceDefectiveRate())
                .remark(perf.getRemark())
                .build();
    }
}
