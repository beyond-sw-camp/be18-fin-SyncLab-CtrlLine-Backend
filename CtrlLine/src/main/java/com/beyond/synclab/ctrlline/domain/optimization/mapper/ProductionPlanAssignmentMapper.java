package com.beyond.synclab.ctrlline.domain.optimization.mapper;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.Duration;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductionPlanAssignmentMapper {

    @Transactional(readOnly = true)
    public ProductionPlanAssignment toAssignment(
        ProductionPlans plan,
        Users userRoleContext // 현재 최적화 실행 사용자
    ) {

        long durationMinutes = Math.max(
            1L,
            Duration.between(plan.getStartTime(), plan.getEndTime()).toMinutes()
        );

        boolean locked = isLocked(plan, userRoleContext);
        Items item = plan.getItemLine().getItem();

        return ProductionPlanAssignment.builder()
            .planId(plan.getId())
            .documentNo(plan.getDocumentNo())

            .planStatus(plan.getStatus())
            .plannedQty(plan.getPlannedQty())
            .itemId(item.getId())
            .itemCode(item.getItemCode())
            .itemName(item.getItemName())

            .durationMinutes(durationMinutes)
            .dueDateTime(plan.getDueDate().atTime(LocalTime.NOON))

            .confirmed(plan.isConfirmed())
            .locked(locked)

            .originalStartTime(plan.getStartTime())
            .startTime(plan.getStartTime())
            .endTime(plan.getEndTime())

            .build();
    }

    /**
     * Manager, User → Confirmed plan은 locked, Pending은 이동 가능
     * Admin → 모든 plan 이동 가능 (locked=false)
     */
    private boolean isLocked(ProductionPlans plan, Users user) {

        if (user.isAdminRole()) {
            return false; // confirmed도 최적화 가능
        }

        return plan.isConfirmed(); // confirmed만 lock
    }
}