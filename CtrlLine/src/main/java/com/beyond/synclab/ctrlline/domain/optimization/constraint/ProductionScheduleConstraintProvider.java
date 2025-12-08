package com.beyond.synclab.ctrlline.domain.optimization.constraint;

import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import java.time.Duration;
import java.time.LocalDateTime;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.springframework.stereotype.Component;

@Component
public class ProductionScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
            // HARD
            noOverlap(factory),
            lockedPlansCannotMove(factory),
            startTimeMustBeInFuture(factory),
            dueDateMustBeRespected(factory),

            // SOFT
            minimizeIdleTime(factory),
            minimizeTardiness(factory),
            orderByDueDate(factory)
        };
    }


    /* ============================================================
     * HARD 1 — Overlap 금지
     * ============================================================ */
    Constraint noOverlap(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                ProductionPlanAssignment.class,
                Joiners.filtering(this::overlaps)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("NoOverlap");
    }

    private boolean overlaps(ProductionPlanAssignment a, ProductionPlanAssignment b) {
        if (a.getStartTime() == null || a.getEndTime() == null ||
            b.getStartTime() == null || b.getEndTime() == null) {
            return false;
        }
        return a.getStartTime().isBefore(b.getEndTime()) &&
            b.getStartTime().isBefore(a.getEndTime());
    }

    /* ============================================================
     * HARD 2 — Locked 플랜(Confirmed + Manager)는 이동 금지
     * ============================================================ */
    Constraint lockedPlansCannotMove(ConstraintFactory factory) {
        return factory.forEach(ProductionPlanAssignment.class)
            .filter(ProductionPlanAssignment::isLocked)
            .filter(a -> a.getStartTime() != null)
            .filter(a -> a.getOriginalStartTime() != null)
            .filter(a -> !a.getStartTime().equals(a.getOriginalStartTime()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("LockedPlanCannotMove");
    }

    /* ============================================================
     * HARD 3 — StartTime must be >= now
     * ============================================================ */
    Constraint startTimeMustBeInFuture(ConstraintFactory factory) {
        LocalDateTime now = LocalDateTime.now();

        return factory.forEach(ProductionPlanAssignment.class)
            .filter(a -> a.getStartTime() != null)
            .filter(a -> a.getStartTime().isBefore(now))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("StartTimeInFuture");
    }


    /* ============================================================
     * HARD 4 — DueDate 초과 금지
     * ============================================================ */
    Constraint dueDateMustBeRespected(ConstraintFactory factory) {
        return factory.forEach(ProductionPlanAssignment.class)
            .filter(a -> a.getEndTime() != null && a.getDueDateTime() != null)
            .filter(a -> a.getEndTime().isAfter(a.getDueDateTime()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("RespectDueDate");
    }


    /* ============================================================
     * SOFT 1 — Idle 최소화
     * ============================================================ */
    Constraint minimizeIdleTime(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                ProductionPlanAssignment.class,
                Joiners.lessThan(ProductionPlanAssignment::getStartTime)
            )
            .penalize(
                HardSoftScore.ONE_SOFT,
                (a, b) -> {
                    if (a.getEndTime() == null || b.getStartTime() == null) return 0;

                    long gap = Duration.between(a.getEndTime(), b.getStartTime()).toMinutes();
                    return gap > 0 ? (int) gap : 0;
                }
            )
            .asConstraint("MinimizeIdleGap");
    }

    /* ============================================================
     * SOFT 2 — 지각(Tardiness) 최소화
     * ============================================================ */
    Constraint minimizeTardiness(ConstraintFactory factory) {
        return factory.forEach(ProductionPlanAssignment.class)
            .filter(a -> a.getEndTime() != null && a.getDueDateTime() != null)
            .penalize(
                HardSoftScore.ONE_SOFT,
                a -> {
                    long tardiness = Duration.between(a.getDueDateTime(), a.getEndTime()).toMinutes();
                    return (int) Math.max(0, tardiness); // late → positive, early → 0
                }
            )
            .asConstraint("MinimizeTardiness");
    }

    /* ============================================================
     * SOFT 3 — DueDate 빠른 작업은 먼저 배치
     * ============================================================
     * early.dueDate < late.dueDate 이지만
     * early.start > late.start 이면 페널티.
     *
     * 즉 dueDate 순서를 강제 정렬한다.
     * ============================================================ */
    Constraint orderByDueDate(ConstraintFactory factory) {

        return factory.forEachUniquePair(
                ProductionPlanAssignment.class,
                Joiners.lessThan(ProductionPlanAssignment::getDueDateTime) // early < late
            )
            .filter((early, late) ->
                early.getStartTime() != null &&
                    late.getStartTime() != null &&
                    early.getStartTime().isAfter(late.getStartTime()) // 순서 뒤집힘
            )
            .penalize(
                HardSoftScore.ofSoft(100_000), // 아주 큰 soft penalty → 정렬 강제
                (early, late) -> 1
            )
            .asConstraint("OrderByDueDateAsc");
    }

}
