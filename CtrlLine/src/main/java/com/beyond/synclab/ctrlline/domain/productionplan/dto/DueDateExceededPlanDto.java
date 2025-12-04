package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DueDateExceededPlanDto {
    private Long id;
    private LocalDateTime newEndTime;
    private LocalDateTime dueDateLimit;
}
