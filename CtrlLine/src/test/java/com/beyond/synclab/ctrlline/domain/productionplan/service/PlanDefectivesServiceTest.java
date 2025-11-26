package com.beyond.synclab.ctrlline.domain.productionplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanDefectivesServiceTest {

    @Mock
    private PlanDefectiveRepository planDefectiveRepository;

    private Clock fixedClock;

    private PlanDefectiveService planDefectiveService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-10-30T02:45:12Z"), ZoneOffset.UTC);
        planDefectiveService = new PlanDefectiveService(planDefectiveRepository, fixedClock);
    }

    @Test
    void createPlanDefective_generatesDocumentNumberWithIncrement() {
        ProductionPlans plan = samplePlan(10L);
        when(planDefectiveRepository.existsByProductionPlanId(10L)).thenReturn(false);
        when(planDefectiveRepository.findTopByDefectiveDocumentNoStartingWithOrderByIdDesc("2025/10/30-"))
                .thenReturn(Optional.of(PlanDefectives.builder()
                        .id(5L)
                        .productionPlanId(9L)
                        .defectiveDocumentNo("2025/10/30-3")
                        .build()));
        when(planDefectiveRepository.save(any(PlanDefectives.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, PlanDefectives.class));

        planDefectiveService.createPlanDefective(plan);

        ArgumentCaptor<PlanDefectives> captor = ArgumentCaptor.forClass(PlanDefectives.class);
        verify(planDefectiveRepository).save(captor.capture());
        PlanDefectives saved = captor.getValue();
        assertThat(saved.getProductionPlanId()).isEqualTo(10L);
        assertThat(saved.getDefectiveDocumentNo()).isEqualTo("2025/10/30-4");
    }

    @Test
    void createPlanDefective_skipsWhenAlreadyExists() {
        ProductionPlans plan = samplePlan(15L);
        when(planDefectiveRepository.existsByProductionPlanId(15L)).thenReturn(true);

        planDefectiveService.createPlanDefective(plan);

        verify(planDefectiveRepository, never()).save(any());
    }

    @Test
    void createPlanDefective_skipsWhenPlanMissing() {
        planDefectiveService.createPlanDefective(null);
        verify(planDefectiveRepository, never()).save(any());
    }

    private ProductionPlans samplePlan(Long id) {
        LocalDate today = LocalDate.parse("2025-10-30");
        LocalDateTime now = LocalDateTime.of(2025, 10, 30, 12, 0);
        return ProductionPlans.builder()
                .id(id)
                .documentNo("PLAN-" + id)
                .status(PlanStatus.CONFIRMED)
                .dueDate(today)
                .plannedQty(BigDecimal.TEN)
                .startTime(now)
                .endTime(now.plusHours(8))
                .build();
    }
}
