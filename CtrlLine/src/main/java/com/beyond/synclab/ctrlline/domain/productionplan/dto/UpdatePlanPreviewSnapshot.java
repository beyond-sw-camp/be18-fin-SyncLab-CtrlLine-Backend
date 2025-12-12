package com.beyond.synclab.ctrlline.domain.productionplan.dto;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlanPreviewSnapshot {
    private Long planId;       // 대상 plan
    private String documentNo;
    private Long lineId;

    // 전체 플랜들의 최종 시간 정보(shift/compact 적용 후)
    private List<PlanTimeSnapshot> plans;

    // 업데이트 대상 plan의 실제 변경될 필드들
    private UpdateFieldSnapshot updateFields;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlanTimeSnapshot {
        private Long planId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateFieldSnapshot {
        private PlanStatus status;
        private Long salesManagerId;
        private Long productionManagerId;
        private String remark;
        private Long itemLineId;
        private Long lineId;
        private Long itemId;
        private LocalDate dueDate;
        private BigDecimal plannedQty;
    }
}
