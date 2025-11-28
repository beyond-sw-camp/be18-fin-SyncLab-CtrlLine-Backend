package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.lot.service.LotGeneratorService;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSerialArchiveServiceTest {

    @Mock
    private SerialStorageService serialStorageService;
    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private LotGeneratorService lotGeneratorService;
    @Mock
    private ItemSerialRepository itemSerialRepository;
    @Mock
    private ObjectMapper objectMapper;

    private OrderSerialArchiveService service;

    @BeforeEach
    void setUp() {
        service = new OrderSerialArchiveService(
                serialStorageService,
                productionPlanRepository,
                itemSerialRepository,
                objectMapper,
                lotGeneratorService,
                lotRepository
        );
    }

    @Test
    void archive_compressesPlainSerialListWhenGzipMissing() throws Exception {
        OrderSummaryTelemetryPayload payload = OrderSummaryTelemetryPayload.builder()
                .orderNo("PLAN-1")
                .equipmentCode("EQP-1")
                .producedQuantity(BigDecimal.TEN)
                .goodSerials(List.of("S-001", "S-002"))
                .build();
        ProductionPlans plan = samplePlan(100L, "PLAN-1");
        Lots lot = sampleLot(200L, plan.getId());
        when(productionPlanRepository.findFirstByDocumentNoAndStatusOrderByIdDesc("PLAN-1", PlanStatus.RUNNING))
                .thenReturn(Optional.empty());
        when(productionPlanRepository.findFirstByDocumentNoOrderByIdDesc("PLAN-1"))
                .thenReturn(Optional.of(plan));
        when(lotRepository.findByProductionPlanId(plan.getId())).thenReturn(Optional.of(lot));
        when(itemSerialRepository.findByLotId(lot.getId())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(payload.goodSerials())).thenReturn("[\"S-001\",\"S-002\"]");
        when(serialStorageService.store(eq("PLAN-1"), any())).thenReturn("/tmp/serial.gz");

        service.archive(payload);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(serialStorageService).store(eq("PLAN-1"), bytesCaptor.capture());
        assertThat(bytesCaptor.getValue()).isNotEmpty();
        verify(itemSerialRepository).save(any(ItemSerials.class));
    }

    private ProductionPlans samplePlan(Long id, String documentNo) {
        return ProductionPlans.builder()
                .id(id)
                .documentNo(documentNo)
                .dueDate(LocalDate.now())
                .plannedQty(BigDecimal.ONE)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    private Lots sampleLot(Long lotId, Long planId) {
        return Lots.builder()
                .id(lotId)
                .productionPlanId(planId)
                .itemId(10L)
                .lotNo("LOT-1")
                .build();
    }
}
