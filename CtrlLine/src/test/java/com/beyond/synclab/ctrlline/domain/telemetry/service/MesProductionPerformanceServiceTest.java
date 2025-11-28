package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.production.service.ProductionOrderService;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.ProductionPerformanceTelemetryPayload;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MesProductionPerformanceServiceTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ProductionPerformanceRepository productionPerformanceRepository;
    @Mock
    private ProductionOrderService productionOrderService;
    @Mock
    private Clock clock;

    @InjectMocks
    private MesProductionPerformanceService mesProductionPerformanceService;

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Instant fixedInstant = LocalDateTime.of(2025, 11, 19, 0, 0).atZone(zoneId).toInstant();

    @BeforeEach
    void setUpClock() {
        lenient().when(clock.instant()).thenReturn(fixedInstant);
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    void saveProductionPerformance_successfullyStoresEntityAndSendsAck() {
        ProductionPlans plan = ProductionPlans.builder()
                .id(9L)
                .documentNo("2025/11/19-1")
                .dueDate(LocalDate.of(2025, 11, 19))
                .plannedQty(BigDecimal.valueOf(40))
                .startTime(LocalDateTime.of(2025, 11, 19, 9, 0))
                .endTime(LocalDateTime.of(2025, 11, 19, 11, 0))
                .build();

        when(productionPlanRepository.findByDocumentNo("2025/11/19-1"))
                .thenReturn(Optional.of(plan));
        when(productionPerformanceRepository.findDocumentNosByPrefix("2025/11/19"))
                .thenReturn(Collections.emptyList());

        ProductionPerformanceTelemetryPayload payload = ProductionPerformanceTelemetryPayload.builder()
                .orderNo("2025/11/19-1")
                .orderProducedQty(new BigDecimal("36"))
                .ngCount(new BigDecimal("4"))
                .executeAt(LocalDateTime.of(2025, 11, 19, 9, 30))
                .waitingAckAt(LocalDateTime.of(2025, 11, 19, 10, 15))
                .build();

        mesProductionPerformanceService.saveProductionPerformance(payload);

        ArgumentCaptor<ProductionPerformances> captor = ArgumentCaptor.forClass(ProductionPerformances.class);
        verify(productionPerformanceRepository).save(captor.capture());
        ProductionPerformances saved = captor.getValue();

        assertThat(saved.getPerformanceDocumentNo()).startsWith("2025/11/19-");
        assertThat(saved.getTotalQty()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(saved.getPerformanceQty()).isEqualByComparingTo(new BigDecimal("36.00"));
        assertThat(saved.getPerformanceDefectiveRate()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(saved.getStartTime()).isEqualTo(payload.executeAt());
        assertThat(saved.getEndTime()).isEqualTo(payload.waitingAckAt());
        assertThat(saved.getProductionPlan()).isSameAs(plan);

        verify(productionOrderService).sendLineAck(plan);
    }

    @Test
    void saveProductionPerformance_ignoresPayloadWithoutOrderNo() {
        ProductionPerformanceTelemetryPayload payload = ProductionPerformanceTelemetryPayload.builder()
                .orderNo(null)
                .orderProducedQty(BigDecimal.TEN)
                .ngCount(BigDecimal.ONE)
                .executeAt(LocalDateTime.now())
                .waitingAckAt(LocalDateTime.now())
                .build();

        mesProductionPerformanceService.saveProductionPerformance(payload);

        verify(productionPlanRepository, never()).findByDocumentNo(any());
        verify(productionPerformanceRepository, never()).save(any());
        verify(productionOrderService, never()).sendLineAck(any());
    }
}
