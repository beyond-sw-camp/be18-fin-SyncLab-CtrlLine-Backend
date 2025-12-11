package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRuntimeStatusLevel;
import com.beyond.synclab.ctrlline.domain.equipment.service.dto.EquipmentRuntimeStatusSnapshot;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.EquipmentStatusTelemetryPayload;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Keeps the latest runtime status reported by Milo per equipment so that DTOs can surface a
 * derived level (정지중/생산중/경고/심각경고) without altering the DB schema.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentRuntimeStatusService {

    private static final Set<String> RUNNING_STATES =
            Set.of("EXECUTE", "STARTING", "COMPLETING");

    private static final Set<String> STOPPED_STATES =
            Set.of("IDLE", "RESETTING", "HOLD", "SUSPEND", "COMPLETE", "WAITING_ACK");

    private static final Set<String> WARNING_STATES =
            Set.of("STOPPING");

    private static final Set<String> SEVERE_ALARM_LEVELS =
            Set.of("CRITICAL", "FATAL", "ERROR", "MAJOR", "FAULT", "EMERGENCY");

    private static final Set<String> LIGHT_ALARM_LEVELS =
            Set.of("WARN", "WARNING", "INFO", "MINOR", "NOTICE");

    private final Clock clock;
    private final EquipmentStatusStreamService statusStreamService;
    private final Map<String, EquipmentRuntimeStatusSnapshot> snapshotByEquipment = new ConcurrentHashMap<>();

    public void updateStatus(EquipmentStatusTelemetryPayload payload) {
        if (payload == null || !StringUtils.hasText(payload.equipmentCode()) || !StringUtils.hasText(payload.state())) {
            return;
        }
        EquipmentRuntimeStatusLevel level = resolveLevel(payload);
        LocalDateTime eventTime = Optional.ofNullable(payload.eventTime())
                .orElse(LocalDateTime.now(clock));

        EquipmentRuntimeStatusSnapshot snapshot = new EquipmentRuntimeStatusSnapshot(
                payload.equipmentCode(),
                payload.state(),
                payload.alarmLevel(),
                payload.alarmActive(),
                level,
                eventTime
        );
        snapshotByEquipment.put(payload.equipmentCode(), snapshot);
        log.debug("Updated runtime status equipmentCode={} state={} alarmLevel={} level={}",
                payload.equipmentCode(), payload.state(), payload.alarmLevel(), level);
        statusStreamService.broadcast(snapshot);
    }

    public EquipmentRuntimeStatusLevel getLevelOrDefault(String equipmentCode) {
        return Optional.ofNullable(snapshotByEquipment.get(equipmentCode))
                .map(EquipmentRuntimeStatusSnapshot::statusLevel)
                .orElse(EquipmentRuntimeStatusLevel.STOPPED);
    }

    public EquipmentRuntimeStatusSnapshot getSnapshot(String equipmentCode) {
        return snapshotByEquipment.get(equipmentCode);
    }

    public void applyAlarm(AlarmTelemetryPayload payload) {
        if (payload == null || !StringUtils.hasText(payload.equipmentCode())) {
            return;
        }

        EquipmentRuntimeStatusSnapshot previous = snapshotByEquipment.get(payload.equipmentCode());
        boolean alarmActive = payload.clearedAt() == null;
        EquipmentRuntimeStatusLevel level = alarmActive
                ? levelFromAlarm(payload.alarmLevel())
                : resolveFromState(previous != null ? previous.state() : null);
        String state = previous != null ? previous.state() : null;
        LocalDateTime timestamp = Optional.ofNullable(alarmActive ? payload.occurredAt() : payload.clearedAt())
                .orElse(LocalDateTime.now(clock));

        EquipmentRuntimeStatusSnapshot snapshot = new EquipmentRuntimeStatusSnapshot(
                payload.equipmentCode(),
                state,
                alarmActive ? payload.alarmLevel() : null,
                alarmActive,
                level,
                timestamp
        );
        snapshotByEquipment.put(payload.equipmentCode(), snapshot);
        log.debug("Updated runtime status from alarm equipmentCode={} alarmLevel={} alarmActive={} level={}",
                payload.equipmentCode(), payload.alarmLevel(), alarmActive, level);
        statusStreamService.broadcast(snapshot);
    }

    private EquipmentRuntimeStatusLevel resolveLevel(EquipmentStatusTelemetryPayload payload) {
        if (payload.alarmActive()) {
            AlarmSeverity severity = resolveAlarmSeverity(payload.alarmLevel());
            return severity == AlarmSeverity.SEVERE
                    ? EquipmentRuntimeStatusLevel.HIGH_WARNING
                    : EquipmentRuntimeStatusLevel.LOW_WARNING;
        }
        return resolveFromState(payload.state());
    }

    private EquipmentRuntimeStatusLevel resolveFromState(String state) {
        if (!StringUtils.hasText(state)) {
            return EquipmentRuntimeStatusLevel.STOPPED;
        }
        String normalized = state.trim().toUpperCase();
        if (RUNNING_STATES.contains(normalized)) {
            return EquipmentRuntimeStatusLevel.RUNNING;
        }
        if (WARNING_STATES.contains(normalized)) {
            return EquipmentRuntimeStatusLevel.LOW_WARNING;
        }
        if (STOPPED_STATES.contains(normalized)) {
            return EquipmentRuntimeStatusLevel.STOPPED;
        }
        // Unknown state defaults to stopped to avoid false alarms.
        return EquipmentRuntimeStatusLevel.STOPPED;
    }

    private AlarmSeverity resolveAlarmSeverity(String alarmLevel) {
        if (!StringUtils.hasText(alarmLevel)) {
            return AlarmSeverity.WARNING;
        }
        String normalized = alarmLevel.trim().toUpperCase();
        if (SEVERE_ALARM_LEVELS.contains(normalized)) {
            return AlarmSeverity.SEVERE;
        }
        if (LIGHT_ALARM_LEVELS.contains(normalized)) {
            return AlarmSeverity.WARNING;
        }
        return AlarmSeverity.WARNING;
    }

    private EquipmentRuntimeStatusLevel levelFromAlarm(String alarmLevel) {
        AlarmSeverity severity = resolveAlarmSeverity(alarmLevel);
        return severity == AlarmSeverity.SEVERE
                ? EquipmentRuntimeStatusLevel.HIGH_WARNING
                : EquipmentRuntimeStatusLevel.LOW_WARNING;
    }

    private enum AlarmSeverity {
        WARNING,
        SEVERE
    }
}
