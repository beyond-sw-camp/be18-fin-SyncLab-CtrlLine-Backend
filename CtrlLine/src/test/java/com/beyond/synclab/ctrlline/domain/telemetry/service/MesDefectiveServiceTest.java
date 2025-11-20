package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.service.PlanDefectiveXrefService;
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
    private EquipmentRepository equipmentRepository;

    @Mock
    private DefectiveRepository defectiveRepository;
    @Mock
    private PlanDefectiveXrefService planDefectiveXrefService;

    @Captor
    private ArgumentCaptor<Defectives> defectiveCaptor;

    private MesDefectiveService mesDefectiveService;

    @BeforeEach
    void setUp() {
        mesDefectiveService = new MesDefectiveService(defectiveRepository, equipmentRepository, planDefectiveXrefService);
    }

    @Test
    void saveNgTelemetry_insertsWhenNameMissing() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentCode("EQP-10")
                .defectiveCode("DF-01")
                .defectiveName("Dent")
                .defectiveQuantity(BigDecimal.ONE)
                .orderNo("PLAN-100")
                .status("NG")
                .defectiveType("TYPE")
                .build();
        when(defectiveRepository.findByDefectiveName("Dent")).thenReturn(Optional.empty());
        when(equipmentRepository.findByEquipmentCode("EQP-10")).thenReturn(Optional.of(sampleEquipment(10L)));

        mesDefectiveService.saveNgTelemetry(payload);

        verify(defectiveRepository).save(defectiveCaptor.capture());
        verify(planDefectiveXrefService).linkPlanDefective(anyLong(), eq(payload));
        Defectives saved = defectiveCaptor.getValue();
        assertThat(saved.getEquipmentId()).isEqualTo(10L);
        assertThat(saved.getDefectiveCode()).isEqualTo("EQP-10-DF-01");
        assertThat(saved.getDefectiveType()).isEqualTo("DF-01");
        assertThat(saved.getDefectiveName()).isEqualTo("Dent");
    }

    @Test
    void saveNgTelemetry_skipsWhenNameAlreadyExists() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentCode("EQP-5")
                .defectiveCode("DF-02")
                .defectiveName("Scratch")
                .defectiveQuantity(BigDecimal.TEN)
                .orderNo("PLAN-200")
                .status("NG")
                .defectiveType("TYPE")
                .build();
        when(defectiveRepository.findByDefectiveName("Scratch")).thenReturn(Optional.of(existingDefective(1L)));
        when(equipmentRepository.findByEquipmentCode("EQP-5")).thenReturn(Optional.of(sampleEquipment(5L)));

        mesDefectiveService.saveNgTelemetry(payload);

        verify(defectiveRepository, never()).save(any());
    }

    @Test
    void saveNgTelemetry_skipsWhenEquipmentNotFound() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentCode("UNKNOWN")
                .defectiveCode("DF-99")
                .defectiveName("Unknown")
                .defectiveQuantity(BigDecimal.ONE)
                .build();

        when(equipmentRepository.findByEquipmentCode("UNKNOWN")).thenReturn(Optional.empty());

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

    private Defectives existingDefective(Long id) {
        return Defectives.builder()
                .id(id)
                .equipmentId(5L)
                .defectiveCode("CODE")
                .defectiveName("Scratch")
                .defectiveType("TYPE")
                .build();
    }

    private Equipments sampleEquipment(Long id) {
        return Equipments.builder()
                .id(id)
                // 이거 안 써서 지움.
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
