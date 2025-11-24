package com.beyond.synclab.ctrlline.domain.production.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.service.PlanDefectiveService;
import com.beyond.synclab.ctrlline.domain.lot.service.LotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private MiloProductionOrderClient miloProductionOrderClient;

    @Mock
    private PlanDefectiveService planDefectiveService;

    @Mock
    private LotService lotService;

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
                planDefectiveService,
                lotService,
                fixedClock
        );
    }

    @Test
    @DisplayName("dispatchOrder는 Milo 클라이언트에 새 명세로 요청을 전달한다")
    void dispatchOrder_withFactoryLineAndPayload() {
        // given
        ProductionOrderCommandRequest commandRequest = new ProductionOrderCommandRequest(
                "START",
                "2025-10-24-1",
                7000,
                "PRD-7782",
                null
        );
        
        MiloProductionOrderResponse response = new MiloProductionOrderResponse(
                "2025-10-24-1",
                "PS-001",
                "PRD-7782",
                7000,
                "2025-10-30T02:45:12Z"
        );

        when(miloProductionOrderClient.dispatchOrder(eq("FC-001"), eq("PS-001"), any(MiloProductionOrderRequest.class)))
                .thenReturn(response);

        // when
        ProductionOrderCommandResponse actual = productionOrderService.dispatchOrder("FC-001", "PS-001", commandRequest);

        // then
        ArgumentCaptor<MiloProductionOrderRequest> captor = ArgumentCaptor.forClass(MiloProductionOrderRequest.class);
        verify(miloProductionOrderClient).dispatchOrder(eq("FC-001"), eq("PS-001"), captor.capture());

        MiloProductionOrderRequest sentRequest = captor.getValue();
        assertThat(sentRequest.action()).isEqualTo("START");
        assertThat(sentRequest.orderNo()).isEqualTo("2025-10-24-1");
        assertThat(sentRequest.targetQty()).isEqualTo(7000);
        assertThat(sentRequest.itemCode()).isEqualTo("PRD-7782");
        assertThat(sentRequest.ppm()).isNull();

        assertThat(actual.documentNo()).isEqualTo("2025-10-24-1");
        assertThat(actual.lineCode()).isEqualTo("PS-001");
    }

    
    @Test
    @DisplayName("시작 시간이 경과한 생산계획은 Milo에 생산지시를 전달하고 RUNNING으로 변경한다")
    void dispatchDuePlans_sendOrderAndMarkRunning() {
        // given
        LocalDateTime now = LocalDateTime.now(fixedClock);
        Factories factory = Factories.builder().id(1L).factoryName("F0001").build();
        Lines line = Lines.builder().id(1L).lineCode("PS-001").factory(factory).build();
        Items item = Items.builder()
                .id(5L)
                .itemCode("PRD-7782")
                .itemName("Cell")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
        ProductionPlans plan = ProductionPlans.builder()
                .documentNo("2025-10-24-1")
                .itemLine(ItemsLines.builder().id(1L).lineId(1L).line(line).itemId(5L).item(item).build())
                .startTime(now.minusMinutes(1))
                .plannedQty(new java.math.BigDecimal("7000"))
                .status(PlanStatus.CONFIRMED)
                .build();


        when(productionPlanRepository.findAllByStatusAndStartTimeLessThanEqual(PlanStatus.CONFIRMED, now))
                .thenReturn(List.of(plan));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(line));
        when(lineRepository.findFactoryCodeByLineId(1L)).thenReturn(Optional.of("FC-001"));

        when(miloProductionOrderClient.dispatchOrder(eq("FC-001"), eq("PS-001"), any(MiloProductionOrderRequest.class)))
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
        verify(miloProductionOrderClient).dispatchOrder(eq("FC-001"), eq("PS-001"), requestCaptor.capture());

        MiloProductionOrderRequest request = requestCaptor.getValue();
        assertThat(request.action()).isEqualTo("START");
        assertThat(request.orderNo()).isEqualTo("2025-10-24-1");
        assertThat(request.targetQty()).isEqualTo(7_000);
        assertThat(request.itemCode()).isEqualTo("PRD-7782");
        assertThat(request.ppm()).isNull();

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.RUNNING);
        verify(planDefectiveService).createPlanDefective(plan);
        verify(lotService).createLot(plan);
        verify(productionPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Milo 전송 실패 시 생산계획을 RETURNED로 표시한다")
    void dispatchDuePlans_onFailureMarksPlanReturned() {
        // given
        LocalDateTime now = LocalDateTime.now(fixedClock);
        Lines line = Lines.builder().id(1L).lineCode("L001").factoryId(10L).build();
        Items item = Items.builder()
                .id(5L)
                .itemCode("PRD-7782")
                .itemName("Cell")
                .itemUnit("EA")
                .itemStatus(ItemStatus.FINISHED_PRODUCT)
                .isActive(true)
                .build();
        ProductionPlans plan = ProductionPlans.builder()
                .documentNo("2025-10-24-1")
                .itemLine(ItemsLines.builder().id(1L).lineId(1L).line(line).itemId(5L).item(item).build())
                .startTime(now.minusMinutes(1))
                .plannedQty(new java.math.BigDecimal("7000"))
                .status(PlanStatus.CONFIRMED)
                .build();

        when(productionPlanRepository.findAllByStatusAndStartTimeLessThanEqual(PlanStatus.CONFIRMED, now))
                .thenReturn(List.of(plan));
        when(lineRepository.findById(1L)).thenReturn(Optional.of(Lines.of(1L, 10L, "PS-001")));
        when(lineRepository.findFactoryCodeByLineId(1L)).thenReturn(Optional.of("FC-001"));

        Mockito.doThrow(new RuntimeException("Milo down"))
               .when(miloProductionOrderClient).dispatchOrder(eq("FC-001"), eq("PS-001"), any(MiloProductionOrderRequest.class));

        // when
        productionOrderService.dispatchDuePlans();

        // then
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.RETURNED);
        verify(productionPlanRepository).save(plan);
        Mockito.verifyNoInteractions(planDefectiveService, lotService);
    }
}
