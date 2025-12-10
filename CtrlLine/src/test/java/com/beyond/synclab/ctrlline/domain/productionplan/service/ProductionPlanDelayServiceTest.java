package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.mockito.Mockito.*;

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
    @DisplayName("실제 종료시간 기반 전체 스케줄 재정렬 - 뒤 계획 밀기 테스트")
    void applyRealPerformanceDelay_shiftPlansAfterCompleted() {

        // ---------------------------
        // GIVEN
        // ---------------------------

        LocalDateTime scheduledEnd = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime actualEnd    = scheduledEnd.plusMinutes(20); // 10:20

        // completedPlan 설정
        Lines line = Lines.builder().id(1L).build();
        ItemsLines itemLine = ItemsLines.builder()
            .id(10L)
            .lineId(line.getId())
            .line(line)
            .build();

        ProductionPlans completedPlan = mock(ProductionPlans.class);
        when(completedPlan.getId()).thenReturn(100L);
        when(completedPlan.getEndTime()).thenReturn(scheduledEnd);
        when(completedPlan.getItemLine()).thenReturn(itemLine);

        // 다른 계획들 준비
        ProductionPlans planB = mock(ProductionPlans.class);
        when(planB.getId()).thenReturn(200L);
        when(planB.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));
        when(planB.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));

        ProductionPlans planC = mock(ProductionPlans.class);
        when(planC.getId()).thenReturn(300L);
        when(planC.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(planC.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 14, 0));

        // repository mock
        when(productionPlanRepository.findAllByLineIdAndStatusesOrderByStartTimeAsc(
            eq(1L),
            any()
        )).thenReturn(List.of(completedPlan, planB, planC));

        // ---------------------------
        // WHEN
        // ---------------------------
        delayService.applyRealPerformanceDelay(completedPlan, actualEnd);

        // ---------------------------
        // THEN
        // ---------------------------

        // PlanB의 duration = 2시간
        verify(planB).updateStartTime(actualEnd);
        verify(planB).updateEndTime(actualEnd.plus(Duration.ofHours(2)));

        // PlanC는 B의 newEnd 에 붙음
        LocalDateTime expectedCStart = actualEnd.plus(Duration.ofHours(2));
        LocalDateTime expectedCEnd = expectedCStart.plus(Duration.ofHours(2));

        verify(planC).updateStartTime(expectedCStart);
        verify(planC).updateEndTime(expectedCEnd);

        // completedPlan은 이동되지 않음
        verify(completedPlan, never()).updateStartTime(any());
        verify(completedPlan, never()).updateEndTime(any());

        // saveAll 호출 확인
        verify(productionPlanRepository).saveAll(anyList());
    }
}
