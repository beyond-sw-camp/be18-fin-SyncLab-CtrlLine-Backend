package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PlanScheduleChangeResponseDto {
    private Long planId;  // 업데이트 or 신규 삽입된 plan id
    private String planDocumentNo;
    private String previewKey;

    private List<AffectedPlanDto> affectedPlans;

    private List<DueDateExceededPlanDto> dueDateExceededPlans;
}
