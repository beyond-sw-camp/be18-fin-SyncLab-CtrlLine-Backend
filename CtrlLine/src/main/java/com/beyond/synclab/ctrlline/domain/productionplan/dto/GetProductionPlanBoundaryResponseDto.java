package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetProductionPlanBoundaryResponseDto {
    private final LocalDateTime earliestStartTime;
    private final LocalDateTime latestEndTime;
}
