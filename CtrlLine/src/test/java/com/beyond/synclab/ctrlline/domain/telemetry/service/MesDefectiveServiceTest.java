package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2024-11-15T00:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        mesDefectiveService = new MesDefectiveService(defectiveRepository, equipmentRepository, fixedClock);
    }

    @Test
    void saveNgTelemetry_persistsRecordWithGeneratedDocumentNo() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentId(5L)
                .defectiveCode("DF-01")
                .defectiveName("Scratch")
                .defectiveQuantity(BigDecimal.valueOf(4))
                .status("NG")
                .build();

        when(equipmentRepository.findById(5L)).thenReturn(Optional.of(sampleEquipment(5L)));
        when(defectiveRepository.findTopByDocumentNoStartingWithOrderByDocumentNoDesc("202411"))
                .thenReturn(Optional.empty());

        mesDefectiveService.saveNgTelemetry(payload);

        verify(defectiveRepository).save(defectiveCaptor.capture());
        Defectives saved = defectiveCaptor.getValue();
        assertThat(saved.getDocumentNo()).isEqualTo("202411-0001");
        assertThat(saved.getDefectiveCode()).isEqualTo("DF-01");
        assertThat(saved.getDefectiveQty()).isEqualByComparingTo("4");
    }

    @Test
    void saveNgTelemetry_usesExistingMonthlySequence() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentId(8L)
                .defectiveCode("DF-09")
                .defectiveName("Bent")
                .defectiveQuantity(BigDecimal.ONE)
                .status("NG")
                .build();

        when(equipmentRepository.findById(8L)).thenReturn(Optional.of(sampleEquipment(8L)));
        when(defectiveRepository.findTopByDocumentNoStartingWithOrderByDocumentNoDesc("202411"))
                .thenReturn(Optional.of(Defectives.builder().documentNo("202411-0004").build()));

        mesDefectiveService.saveNgTelemetry(payload);

        verify(defectiveRepository).save(defectiveCaptor.capture());
        assertThat(defectiveCaptor.getValue().getDocumentNo()).isEqualTo("202411-0005");
    }

    @Test
    void saveNgTelemetry_skipsWhenEquipmentMissing() {
        DefectiveTelemetryPayload payload = DefectiveTelemetryPayload.builder()
                .equipmentId(999L)
                .defectiveCode("DF-00")
                .defectiveName("Unknown")
                .defectiveQuantity(BigDecimal.TEN)
                .status("NG")
                .build();

        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        mesDefectiveService.saveNgTelemetry(payload);

        verifyNoInteractions(defectiveRepository);
    }

    private Equipments sampleEquipment(Long id) {
        return Equipments.builder()
                .id(id)
                // 이거 안 써서 지움.
                // lineId(1L)
                // .equipmentStatusId(1L)
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
