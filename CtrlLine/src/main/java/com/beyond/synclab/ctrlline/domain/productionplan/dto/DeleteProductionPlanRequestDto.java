package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteProductionPlanRequestDto {
    @NotEmpty(message = "생산계획 ID가 비어있으면 안됩니다.")
    private List<Long> planIds;
}
