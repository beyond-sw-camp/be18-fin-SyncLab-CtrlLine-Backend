package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesDefectiveService {
    private final DefectiveRepository defectiveRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public void saveNgTelemetry(DefectiveTelemetryPayload payload) {
        if (payload == null) {
            log.warn("NG telemetry payload is null. Skipping save operation.");
            return;
        }
        Equipments equipment = findEquipment(payload);
        if (equipment == null) {
            log.warn("설비 정보를 찾을 수 없어 불량 데이터를 저장하지 않습니다. equipmentId={}, equipmentCode={}",
                    payload.equipmentId(), payload.equipmentCode());
            return;
        }
        if (!isPayloadValid(payload)) {
            log.warn("필수 불량 정보가 누락되어 저장하지 않습니다. payload={}", payload);
            return;
        }

        Defectives defective = payload.toEntity(
                Defectives.builder().equipment(equipment)
        );
        defectiveRepository.save(defective);
    }

    private boolean isPayloadValid(DefectiveTelemetryPayload payload) {
        return payload.defectiveQuantity() != null
                && StringUtils.hasText(payload.defectiveCode())
                && StringUtils.hasText(payload.defectiveName())
                && StringUtils.hasText(payload.defectiveType());
    }

    private Equipments findEquipment(DefectiveTelemetryPayload payload) {
        Equipments equipment = findByNumericEquipmentId(payload.equipmentId());
        if (equipment != null) {
            return equipment;
        }
        if (StringUtils.hasText(payload.equipmentCode())) {
            equipment = equipmentRepository.findByEquipmentCode(payload.equipmentCode()).orElse(null);
            if (equipment != null) {
                return equipment;
            }
        }
        String fallbackCode = payload.equipmentId() != null ? payload.equipmentId().toString() : null;
        if (StringUtils.hasText(fallbackCode)) {
            return equipmentRepository.findByEquipmentCode(fallbackCode).orElse(null);
        }
        return null;
    }

    private Equipments findByNumericEquipmentId(Long equipmentId) {
        if (equipmentId == null) {
            return null;
        }
        Optional<Equipments> equipment = equipmentRepository.findById(equipmentId);
        return equipment.orElse(null);
    }

}
