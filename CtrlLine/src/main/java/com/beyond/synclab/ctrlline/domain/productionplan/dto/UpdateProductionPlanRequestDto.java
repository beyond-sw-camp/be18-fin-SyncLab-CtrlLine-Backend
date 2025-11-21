package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProductionPlanRequestDto {
    private PlanStatus status;
    private String salesManagerNo;
    private String productionManagerNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private String factoryCode;
    private String itemCode;
    private String lineCode;
    private LocalDate dueDate;
    private BigDecimal plannedQty;
}
