package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionPlanDelayServiceTest {

    @Mock
    ProductionPlanRepository productionPlanRepository;

    @InjectMocks
    ProductionPlanDelayService delayService;

    @Test
    @DisplayName("RUNNING → COMPLETED 지연 기반 뒤 계획 Shift 테스트")
    void applyRealPerformanceDelay_success() {
        // given
        LocalDateTime scheduledEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime actualEnd = scheduledEnd.plusMinutes(20); // 20분 지연

        // completedPlan 설정
        Lines line = Lines.builder().id(1L).build();

        ItemsLines itemLine = ItemsLines.builder().id(10L).line(line).lineId(line.getId()).build();

        ProductionPlans completedPlan = mock(ProductionPlans.class);
        when(completedPlan.getEndTime()).thenReturn(scheduledEnd);
        when(completedPlan.getItemLine()).thenReturn(itemLine);

        // future plans 준비
        ProductionPlans planB = mock(ProductionPlans.class);
        when(planB.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));
        when(planB.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));

        ProductionPlans planC = mock(ProductionPlans.class);
        when(planC.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(planC.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 14, 0));

        List<ProductionPlans> futurePlans = List.of(planB, planC);

        when(productionPlanRepository.findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
            1L, scheduledEnd)
        ).thenReturn(futurePlans);

        // when
        delayService.applyRealPerformanceDelay(completedPlan, actualEnd);

        // then
        // Plan B duration: 2h
        verify(planB).updateStartTime(actualEnd);
        verify(planB).updateEndTime(actualEnd.plus(Duration.ofHours(2)));

        // Plan C should start at PlanB.newEnd
        LocalDateTime expectedPlanCStart = actualEnd.plus(Duration.ofHours(2));
        LocalDateTime expectedPlanCEnd = expectedPlanCStart.plus(Duration.ofHours(2));

        verify(planC).updateStartTime(expectedPlanCStart);
        verify(planC).updateEndTime(expectedPlanCEnd);

        verify(productionPlanRepository).saveAll(futurePlans);
    }

}