package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductionPlanCreateRequestDto {

    @NotNull(message = "납기일은 필수입니다.")
    private LocalDate dueDate;

    @NotNull(message = "상태는 필수입니다.")
    private ProductionPlans.PlanStatus status;

    @NotNull(message = "Sales Manager 번호는 필수입니다.")
    @Positive(message = "Sales Manager 번호는 양수여야 합니다.")
    private Long salesManagerNo;

    @NotNull(message = "Production Manager 번호는 필수입니다.")
    @Positive(message = "Production Manager 번호는 양수여야 합니다.")
    private Long productionManagerNo;

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
}
