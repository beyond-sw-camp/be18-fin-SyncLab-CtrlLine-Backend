package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.time.LocalDateTime;

public record EquipmentStatusTelemetryPayload(
        String equipmentCode,
        String state,
        String alarmLevel,
        boolean alarmActive,
        LocalDateTime eventTime
) {}
