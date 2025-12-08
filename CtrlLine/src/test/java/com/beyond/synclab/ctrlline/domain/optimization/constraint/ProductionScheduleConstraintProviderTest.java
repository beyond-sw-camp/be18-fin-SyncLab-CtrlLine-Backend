package com.beyond.synclab.ctrlline.domain.optimization.constraint;

import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionScheduleSolution;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class ProductionScheduleConstraintProviderTest {

    private final ConstraintVerifier<
        ProductionScheduleConstraintProvider,
        ProductionScheduleSolution> constraintVerifier =
        ConstraintVerifier.build(
            new ProductionScheduleConstraintProvider(),
            ProductionScheduleSolution.class,
            ProductionPlanAssignment.class
        );

    @Test
    @DisplayName("No Overlap - 겹치면 Hard 페널티 1")
    void noOverlapPenalty() {

        ProductionPlanAssignment a = ProductionPlanAssignment.builder()
            .planId(1L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 11, 0))
            .build();

        ProductionPlanAssignment b = ProductionPlanAssignment.builder()
            .planId(2L)
            .startTime(LocalDateTime.of(2025, 1, 1, 10, 30))
            .endTime(LocalDateTime.of(2025, 1, 1, 11, 30))
            .build();

        constraintVerifier.verifyThat(ProductionScheduleConstraintProvider::noOverlap)
            .given(a, b)
            .penalizesBy(1);
    }


    @Test
    @DisplayName("Locked 플랜 이동 시 Hard penalty")
    void lockedPlanCannotMove() {

        ProductionPlanAssignment locked = ProductionPlanAssignment.builder()
            .planId(1L)
            .originalStartTime(LocalDateTime.of(2025, 1, 1, 10, 0))
            .startTime(LocalDateTime.of(2025, 1, 1, 11, 0)) // 이동됨
            .locked(true)
            .build();

        constraintVerifier.verifyThat(ProductionScheduleConstraintProvider::lockedPlansCannotMove)
            .given(locked)
            .penalizesBy(1);
    }


    @Test
    @DisplayName("DueDate 정렬 위반 시 Soft penalty")
    void orderByDueDatePenalty() {


        ProductionPlanAssignment early = ProductionPlanAssignment.builder()
            .planId(1L)
            .dueDateTime(LocalDateTime.of(2025, 1, 5, 12, 0))
            .startTime(LocalDateTime.of(2025, 1, 1, 13, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 14, 0))
            .build();

        ProductionPlanAssignment late = ProductionPlanAssignment.builder()
            .planId(2L)
            .dueDateTime(LocalDateTime.of(2025, 1, 10, 12, 0))
            .startTime(LocalDateTime.of(2025, 1, 1, 12, 0))
            .endTime(LocalDateTime.of(2025, 1, 1, 13, 0))
            .build();

        constraintVerifier.verifyThat(ProductionScheduleConstraintProvider::orderByDueDate)
            .given(early, late)
            .penalizesBy(1);
    }
}