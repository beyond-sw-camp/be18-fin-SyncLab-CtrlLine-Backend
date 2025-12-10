package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanDelayService {
    private final ProductionPlanRepository productionPlanRepository;

    @Transactional
    public void applyRealPerformanceDelay(ProductionPlans completedPlan, LocalDateTime actualEndTime) {


        if (completedPlan == null || actualEndTime == null) return;

        Long lineId = completedPlan.getItemLine().getLineId();
        LocalDateTime scheduledEnd = completedPlan.getEndTime();

        // 해당 라인의 활성 계획 전체 조회 (뒤로 미루거나 당기려면 전체가 필요)
        List<ProductionPlans> plans =
            productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
                lineId,
                List.of(
                    ProductionPlans.PlanStatus.PENDING,
                    ProductionPlans.PlanStatus.CONFIRMED,
                    ProductionPlans.PlanStatus.RUNNING
                )
            );

        if (plans.isEmpty()) return;

        LocalDateTime cursor = null;

        for (ProductionPlans plan : plans) {

            // 1) COMPLETED → cursor 를 실제 또는 예정 종료시간 중 더 큰 값으로
            if (plan.getId().equals(completedPlan.getId())) {

                // 실제시간이 예정보다 늦음 → cursor = actualEnd
                // 실제시간이 예정보다 빠름 → cursor = scheduledEnd
                cursor = actualEndTime.isAfter(scheduledEnd)
                    ? actualEndTime
                    : scheduledEnd;

                continue; // 본인 계획은 이동시키지 않음
            }

            if (cursor == null) {
                // 아직 completedPlan 이전의 계획들 → 건드리지 않음
                cursor = plan.getEndTime();
            }

            // 2) cursor 이후 계획들은 이동 대상
            else if (plan.getStartTime().isBefore(cursor)) {

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

        productionPlanRepository.saveAll(plans);
    }
}
