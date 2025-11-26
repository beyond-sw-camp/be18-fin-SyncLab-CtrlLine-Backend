package com.beyond.synclab.ctrlline.domain.defective.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXrefs;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    private String itemCode;           // 품목 코드
    private String itemName;           // 품목명
    private String itemSpecification;  // 규격
    private String itemUnit;           // 단위
    private BigDecimal totalQty;          // 투입 수량

    @JsonProperty("defectives")
    private List<DefectiveItem> defectiveItems;

    public static GetDefectiveDetailResponseDto fromEntity(PlanDefectives planDefectives, List<PlanDefectiveXrefs> planDefectiveXrefs) {
        BigDecimal totalDefectiveQty = planDefectiveXrefs.stream().map(p -> p.getDefectiveQty()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return GetDefectiveDetailResponseDto.builder()
            .defectiveDocNo(planDefectives.getDefectiveDocumentNo())
            .factoryName(planDefectives.getProductionPlan().getItemLine().getLine().getFactory().getFactoryName())
            .lineName(planDefectives.getProductionPlan().getItemLine().getLine().getLineName())
            .itemCode(planDefectives.getProductionPlan().getItemLine().getItem().getItemCode())
            .itemName(planDefectives.getProductionPlan().getItemLine().getItem().getItemName())
            .itemSpecification(planDefectives.getProductionPlan().getItemLine().getItem().getItemSpecification())
            .itemUnit(planDefectives.getProductionPlan().getItemLine().getItem().getItemUnit())
            .totalQty(planDefectives.getProductionPlan().getPlannedQty())
            .defectiveItems(planDefectiveXrefs.stream().map(p ->
                DefectiveItem.builder()
                    .equipmentName(p.getDefective().getEquipment().getEquipmentName())
                    .equipmentCode(p.getDefective().getEquipment().getEquipmentCode())
                    .defectiveCode(p.getDefective().getDefectiveCode())
                    .defectiveType(p.getDefective().getDefectiveType())
                    .defectiveName(p.getDefective().getDefectiveName())
                    .defectiveQty(p.getDefectiveQty())
                    .defectiveRate(p.getDefectiveQty().divide(totalDefectiveQty, 2, RoundingMode.FLOOR).doubleValue())
                    .build()
                    )
                .toList()
            )
            .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DefectiveItem {
        private String equipmentName;      // 설비 이름
        private String equipmentCode;      // 설비 코드
        private String defectiveCode;      // 불량 코드
        private String defectiveType;      // 불량 유형
        private String defectiveName;      // 불량 명칭
        private BigDecimal defectiveQty;      // 불량 수량
        private Double defectiveRate;      // 불량률(예: 3.5%)
    }
}
