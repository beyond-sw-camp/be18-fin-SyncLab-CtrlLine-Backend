package com.beyond.synclab.ctrlline.domain.productionplan.dto;


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
public class GetAllProductionPlanResponseDto {
    private Long id;
    private String documentNo;
    private ProductionPlans.PlanStatus status;
    private String factoryName;
    private String lineName;
    private String itemCode;
    private String itemName;
    private String itemSpecification;
    private BigDecimal plannedQty;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDate dueDate;
    private String salesManagerName;
    private String salesManagerNo;
    private String productionManagerName;
    private String productionManagerNo;
    private String remark;

    public static GetAllProductionPlanResponseDto fromEntity(ProductionPlans productionPlans) {
        return GetAllProductionPlanResponseDto.builder()
            .id(productionPlans.getId())
            .documentNo(productionPlans.getDocumentNo())
            .status(productionPlans.getStatus())
            .factoryName(productionPlans.getItemLine().getLine().getFactory().getFactoryName())
            .lineName(productionPlans.getItemLine().getLine().getLineName())
            .itemCode(productionPlans.getItemLine().getItem().getItemCode())
            .itemName(productionPlans.getItemLine().getItem().getItemName())
            .itemName(productionPlans.getItemLine().getItem().getItemSpecification())
            .plannedQty(productionPlans.getPlannedQty())
            .startTime(productionPlans.getStartTime())
            .endTime(productionPlans.getEndTime())
            .dueDate(productionPlans.getDueDate())
            .salesManagerNo(productionPlans.getSalesManager().getEmpNo())
            .salesManagerName(productionPlans.getSalesManager().getName())
            .productionManagerName(productionPlans.getProductionManager().getName())
            .productionManagerNo(productionPlans.getProductionManager().getEmpNo())
            .remark(productionPlans.getRemark())
            .build();
    }
}
