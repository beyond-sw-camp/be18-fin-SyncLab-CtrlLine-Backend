package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetProductionPlanListResponseDto {
    private Long id;
    private String documentNo;
    private ProductionPlans.PlanStatus status;
    private String factoryName;
    private String salesManagerName;
    private String productionManagerName;
    private String itemName;
    private BigDecimal plannedQty;
    private LocalDate dueDate;
    private String remark;

    public static GetProductionPlanListResponseDto fromEntity(ProductionPlans productionPlans) {
        return GetProductionPlanListResponseDto.builder()
            .id(productionPlans.getId())
            .documentNo(productionPlans.getDocumentNo())
            .status(productionPlans.getStatus())
            .factoryName(productionPlans.getItemLine().getLine().getFactory().getFactoryName())
            .salesManagerName(productionPlans.getSalesManager().getName())
            .productionManagerName(productionPlans.getProductionManager().getName())
            .itemName(productionPlans.getItemLine().getItem().getItemName())
            .plannedQty(productionPlans.getPlannedQty())
            .dueDate(productionPlans.getDueDate())
            .remark(productionPlans.getRemark())
            .build();
    }
}
