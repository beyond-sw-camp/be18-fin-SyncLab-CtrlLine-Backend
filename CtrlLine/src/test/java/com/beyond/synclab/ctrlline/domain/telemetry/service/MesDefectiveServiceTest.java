package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MesDefectiveServiceTest {

    @Mock
    private DefectiveRepository defectiveRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Captor
    private ArgumentCaptor<Defectives> defectiveCaptor;

    private MesDefectiveService mesDefectiveService;

    @BeforeEach
    void setUp() {
        mesDefectiveService = new MesDefectiveService(defectiveRepository, equipmentRepository);
    }

    @Test
    void saveNgTelemetry_persistsRecord() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentId(5L)
                .defectiveCode("DF-01")
                .defectiveName("Scratch")
                .defectiveQuantity(BigDecimal.valueOf(4))
                .producedQuantity(BigDecimal.valueOf(10))
                .status("NG")
                .defectiveType("ORDER_NG")
                .build();

        Equipments equipment = sampleEquipment(5L);
        when(equipmentRepository.findById(5L)).thenReturn(Optional.of(equipment));

        mesDefectiveService.saveNgTelemetry(payload);

        verify(defectiveRepository).save(defectiveCaptor.capture());
        Defectives saved = defectiveCaptor.getValue();
        assertThat(saved.getDefectiveCode()).isEqualTo("DF-01");
        assertThat(saved.getDefectiveQty()).isEqualByComparingTo("4");
        assertThat(saved.getDefectiveType()).isEqualTo("ORDER_NG");
        assertThat(equipment.getTotalCount()).isEqualByComparingTo("0");
        assertThat(equipment.getDefectiveCount()).isEqualByComparingTo("0");
    }

    @Test
    void saveNgTelemetry_skipsWhenEquipmentMissing() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentId(999L)
                .defectiveCode("DF-00")
                .defectiveName("Unknown")
                .defectiveQuantity(BigDecimal.TEN)
                .status("NG")
                .defectiveType("ORDER_NG")
                .build();

        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        mesDefectiveService.saveNgTelemetry(payload);

        verifyNoInteractions(defectiveRepository);
    }

    @Test
    void saveOrderSummaryTelemetry_updatesEquipmentCounts() {
        OrderSummaryTelemetryPayload payload = OrderSummaryTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .producedQuantity(BigDecimal.valueOf(40))
                .defectiveQuantity(BigDecimal.valueOf(8))
                .build();

        Equipments equipment = sampleEquipment(5L);
        when(equipmentRepository.findByEquipmentCode("EQP-5")).thenReturn(Optional.of(equipment));

        mesDefectiveService.saveOrderSummaryTelemetry(payload);

        assertThat(equipment.getTotalCount()).isEqualByComparingTo("40");
        assertThat(equipment.getDefectiveCount()).isEqualByComparingTo("8");
        verifyNoInteractions(defectiveRepository);
    }

    @Test
    void saveOrderSummaryTelemetry_accumulatesOnIncrease() {
        OrderSummaryTelemetryPayload firstPayload = OrderSummaryTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .producedQuantity(BigDecimal.valueOf(10))
                .defectiveQuantity(BigDecimal.ZERO)
                .build();
        OrderSummaryTelemetryPayload secondPayload = OrderSummaryTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .producedQuantity(BigDecimal.valueOf(15))
                .defectiveQuantity(BigDecimal.valueOf(2))
                .build();

        Equipments equipment = sampleEquipment(5L);
        when(equipmentRepository.findByEquipmentCode("EQP-5")).thenReturn(Optional.of(equipment));

        mesDefectiveService.saveOrderSummaryTelemetry(firstPayload);
        mesDefectiveService.saveOrderSummaryTelemetry(secondPayload);

        assertThat(equipment.getTotalCount()).isEqualByComparingTo("15"); // 10 + (15-10)
        assertThat(equipment.getDefectiveCount()).isEqualByComparingTo("2");
    }

    @Test
    void saveOrderSummaryTelemetry_accumulatesEntireValueWhenResetDetected() {
        OrderSummaryTelemetryPayload firstPayload = OrderSummaryTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .producedQuantity(BigDecimal.valueOf(30))
                .defectiveQuantity(BigDecimal.valueOf(3))
                .build();
        OrderSummaryTelemetryPayload resetPayload = OrderSummaryTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .producedQuantity(BigDecimal.valueOf(5))
                .defectiveQuantity(BigDecimal.ONE)
                .build();

        Equipments equipment = sampleEquipment(5L);
        when(equipmentRepository.findByEquipmentCode("EQP-5")).thenReturn(Optional.of(equipment));

        mesDefectiveService.saveOrderSummaryTelemetry(firstPayload);
        mesDefectiveService.saveOrderSummaryTelemetry(resetPayload);

        assertThat(equipment.getTotalCount()).isEqualByComparingTo("35"); // 30 + 5 (reset)
        assertThat(equipment.getDefectiveCount()).isEqualByComparingTo("4");
    }

    private Equipments sampleEquipment(Long id) {
        return Equipments.builder()
                .id(id)
                .lineId(1L)
                .equipmentStatusId(1L)
                .equipmentCode("EQP-" + id)
                .equipmentName("Eqp " + id)
                .equipmentType("TYPE")
                .operatingTime(LocalDateTime.now())
                .maintenanceHistory(LocalDateTime.now())
                .equipmentPpm(BigDecimal.ZERO)
                .totalCount(BigDecimal.ZERO)
                .defectiveCount(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }
}
