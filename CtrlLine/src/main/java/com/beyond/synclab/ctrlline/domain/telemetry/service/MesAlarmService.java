package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Alarms;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesAlarmService {

    private final AlarmRepository alarmRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public void saveAlarmTelemetry(AlarmTelemetryPayload payload) {
        if (payload == null) {
            log.warn("알람 페이로드가 비어있어 저장하지 않습니다.");
            return;
        }
        if (!StringUtils.hasText(payload.equipmentCode())) {
            log.warn("설비 코드가 없어 알람을 저장하지 않습니다. payload={}", payload);
            return;
        }
        if (!StringUtils.hasText(payload.alarmName())) {
            log.warn("알람명이 없어 알람을 저장하지 않습니다. payload={}", payload);
            return;
        }

        Equipments equipment = equipmentRepository.findByEquipmentCode(payload.equipmentCode()).orElse(null);
        if (equipment == null) {
            log.warn("설비 정보를 찾을 수 없어 알람을 저장하지 않습니다. equipmentCode={}", payload.equipmentCode());
            return;
        }

        Alarms alarm = Alarms.builder()
                .equipment(equipment)
                .userId(parseUserId(payload.user()))
                .alarmCode(deriveAlarmCode(payload))
                .alarmName(payload.alarmName())
                .alarmType(resolveAlarmType(payload))
                .alarmLevel(trimToNull(payload.alarmLevel()))
                .occurredAt(payload.occurredAt())
                .clearedAt(payload.clearedAt())
                .alarmCause(trimToNull(payload.alarmCause()))
                .build();

        alarmRepository.save(alarm);
    }

    private String deriveAlarmCode(AlarmTelemetryPayload payload) {
        if (StringUtils.hasText(payload.alarmCode())) {
            return trimTo32(payload.alarmCode());
        }
        if (StringUtils.hasText(payload.alarmType())) {
            return trimTo32(payload.alarmType());
        }
        if (StringUtils.hasText(payload.alarmName())) {
            return trimTo32(payload.alarmName());
        }
        return trimTo32(payload.equipmentCode());
    }

    private String resolveAlarmType(AlarmTelemetryPayload payload) {
        if (StringUtils.hasText(payload.alarmType())) {
            return trimTo32(payload.alarmType());
        }
        if (StringUtils.hasText(payload.alarmLevel())) {
            return trimTo32(payload.alarmLevel());
        }
        return "UNKNOWN";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimTo32(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.length() <= 32 ? trimmed : trimmed.substring(0, 32);
    }

    private Long parseUserId(String user) {
        String trimmed = trimToNull(user);
        if (trimmed == null) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            log.warn("알람 처리자 ID를 숫자로 변환할 수 없습니다. user={}", user, ex);
            return null;
        }
    }
}
