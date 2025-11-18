package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateProductionPlanRequestDto {

    @NotNull(message = "납기일은 필수입니다.")
    @FutureOrPresent(message = "납기일은 오늘 포함 이후여야 합니다.")
    private LocalDate dueDate;

    @NotNull(message = "Sales Manager 사번은 필수입니다.")
    private String salesManagerNo;

    @NotNull(message = "Production Manager 사번은 필수입니다.")
    private String productionManagerNo;

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

    public ProductionPlans toEntity(Users salesManager, Users productionManager, ItemsLines itemsLines, String documentNo) {
        return ProductionPlans.builder()
                .itemLineId(itemsLines.getId())
                .itemLine(itemsLines)
                .salesManagerId(salesManager.getId())
                .salesManager(salesManager)
                .productionManagerId(productionManager.getId())
                .productionManager(productionManager)
                .documentNo(documentNo)
                .dueDate(this.dueDate)
                .plannedQty(this.plannedQty)
                .remark(this.remark)
                .build();
    }
}
