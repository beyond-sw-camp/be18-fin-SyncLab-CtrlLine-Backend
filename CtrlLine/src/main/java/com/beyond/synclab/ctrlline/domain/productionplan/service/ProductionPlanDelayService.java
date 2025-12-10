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

        LocalDateTime scheduledEnd = completedPlan.getEndTime();
        if (actualEndTime.isBefore(scheduledEnd)) {
            return; // 지연 없음
        }

        long delayMinutes = Duration.between(scheduledEnd, actualEndTime).toMinutes();
        if (delayMinutes <= 0) return;

        Long lineId = completedPlan.getItemLine().getLineId();

        // 지연 영향을 받을 뒤 계획들 조회
        List<ProductionPlans> futurePlans = productionPlanRepository
            .findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
                lineId, scheduledEnd
            );

        LocalDateTime cursor = actualEndTime;

        for (ProductionPlans plan : futurePlans) {
            Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

            LocalDateTime newStart = cursor;
            LocalDateTime newEnd = newStart.plus(duration);

            plan.updateStartTime(newStart);
            plan.updateEndTime(newEnd);

            cursor = newEnd;
        }

        productionPlanRepository.saveAll(futurePlans);
    }
}
