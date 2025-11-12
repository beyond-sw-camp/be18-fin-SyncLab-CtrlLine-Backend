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
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProductionPlanResponseDto {
    private String documentNo;
    private LocalDate dueDate;
    private ProductionPlans.PlanStatus status;
    private Long salesManagerNo;
    private Long productionManagerNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String factoryCode;
    private String itemCode;
    private BigDecimal plannedQty;
    private String lineCode;
    private String remark;
}
