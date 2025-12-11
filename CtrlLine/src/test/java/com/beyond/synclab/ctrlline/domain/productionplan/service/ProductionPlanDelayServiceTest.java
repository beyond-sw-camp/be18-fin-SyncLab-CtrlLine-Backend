package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.mockito.Mockito.lenient;
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
    @DisplayName("실제 종료시간 기반 뒤 계획 Shift 테스트 - completedPlan 제외 후 미래 계획만 이동")
    void applyRealPerformanceDelay_shiftOnlyFuturePlans() {

        // ---------------------------
        // GIVEN
        // ---------------------------

        LocalDateTime scheduledEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime actualEnd    = scheduledEnd.plusMinutes(20); // 10:20

        // completedPlan
        Lines line = Lines.builder().id(1L).build();
        ItemsLines itemLine = ItemsLines.builder()
            .id(10L)
            .line(line)
            .lineId(line.getId())
            .build();

        ProductionPlans completedPlan = mock(ProductionPlans.class);
        lenient().when(completedPlan.getId()).thenReturn(100L);
        lenient().when(completedPlan.getEndTime()).thenReturn(scheduledEnd);
        lenient().when(completedPlan.getItemLine()).thenReturn(itemLine);

        // -------- 미래 계획들 --------
        ProductionPlans planB =  mock(ProductionPlans.class);
        lenient().when(planB.getId()).thenReturn(200L);
        lenient().when(planB.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));
        lenient().when(planB.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));

        ProductionPlans planC = mock(ProductionPlans.class);
        lenient().when(planC.getId()).thenReturn(300L);
        lenient().when(planC.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));
        lenient().when(planC.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 14, 0));

        List<ProductionPlans> futurePlans = List.of(planB, planC);

        // repository mocking
        when(productionPlanRepository.findAllByLineIdAndStartTimeAfterOrderByStartTimeAsc(
            1L,
            scheduledEnd
        )).thenReturn(futurePlans);

        // ---------------------------
        // WHEN
        // ---------------------------
        delayService.applyRealPerformanceDelay(completedPlan, actualEnd);

        // ---------------------------
        // THEN
        // ---------------------------

        // PlanB duration = 2h
        verify(planB).updateStartTime(actualEnd);
        verify(planB).updateEndTime(actualEnd.plus(Duration.ofHours(2)));

        // PlanC = PlanB.newEnd + 2h
        LocalDateTime expectedCStart = actualEnd.plus(Duration.ofHours(2));
        LocalDateTime expectedCEnd   = expectedCStart.plus(Duration.ofHours(2));

        verify(planC).updateStartTime(expectedCStart);
        verify(planC).updateEndTime(expectedCEnd);

        // saveAll 호출
        verify(productionPlanRepository).saveAll(futurePlans);
    }
}
