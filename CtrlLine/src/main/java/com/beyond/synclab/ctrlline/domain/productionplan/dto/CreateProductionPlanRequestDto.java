package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateProductionPlanRequestDto {

    @NotNull(message = "납기일은 필수입니다.")
    private LocalDate dueDate;

    @NotNull(message = "상태는 필수입니다.")
    private ProductionPlans.PlanStatus status;

    @NotNull(message = "Sales Manager 사번은 필수입니다.")
    private String salesManagerNo;

    @NotNull(message = "Production Manager 사번은 필수입니다.")
    private String productionManagerNo;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalDateTime endTime;

    @NotBlank(message = "공장 코드는 필수입니다.")
    private String factoryCode;

    @NotBlank(message = "품목 코드는 필수입니다.")
    private String itemCode;

    @NotNull(message = "계획 수량은 필수입니다.")
    private BigDecimal plannedQty;

    @NotBlank(message = "라인 코드는 필수입니다.")
    private String lineCode;

    @Size(max = 500, message = "비고는 최대 500자까지 가능합니다.")
    private String remark;

    public ProductionPlans toEntity(Users salesManager, Users productionManager, Lines line, String documentNo) {
        return ProductionPlans.builder()
                .line(line)
                .salesManager(salesManager)
                .productionManager(productionManager)
                .documentNo(documentNo)
                .status(status)
                .dueDate(this.dueDate)
                .plannedQty(this.plannedQty)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .remark(this.remark)
                .build();
    }
}
