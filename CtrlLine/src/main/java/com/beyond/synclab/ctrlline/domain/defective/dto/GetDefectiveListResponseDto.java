package com.beyond.synclab.ctrlline.domain.defective.dto;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
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

    public static GetDefectiveListResponseDto fromEntity(PlanDefectives px, ProductionPerformances perf) {
        Items item = px.getProductionPlan().getItemLine().getItem();
        Lines line = px.getProductionPlan().getItemLine().getLine();

        return GetDefectiveListResponseDto.builder()
            .planDefectiveId(px.getId())
            .defectiveDocNo(px.getDefectiveDocumentNo())
            .itemId(item.getId())
            .itemCode(item.getItemCode())
            .itemName(item.getItemName())
            .lineId(line.getId())
            .lineCode(line.getLineCode())
            .lineName(line.getLineName())
            .defectiveTotalQty(perf.getPerformanceDefectiveQty())
            .defectiveTotalRate(perf.getPerformanceDefectiveRate())
            .productionPerformanceDocNo(perf.getPerformanceDocumentNo())
            .createdAt(px.getCreatedAt())
            .build();
    }
}
