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
@NoArgsConstructor
public class GetProductionPlanDetailResponseDto {
    private Long id;
    private String planDocumentNo;
    private LocalDate dueDate;
    private ProductionPlans.PlanStatus status;
    private String salesManagerNo;
    private String salesManagerName;
    private String productionManagerNo;
    private String productionManagerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualEndTime;
    private Long factoryId;
    private String factoryCode;
    private String factoryName;
    private Long itemId;
    private String itemSpecification;
    private String itemUnit;
    private String itemCode;
    private String itemName;
    private BigDecimal plannedQty;
    private Long lineId;
    private String lineCode;
    private String lineName;
    private String remark;

    @Deprecated
    public static GetProductionPlanDetailResponseDto fromEntity(ProductionPlans productionPlans, Factories factories, Items items, LocalDateTime actualEndTime) {
        return GetProductionPlanDetailResponseDto.builder()
            .id(productionPlans.getId())
            .planDocumentNo(productionPlans.getDocumentNo())
            .dueDate(productionPlans.getDueDate())
            .status(productionPlans.getStatus())
            .salesManagerNo(productionPlans.getSalesManager().getEmpNo())
            .salesManagerName(productionPlans.getSalesManager().getName())
            .productionManagerNo(productionPlans.getProductionManager().getEmpNo())
            .productionManagerName(productionPlans.getProductionManager().getName())
            .startTime(productionPlans.getStartTime())
            .endTime(productionPlans.getEndTime())
            .actualEndTime(actualEndTime)
            .lineId(productionPlans.getItemLine().getLineId())
            .factoryId(factories.getId())
            .factoryCode(factories.getFactoryCode())
            .factoryName(factories.getFactoryName())
            .itemId(items.getId())
            .itemUnit(items.getItemUnit())
            .itemSpecification(items.getItemSpecification())
            .itemCode(items.getItemCode())
            .itemName(items.getItemName())
            .plannedQty(productionPlans.getPlannedQty())
            .lineCode(productionPlans.getItemLine().getLine().getLineCode())
            .lineName(productionPlans.getItemLine().getLine().getLineName())
            .remark(productionPlans.getRemark())
            .build();
    }
}
