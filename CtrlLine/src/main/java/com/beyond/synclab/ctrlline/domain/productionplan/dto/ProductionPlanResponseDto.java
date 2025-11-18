package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionPlanResponseDto {
    private Long id;
    private String lineCode;
    private String salesManagerNo;
    private String productionManagerNo;
    private String documentNo;
    private ProductionPlans.PlanStatus status;
    private LocalDate dueDate;
    private BigDecimal plannedQty;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private String factoryCode;
    private String itemCode;

    public static ProductionPlanResponseDto fromEntity(ProductionPlans productionPlans, Factories factories, Items items) {
        return ProductionPlanResponseDto.builder()
                .id(productionPlans.getId())
                .lineCode(productionPlans.getItemLine().getLine().getLineCode())
                .salesManagerNo(productionPlans.getSalesManager().getEmpNo())
                .productionManagerNo(productionPlans.getProductionManager().getEmpNo())
                .documentNo(productionPlans.getDocumentNo())
                .status(productionPlans.getStatus())
                .dueDate(productionPlans.getDueDate())
                .plannedQty(productionPlans.getPlannedQty())
                .startTime(productionPlans.getStartTime())
                .endTime(productionPlans.getEndTime())
                .remark(productionPlans.getRemark())
                .factoryCode(factories.getFactoryCode())
                .itemCode(items.getItemCode())
                .build();
    }
}
