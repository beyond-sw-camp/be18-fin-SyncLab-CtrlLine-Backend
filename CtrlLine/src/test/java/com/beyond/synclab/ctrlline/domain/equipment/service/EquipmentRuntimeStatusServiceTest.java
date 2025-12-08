package com.beyond.synclab.ctrlline.domain.equipment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRuntimeStatusLevel;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.EquipmentStatusTelemetryPayload;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class EquipmentRuntimeStatusServiceTest {

    private EquipmentRuntimeStatusService service;

    @BeforeEach
    void setUp() {
        EquipmentStatusStreamService streamService = mock(EquipmentStatusStreamService.class);
        service = new EquipmentRuntimeStatusService(
                Clock.fixed(Instant.parse("2025-05-05T00:00:00Z"), ZoneOffset.UTC),
                streamService
        );
    }

    @Test
    void updateStatus_setsRunningLevelWhenExecuteState() {
        EquipmentStatusTelemetryPayload payload = new EquipmentStatusTelemetryPayload(
                "EQP-100",
                "EXECUTE",
                null,
                false,
                null
        );

        service.updateStatus(payload);

        assertThat(service.getLevelOrDefault("EQP-100")).isEqualTo(EquipmentRuntimeStatusLevel.RUNNING);
    }

    @Test
    void updateStatus_escalatesToSevereWhenCriticalAlarmActive() {
        EquipmentStatusTelemetryPayload payload = new EquipmentStatusTelemetryPayload(
                "EQP-101",
                "EXECUTE",
                "CRITICAL",
                true,
                null
        );

        service.updateStatus(payload);

        assertThat(service.getLevelOrDefault("EQP-101")).isEqualTo(EquipmentRuntimeStatusLevel.HIGH_WARNING);
    }

    @Test
    void updateStatus_marksHoldWithWarningAlarmAsLightWarning() {
        EquipmentStatusTelemetryPayload payload = new EquipmentStatusTelemetryPayload(
                "EQP-102",
                "HOLD",
                "WARNING",
                true,
                null
        );

        service.updateStatus(payload);

        assertThat(service.getLevelOrDefault("EQP-102")).isEqualTo(EquipmentRuntimeStatusLevel.LOW_WARNING);
    }
}
