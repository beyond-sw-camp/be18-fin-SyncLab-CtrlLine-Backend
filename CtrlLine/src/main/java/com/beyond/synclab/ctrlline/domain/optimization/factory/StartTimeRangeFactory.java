package com.beyond.synclab.ctrlline.domain.optimization.factory;

import com.beyond.synclab.ctrlline.domain.optimization.model.ProductionPlanAssignment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class StartTimeRangeFactory {
    private static final long RANGE_BEFORE_MINUTES = 30L;
    private static final long RANGE_AFTER_HOURS = 1L;
    private static final long STEP_MINUTES = 5L;

    public List<LocalDateTime> buildStartTimeRange(List<ProductionPlanAssignment> assignments) {

        if (assignments == null || assignments.isEmpty()) {
            List<LocalDateTime> nowList = new ArrayList<>();
            nowList.add(LocalDateTime.now());
            return nowList;
        }

        LocalDateTime earliest = assignments.stream()
            .map(ProductionPlanAssignment::getOriginalStartTime)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now())
            .minusMinutes(RANGE_BEFORE_MINUTES);

        LocalDateTime latest = assignments.stream()
            .map(a -> a.getOriginalStartTime().plusMinutes(a.getDurationMinutes()))
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now())
            .plusHours(RANGE_AFTER_HOURS);

        // slot + endTime 포함 위해 Set 사용
        TreeSet<LocalDateTime> rangeSet = new TreeSet<>();

        // 기존 slot 생성
        LocalDateTime cursor = earliest;
        while (!cursor.isAfter(latest)) {
            rangeSet.add(cursor);
            cursor = cursor.plusMinutes(STEP_MINUTES);
        }

        // 모든 assignment의 endTime 추가
        assignments.forEach(a -> {
            if (a.getEndTime() != null) {
                rangeSet.add(a.getEndTime());
            }
        });

        return new ArrayList<>(rangeSet);
    }
}
