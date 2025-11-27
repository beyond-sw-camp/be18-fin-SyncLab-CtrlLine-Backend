package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.service.PlanDefectiveXrefService;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesDefectiveService {
    private static final String UNKNOWN_VALUE = "UNKNOWN";

    private final DefectiveRepository defectiveRepository;
    private final EquipmentRepository equipmentRepository;
    private final PlanDefectiveXrefService planDefectiveXrefService;
    private final OrderSerialArchiveService orderSerialArchiveService;
    private final Map<Long, BigDecimal> lastProducedQuantityByEquipment = new ConcurrentHashMap<>();
    private final Map<Long, BigDecimal> lastDefectiveQuantityByEquipment = new ConcurrentHashMap<>();

    @Transactional
    public void saveNgTelemetry(DefectiveTelemetryPayload payload) {
        saveNgTelemetry(payload, true);
    }

    public void saveNgTelemetry(DefectiveTelemetryPayload payload, boolean linkPlanXref) {
        if (!isValidNgPayload(payload)) {
            log.warn("NG telemetry payload가 올바르지 않아 저장하지 않습니다. payload={}", payload);
            return;
        }
        Integer ngType = parseNgType(payload.defectiveType());
        if (ngType == null || ngType < 1 || ngType > 4) {
            log.debug("NG 타입이 유효하지 않아 저장하지 않습니다. type={}", payload.defectiveType());
            return;
        }
        Long equipmentId = resolveEquipmentId(payload);
        if (equipmentId == null) {
            log.warn("설비 정보를 찾을 수 없어 NG 데이터를 저장하지 않습니다. equipmentCode={}, equipmentId={}",
                    payload.equipmentCode(), payload.equipmentId());
            return;
        }
        String defectiveCode = buildDefectiveCode(payload);
        Defectives defective = defectiveRepository.findByEquipmentIdAndDefectiveCode(equipmentId, defectiveCode)
                .orElseGet(() -> defectiveRepository.save(Defectives.builder()
                        .equipmentId(equipmentId)
                        .defectiveCode(defectiveCode)
                        .defectiveName(payload.defectiveName())
                        .defectiveType(resolveDefectiveType(payload))
                        .build()));

        if (payload.defectiveQuantity() != null && payload.defectiveQuantity().compareTo(BigDecimal.ZERO) > 0) {
            if (linkPlanXref) {
                planDefectiveXrefService.linkPlanDefective(defective.getId(), payload);
            } else {
                log.debug("linkPlanXref=false 설정으로 xref 업데이트를 건너뜁니다. payload={}", payload);
            }
        } else {
            log.debug("defectiveQuantity가 0 이하이므로 xref에 저장하지 않습니다. payload={}", payload);
        }
    }

    @Transactional
    public void saveOrderSummaryTelemetry(OrderSummaryTelemetryPayload payload) {
        if (payload == null || !StringUtils.hasText(payload.equipmentCode())) {
            log.warn("Order summary payload가 올바르지 않아 저장하지 않습니다. payload={}", payload);
            return;
        }
        Optional<Equipments> equipmentOptional = equipmentRepository.findByEquipmentCode(payload.equipmentCode());
        if (equipmentOptional.isEmpty()) {
            log.warn("설비 정보를 찾을 수 없어 order summary를 저장하지 않습니다. equipmentCode={}", payload.equipmentCode());
            return;
        }
        Equipments equipment = equipmentOptional.get();
        EquipmentSummaryDelta delta = calculateSummaryDelta(equipment.getId(), payload.producedQuantity(), payload.defectiveQuantity());
        equipment.accumulateProduction(delta.producedDelta(), delta.defectiveDelta());
        if (StringUtils.hasText(payload.goodSerialsGzip())) {
            orderSerialArchiveService.archive(payload);
        }
    }

    private EquipmentSummaryDelta calculateSummaryDelta(Long equipmentId, BigDecimal producedQuantity, BigDecimal defectiveQuantity) {
        BigDecimal producedDelta = null;
        BigDecimal defectiveDelta = null;
        if (producedQuantity != null) {
            BigDecimal lastValue = lastProducedQuantityByEquipment.get(equipmentId);
            producedDelta = calculateDelta(lastValue, producedQuantity);
            lastProducedQuantityByEquipment.put(equipmentId, producedQuantity);
        }
        if (defectiveQuantity != null) {
            BigDecimal lastValue = lastDefectiveQuantityByEquipment.get(equipmentId);
            defectiveDelta = calculateDelta(lastValue, defectiveQuantity);
            lastDefectiveQuantityByEquipment.put(equipmentId, defectiveQuantity);
        }
        return new EquipmentSummaryDelta(producedDelta, defectiveDelta);
    }

    private BigDecimal calculateDelta(BigDecimal lastValue, BigDecimal newValue) {
        if (newValue == null) {
            return null;
        }
        if (lastValue == null) {
            return newValue;
        }
        if (newValue.compareTo(lastValue) >= 0) {
            return newValue.subtract(lastValue);
        }
        return newValue;
    }

    private record EquipmentSummaryDelta(BigDecimal producedDelta, BigDecimal defectiveDelta) {}

    private boolean isValidNgPayload(DefectiveTelemetryPayload payload) {
        return payload != null
                && payload.defectiveQuantity() != null
                && StringUtils.hasText(payload.defectiveCode())
                && StringUtils.hasText(payload.defectiveName());
    }

    private String buildDefectiveCode(DefectiveTelemetryPayload payload) {
        String equipmentCode = payload.equipmentCode();
        if (!StringUtils.hasText(equipmentCode) && payload.equipmentId() != null) {
            equipmentCode = payload.equipmentId().toString();
        }
        String defectiveCode = payload.defectiveCode();
        String left = StringUtils.hasText(equipmentCode) ? equipmentCode : UNKNOWN_VALUE;
        String right = StringUtils.hasText(defectiveCode) ? defectiveCode : UNKNOWN_VALUE;
        return left + "-" + right;
    }

    private String resolveDefectiveType(DefectiveTelemetryPayload payload) {
        if (StringUtils.hasText(payload.defectiveCode())) {
            return payload.defectiveCode();
        }
        return UNKNOWN_VALUE;
    }

    private Long resolveEquipmentId(DefectiveTelemetryPayload payload) {
        if (payload.equipmentId() != null) {
            return payload.equipmentId();
        }
        if (!StringUtils.hasText(payload.equipmentCode())) {
            return null;
        }
        Optional<Equipments> equipment = equipmentRepository.findByEquipmentCode(payload.equipmentCode());
        return equipment.map(Equipments::getId).orElse(null);
    }

    private Integer parseNgType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        try {
            return Integer.parseInt(type.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
