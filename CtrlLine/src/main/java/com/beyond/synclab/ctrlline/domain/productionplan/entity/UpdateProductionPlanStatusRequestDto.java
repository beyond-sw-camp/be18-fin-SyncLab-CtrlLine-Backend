package com.beyond.synclab.ctrlline.domain.productionplan.entity;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateProductionPlanStatusRequestDto {
    @NotEmpty(message = "planIds는 최소 1개 이상이어야 합니다.")
    List<Long> planIds;

    @NotNull(message = "planStatus는 필수 값입니다.")
    PlanStatus planStatus;
}
