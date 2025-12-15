package com.beyond.synclab.ctrlline.domain.productionplan.vo;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PlanScheduleSlot {
    private final Long planId;
    private final ProductionPlans.PlanStatus status;

    private final String documentNo;
    private final LocalDateTime dueDateTime;
    private final LocalDateTime originalStartTime;
    private final LocalDateTime originalEndTime;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public boolean isAnchor() {
        return status == ProductionPlans.PlanStatus.RUNNING || status == ProductionPlans.PlanStatus.COMPLETED;
    }

    public boolean isMovable() {
        return status == ProductionPlans.PlanStatus.PENDING || status == ProductionPlans.PlanStatus.CONFIRMED;
    }

    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    public void shiftBy(Duration delta) {
        this.startTime = this.startTime.plus(delta);
        this.endTime   = this.endTime.plus(delta);
    }

    public void updateSchedule(LocalDateTime newStart, LocalDateTime newEnd) {
        this.startTime = newStart;
        this.endTime = newEnd;
    }

    public void moveTo(LocalDateTime newStart) {
        Duration d = duration();
        this.startTime = newStart;
        this.endTime = newStart.plus(d);
    }

    public static PlanScheduleSlot fromEntity(ProductionPlans plan, LocalDateTime actualEndTime) {
        return PlanScheduleSlot.builder()
                .planId(plan.getId())
                .status(plan.getStatus())
                .documentNo(plan.getDocumentNo())
                .originalStartTime(plan.getStartTime())
                .originalEndTime(plan.getEndTime())
                .dueDateTime(plan.getDueDate().atStartOfDay().withHour(12))
                .startTime(plan.getStartTime())
                .endTime(actualEndTime)
                .build();
    }

}
