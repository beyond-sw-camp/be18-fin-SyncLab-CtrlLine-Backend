package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AlarmTelemetryPayload(
        String equipmentCode,
        String alarmType,
        String alarmName,
        String alarmLevel,
        LocalDateTime occurredAt,
        LocalDateTime clearedAt,
        String user,
        String alarmCause
) {
}
