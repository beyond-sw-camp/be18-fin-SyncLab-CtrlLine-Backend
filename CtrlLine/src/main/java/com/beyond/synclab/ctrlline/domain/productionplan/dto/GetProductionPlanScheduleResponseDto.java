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
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetProductionPlanScheduleResponseDto {
    private Long id;

    private String lineCode;
    private String lineName;

    private String factoryCode;
    private String factoryName;

    private String salesManagerNo;
    private String productionManagerNo;

    private String documentNo;

    private String itemName;
    private String itemCode;

    private ProductionPlans.PlanStatus status;
    private LocalDate dueDate;
    private BigDecimal plannedQty;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualEndTime;

    private String remark;

    public static GetProductionPlanScheduleResponseDto fromEntity(ProductionPlans pp, LocalDateTime actualEndTime) {
        return GetProductionPlanScheduleResponseDto.builder()
            .id(pp.getId())
            .lineCode(pp.getItemLine().getLine().getLineCode())
            .lineName(pp.getItemLine().getLine().getLineName())
            .factoryCode(pp.getItemLine().getLine().getFactory().getFactoryCode())
            .factoryName(pp.getItemLine().getLine().getFactory().getFactoryName())
            .salesManagerNo(pp.getSalesManager().getEmpNo())
            .productionManagerNo(pp.getProductionManager().getEmpNo())
            .documentNo(pp.getDocumentNo())
            .itemName(pp.getItemLine().getItem().getItemName())
            .itemCode(pp.getItemLine().getItem().getItemCode())
            .status(pp.getStatus())
            .dueDate(pp.getDueDate())
            .plannedQty(pp.getPlannedQty())
            .startTime(pp.getStartTime())
            .endTime(pp.getEndTime())
            .actualEndTime(actualEndTime)
            .remark(pp.getRemark())
            .build();
    }
}
