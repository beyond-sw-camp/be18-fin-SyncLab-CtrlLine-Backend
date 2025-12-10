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

        // "뒤" 계획들만 조회
        List<ProductionPlans> futurePlans = productionPlanRepository
            .findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
                lineId,
                scheduledEnd
            );

        if (futurePlans.isEmpty()) return;

        // cursor = 실적 종료시간 (가장 최신 종료 기준)
        LocalDateTime cursor = actualEndTime;

        for (ProductionPlans plan : futurePlans) {

            // 시작시간이 cursor보다 앞서면 밀어야 함
            if (plan.getStartTime().isBefore(cursor)) {

                Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

                LocalDateTime newStart = cursor;
                LocalDateTime newEnd = newStart.plus(duration);

                plan.updateStartTime(newStart);
                plan.updateEndTime(newEnd);

                cursor = newEnd;
            } else {
                // 안 겹치면 그대로 cursor 이동
                cursor = plan.getEndTime();
            }
        }

        productionPlanRepository.saveAll(futurePlans);
    }
}
