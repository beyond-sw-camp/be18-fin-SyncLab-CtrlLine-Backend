package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DOCUMENT_PREFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final DefectiveRepository defectiveRepository;
    private final EquipmentRepository equipmentRepository;
    private final Clock clock;

    @Transactional
    public void saveNgTelemetry(DefectiveTelemetryPayload payload) {
        if (payload == null) {
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

        Defectives defective = Defectives.builder()
                .equipment(equipment)
                .documentNo(nextDocumentNo())
                .defectiveCode(payload.defectiveCode())
                .defectiveName(payload.defectiveName())
                .defectiveQty(payload.defectiveQuantity())
                .defectiveStatus(payload.status())
                .build();
        defectiveRepository.save(defective);
    }

    private boolean isPayloadValid(DefectiveTelemetryPayload payload) {
        return payload.defectiveQuantity() != null
                && StringUtils.hasText(payload.defectiveCode())
                && StringUtils.hasText(payload.defectiveName())
                && StringUtils.hasText(payload.status());
    }

    private Equipments findEquipment(DefectiveTelemetryPayload payload) {
        if (payload.equipmentId() != null) {
            Optional<Equipments> equipment = equipmentRepository.findById(payload.equipmentId());
            if (equipment.isPresent()) {
                return equipment.get();
            }
        }
        if (StringUtils.hasText(payload.equipmentCode())) {
            return equipmentRepository.findByEquipmentCode(payload.equipmentCode()).orElse(null);
        }
        return null;
    }

    private String nextDocumentNo() {
        String prefix = YearMonth.now(clock).format(DOCUMENT_PREFIX_FORMATTER);
        int nextSequence = defectiveRepository
                .findTopByDocumentNoStartingWithOrderByDocumentNoDesc(prefix)
                .map(Defectives::getDocumentNo)
                .map(documentNo -> extractSequence(prefix, documentNo) + 1)
                .orElse(1);
        return prefix + "-" + String.format("%04d", nextSequence);
    }

    private int extractSequence(String prefix, String documentNo) {
        if (!StringUtils.hasText(documentNo) || documentNo.length() <= prefix.length()) {
            return 0;
        }
        String suffix = documentNo.substring(prefix.length()).replaceFirst("^[^0-9]*", "");
        if (!StringUtils.hasText(suffix)) {
            return 0;
        }
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            log.warn("불량 전표번호에서 일련번호를 파싱할 수 없습니다. documentNo={}", documentNo, ex);
            return 0;
        }
    }
}
