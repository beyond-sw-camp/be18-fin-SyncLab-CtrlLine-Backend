package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Alarms;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.AlarmRepository;
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
class MesAlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Captor
    private ArgumentCaptor<Alarms> alarmsCaptor;

    private MesAlarmService mesAlarmService;

    @BeforeEach
    void setUp() {
        mesAlarmService = new MesAlarmService(alarmRepository, equipmentRepository);
    }

    @Test
    void saveAlarmTelemetry_persistsAlarmWhenEquipmentFound() {
        AlarmTelemetryPayload payload = AlarmTelemetryPayload.builder()
                .equipmentCode("F1-CL1-EU001")
                .alarmCode("TC01")
                .alarmType("2")
                .alarmName("슬러리 공급 부족")
                .alarmLevel("WARNING")
                .occurredAt(LocalDateTime.of(2025, 11, 17, 11, 33, 44))
                .user("42")
                .build();

        when(equipmentRepository.findByEquipmentCode("F1-CL1-EU001"))
                .thenReturn(Optional.of(sampleEquipment(1L)));

        mesAlarmService.saveAlarmTelemetry(payload);

        verify(alarmRepository).save(alarmsCaptor.capture());
        Alarms saved = alarmsCaptor.getValue();
        assertThat(saved.getEquipment().getEquipmentCode()).isEqualTo("F1-CL1-EU001");
        assertThat(saved.getAlarmName()).isEqualTo("슬러리 공급 부족");
        assertThat(saved.getAlarmType()).isEqualTo("2");
        assertThat(saved.getAlarmCode()).isEqualTo("TC01");
        assertThat(saved.getAlarmLevel()).isEqualTo("WARNING");
        assertThat(saved.getOccurredAt()).isEqualTo(LocalDateTime.of(2025, 11, 17, 11, 33, 44));
        assertThat(saved.getUserId()).isEqualTo(42L);
    }

    @Test
    void saveAlarmTelemetry_skipsWhenEquipmentMissing() {
        AlarmTelemetryPayload payload = AlarmTelemetryPayload.builder()
                .equipmentCode("UNKNOWN")
                .alarmName("알람")
                .build();

        when(equipmentRepository.findByEquipmentCode("UNKNOWN")).thenReturn(Optional.empty());

        mesAlarmService.saveAlarmTelemetry(payload);

        verifyNoInteractions(alarmRepository);
    }

    private Equipments sampleEquipment(Long id) {
        return Equipments.builder()
                .id(id)
                .equipmentCode("F1-CL1-EU001")
                .equipmentName("장비")
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
