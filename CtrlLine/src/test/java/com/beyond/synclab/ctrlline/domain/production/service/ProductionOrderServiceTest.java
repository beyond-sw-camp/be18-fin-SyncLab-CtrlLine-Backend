package com.beyond.synclab.ctrlline.domain.production.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.production.client.MiloClientException;
import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import com.beyond.synclab.ctrlline.domain.production.entity.Line;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan.PlanStatus;
import com.beyond.synclab.ctrlline.domain.production.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private MiloProductionOrderClient miloProductionOrderClient;

    private Clock fixedClock;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-10-30T02:45:12Z"), ZoneOffset.UTC);
        productionOrderService = new ProductionOrderService(
                productionPlanRepository,
                lineRepository,
                miloProductionOrderClient,
                fixedClock
        );
    }

    @Test
    @DisplayName("시작 시간이 경과한 생산계획은 Milo에 생산지시를 전달하고 DISPATCHED로 변경한다")
    void dispatchDuePlans_sendOrderAndMarkDispatched() {
        // given
        LocalDateTime now = LocalDateTime.now(fixedClock);
        ProductionPlan plan = ProductionPlan.builder()
                .documentNo("2025-10-24-1")
                .lineId(1L)
                .startAt(now.minusMinutes(1))
                .plannedQty(new java.math.BigDecimal("7000"))
                .status(PlanStatus.CONFIRMED)
                .build();

        when(productionPlanRepository.findAllByStatusAndStartAtLessThanEqual(PlanStatus.CONFIRMED, now))
                .thenReturn(List.of(plan));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(Line.of(1L, "PS-001")));
        when(lineRepository.findItemCodeByLineId(1L)).thenReturn(Optional.of("PRD-7782"));
        when(miloProductionOrderClient.dispatchOrder(eq("PS-001"), any(MiloProductionOrderRequest.class)))
                .thenReturn(new MiloProductionOrderResponse(
                        plan.getDocumentNo(),
                        "PS-001",
                        "PRD-7782",
                        7_000,
                        "2025-10-30T02:45:12Z"
                ));

        // when
        productionOrderService.dispatchDuePlans();

        // then
        ArgumentCaptor<MiloProductionOrderRequest> requestCaptor = ArgumentCaptor.forClass(MiloProductionOrderRequest.class);
        verify(miloProductionOrderClient).dispatchOrder(eq("PS-001"), requestCaptor.capture());

        MiloProductionOrderRequest request = requestCaptor.getValue();
        assertThat(request.itemCode()).isEqualTo("PRD-7782");
        assertThat(request.qty()).isEqualTo(7_000);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.RUNNING);
        verify(productionPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Milo 전송 실패 시 생산계획을 DISPATCH_FAILED로 표시한다")
    void dispatchDuePlans_onFailureMarksPlanFailed() {
        // given
        LocalDateTime now = LocalDateTime.now(fixedClock);
        ProductionPlan plan = ProductionPlan.builder()
                .documentNo("2025-10-24-1")
                .lineId(1L)
                .startAt(now.minusMinutes(1))
                .plannedQty(new java.math.BigDecimal("7000"))
                .status(PlanStatus.CONFIRMED)
                .build();

        when(productionPlanRepository.findAllByStatusAndStartAtLessThanEqual(PlanStatus.CONFIRMED, now))
                .thenReturn(List.of(plan));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(Line.of(1L, "PS-001")));
        when(lineRepository.findItemCodeByLineId(1L)).thenReturn(Optional.of("PRD-7782"));

        Mockito.doThrow(new RuntimeException("Milo down"))
                .when(miloProductionOrderClient).dispatchOrder(eq("PS-001"), any(MiloProductionOrderRequest.class));

        // when
        productionOrderService.dispatchDuePlans();

        // then
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.RETURNED);
        verify(productionPlanRepository).save(plan);
    }
}
