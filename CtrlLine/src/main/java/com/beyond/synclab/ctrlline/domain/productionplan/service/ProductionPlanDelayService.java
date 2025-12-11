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

        // completedPlan 이후 계획들만 조회
        List<ProductionPlans> futurePlans =
            productionPlanRepository.findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
                lineId,
                scheduledEnd
            );

        log.debug("futurePlans : {}", futurePlans);

        if (futurePlans.isEmpty()) return;

        // cursor = 실제 종료시간
        LocalDateTime cursor = actualEndTime;

        for (ProductionPlans plan : futurePlans) {

            Duration duration = Duration.between(plan.getStartTime(), plan.getEndTime());

            // --- 1) 밀기: 계획 시작이 cursor보다 앞이면 뒤로 미룬다 ---
            // --- 2) 당기기: 계획 시작이 cursor보다 충분히 뒤에 있으면 당겨온다 ---
            LocalDateTime newStart = cursor;
            LocalDateTime newEnd = newStart.plus(duration);

            plan.updateStartTime(newStart);
            plan.updateEndTime(newEnd);

            cursor = newEnd;
        }

        productionPlanRepository.saveAll(futurePlans);
    }

    @Transactional
    public void applyOngoingDelay(ProductionPlans runningPlan, long delayMinutes) {
        if (delayMinutes <= 0) return;

        Long lineId = runningPlan.getItemLine().getLineId();
        LocalDateTime scheduledEnd = runningPlan.getEndTime();

        List<ProductionPlans> futurePlans =
            productionPlanRepository.findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
                lineId,
                scheduledEnd
            );

        for (ProductionPlans plan : futurePlans) {

            LocalDateTime newStart = plan.getStartTime().plusMinutes(delayMinutes);
            LocalDateTime newEnd   = plan.getEndTime().plusMinutes(delayMinutes);

            log.info("RUNNING 지연 반영 → 계획 밀기: documentNo={} {} → {}",
                plan.getDocumentNo(), plan.getStartTime(), newStart
            );

            plan.updateSchedule(newStart, newEnd);
            productionPlanRepository.save(plan);
        }
    }
}
