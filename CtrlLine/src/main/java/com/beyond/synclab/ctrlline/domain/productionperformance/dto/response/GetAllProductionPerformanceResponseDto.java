package com.beyond.synclab.ctrlline.domain.productionperformance.dto.response;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GetAllProductionPerformanceResponseDto {

    private final Long id;
    private final String documentNo;

    private final String factoryCode;
    private final String factoryName;
    private final String lineCode;
    private final String lineName;
    private final String salesManagerEmpNo;
    private final String salesManagerEmpName;
    private final String productionManagerEmpNo;
    private final String productionManagerEmpName;

    private final String lotNo;
    private final String itemCode;
    private final String itemName;
    private final String itemSpecification;
    private final String unit;

    private final BigDecimal totalQty;
    private final BigDecimal performanceQty;
    private final BigDecimal defectiveQty;
    private final BigDecimal defectiveRate;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDate dueDate;

    private final String remark;


    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 변환 메서드
    public static GetAllProductionPerformanceResponseDto fromEntity(
            ProductionPerformances perf,
            Lots lot
    ) {
        ProductionPlans plan = perf.getProductionPlan();
        Items item = plan.getItemLine().getItem();
        Lines line = plan.getItemLine().getLine();
        Factories factory = line.getFactory();

        return GetAllProductionPerformanceResponseDto.builder()
                .id(perf.getId())
                .documentNo(perf.getPerformanceDocumentNo())

                .factoryCode(factory.getFactoryCode())
                .factoryName(factory.getFactoryName())

                .lineCode(line.getLineCode())
                .lineName(line.getLineName())

                .salesManagerEmpNo(plan.getSalesManager().getEmpNo())
                .salesManagerEmpName(plan.getSalesManager().getName())

                .productionManagerEmpNo(plan.getProductionManager().getEmpNo())
                .productionManagerEmpName(plan.getProductionManager().getName())

                .lotNo(lot != null ? lot.getLotNo() : null)

                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpecification(item.getItemSpecification())
                .unit(item.getItemUnit())

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