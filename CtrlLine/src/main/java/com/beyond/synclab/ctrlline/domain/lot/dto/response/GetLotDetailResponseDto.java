package com.beyond.synclab.ctrlline.domain.lot.dto.response;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetLotDetailResponseDto {

    private final Long lotId;
    private final String lotNo;
    private final String factoryCode;
    private final String lineCode;
    private final String productionManagerNo;
    private final String productionPerformanceDocNo;
    private final String remark;

    private final String itemCode;
    private final String itemName;

    private final Integer lotQty;
    private final Integer performanceQty;
    private final Integer defectiveQty;
    private final Integer defectiveRate;

    private final String serialFilePath;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isDeleted;

    public static GetLotDetailResponseDto fromEntity(
            Lots lot,
            ProductionPerformances perf,
            String serialFilePath
    ) {

        ProductionPlans plan = perf.getProductionPlan();
        Items item = plan.getItemLine().getItem();
        Lines line = plan.getItemLine().getLine();

        return GetLotDetailResponseDto.builder()
                .lotId(lot.getId())
                .lotNo(lot.getLotNo())

                .factoryCode(line.getFactory().getFactoryCode())
                .lineCode(line.getLineCode())

                .productionManagerNo(plan.getProductionManager().getEmpNo())
                .productionPerformanceDocNo(perf.getPerformanceDocumentNo())
                .remark(perf.getRemark())

                .itemCode(item.getItemCode())
                .itemName(item.getItemName())

                .lotQty(perf.getTotalQty() != null ? perf.getTotalQty().intValue() : 0)
                .performanceQty(perf.getPerformanceQty() != null ? perf.getPerformanceQty().intValue() : 0)
                .defectiveQty(perf.getPerformanceDefectiveQty() != null ? perf.getPerformanceDefectiveQty().intValue() : 0)
                .defectiveRate(perf.getPerformanceDefectiveRate() != null ? perf.getPerformanceDefectiveRate().intValue() : 0)

                .serialFilePath(serialFilePath)

                .createdAt(lot.getCreatedAt())
                .updatedAt(lot.getUpdatedAt())
                .isDeleted(lot.getIsDeleted())
                .build();
    }
}
