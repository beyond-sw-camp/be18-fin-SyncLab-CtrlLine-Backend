package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetProductionPerformanceDetailResponseDto {

    private final String documentNo;
    private final String factoryCode;
    private final String factoryName;
    private final String lineCode;
    private final String lineName;
    private final String salesManagerNo;
    private final String salesManagerName;
    private final String productionManagerNo;
    private final String productionManagerName;
    private final String remark;
    private final String lotNo;
    private final String itemCode;
    private final String itemName;
    private final String itemSpecification;
    private final String itemUnit;
    private final BigDecimal totalQty;
    private final BigDecimal performanceQty;
    private final BigDecimal defectiveQty;
    private final BigDecimal defectiveRate;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDate dueDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static GetProductionPerformanceDetailResponseDto fromEntity(
            ProductionPerformances perf,
            Lots lot
    ) {

        ProductionPlans plan = perf.getProductionPlan();
        Items item = plan.getItemLine().getItem();
        Lines line = plan.getItemLine().getLine();
        Factories factory = line.getFactory();

        return GetProductionPerformanceDetailResponseDto.builder()
                .documentNo(perf.getPerformanceDocumentNo())
                .factoryCode(factory.getFactoryCode())
                .lineCode(line.getLineCode())
                .salesManagerNo(plan.getSalesManager().getEmpNo())
                .productionManagerNo(plan.getProductionManager().getEmpNo())
                .lotNo(lot != null ? lot.getLotNo() : null)
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpecification(item.getItemSpecification())
                .itemUnit(item.getItemUnit())
                .totalQty(perf.getTotalQty())
                .performanceQty(perf.getPerformanceQty())
                .defectiveQty(perf.getPerformanceDefectiveQty())
                .defectiveRate(perf.getPerformanceDefectiveRate())
                .startTime(perf.getStartTime())
                .endTime(perf.getEndTime())
                .dueDate(plan.getDueDate())
                .remark(perf.getRemark())
                .createdAt(perf.getCreatedAt())
                .updatedAt(perf.getUpdatedAt())
                .build();
    }
}
