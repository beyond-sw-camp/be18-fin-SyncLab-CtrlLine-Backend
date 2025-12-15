package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import jakarta.persistence.Tuple;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductionPlanReconciliationService {

    private final ProductionPerformanceRepository performanceRepository;

    @Transactional
    public void reconcileWithActualEndTimes(List<ProductionPlans> plans) {

        if (plans == null || plans.isEmpty()) return;

        Map<Long, LocalDateTime> actualEndMap =
                performanceRepository.findLatestActualEndTimeTuples(
                    plans.stream().map(ProductionPlans::getId).toList()
                ).stream()
                .collect(Collectors.toMap(
                t -> t.get("planId", Long.class),
                t -> t.get("actualEnd", LocalDateTime.class)
                ));

        plans.sort(Comparator.comparing(ProductionPlans::getStartTime));

        LocalDateTime cursor = null;

        for (ProductionPlans plan : plans) {

            /** 1) COMPLETED 이면 cursor = 실제 종료시간 또는 예정 종료시간 중 더 큰 값 */
            if (plan.isCompleted()) {
                LocalDateTime scheduledEnd = plan.getEndTime();
                LocalDateTime actualEnd = actualEndMap.get(plan.getId());

                if (actualEnd != null && actualEnd.isAfter(scheduledEnd)) {
                    cursor = actualEnd;   // ← 계획을 변경하지 않고 cursor만 이동
                } else {
                    cursor = scheduledEnd;
                }
            }
            /** 2) RUNNING(진행 중) 계획 → 편의상 예정 종료시간을 cursor로 둔다 */
            else if (plan.isRunning()) {
                cursor = plan.getEndTime();
            }

            /** 3) PENDING / CONFIRMED 계획: cursor 이후로 밀어준다 */
            else if (cursor != null && plan.getStartTime().isBefore(cursor)) {

                Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

                LocalDateTime newStart = cursor;
                LocalDateTime newEnd = newStart.plus(duration);

                plan.updateStartTime(newStart);
                plan.updateEndTime(newEnd);

                cursor = newEnd;

            } else {
                cursor = plan.getEndTime();
            }
        }
    }
}
