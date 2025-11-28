package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXrefs;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveXrefRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanDefectiveXrefService {

    private final ProductionPlanRepository productionPlanRepository;
    private final PlanDefectiveRepository planDefectiveRepository;
    private final PlanDefectiveXrefRepository planDefectiveXrefRepository;
    private final PlanDefectiveLastReportedCache lastReportedCache;

    @Transactional
    public void linkPlanDefective(Long defectiveId, DefectiveTelemetryPayload payload) {
        if (defectiveId == null || payload == null) {
            return;
        }
        Integer ngType = parseNgType(payload.defectiveType());
        if (ngType == null || ngType < 1 || ngType > 4) {
            log.debug("유효하지 않은 NG 타입으로 xref를 생성하지 않습니다. type={}", payload.defectiveType());
            return;
        }
        String orderNo = payload.orderNo();
        BigDecimal defectiveQty = payload.defectiveQuantity();
        if (!StringUtils.hasText(orderNo) || defectiveQty == null) {
            log.debug("order_no 또는 defective_qty가 없어 plan_defective_xref를 생성하지 않습니다. orderNo={}, defectiveQty={}",
                    orderNo, defectiveQty);
            return;
        }
        Optional<ProductionPlans> planOptional = findLatestPlan(orderNo);
        if (planOptional.isEmpty()) {
            log.warn("order_no에 해당하는 plan_defective 정보를 찾을 수 없어 저장하지 않습니다. orderNo={}", orderNo);
            return;
        }
        ProductionPlans plan = planOptional.get();
        if (!PlanStatus.RUNNING.equals(plan.getStatus())) {
            log.debug("RUNNING 상태가 아닌 생산계획의 NG는 저장하지 않습니다. orderNo={}, status={}",
                    orderNo, plan.getStatus());
            return;
        }
        Optional<Long> planDefectiveIdOptional = planDefectiveRepository.findByProductionPlanId(plan.getId())
                .map(PlanDefectives::getId);
        if (planDefectiveIdOptional.isEmpty()) {
            log.warn("plan_defective 정보를 찾을 수 없어 저장하지 않습니다. orderNo={}", orderNo);
            return;
        }
        Long planDefectiveId = planDefectiveIdOptional.get();
        BigDecimal reportedQty = sanitize(defectiveQty);
        String equipmentKey = resolveEquipmentKey(payload);
        PlanDefectiveXrefs xref = planDefectiveXrefRepository
                .findByPlanDefectiveIdAndDefectiveId(planDefectiveId, defectiveId)
                .map(existing -> {
                    BigDecimal updatedQty = calculateUpdatedQty(existing.getDefectiveQty(), reportedQty,
                            lastReportedCache.get(planDefectiveId, defectiveId, equipmentKey).orElse(null));
                    existing.updateDefectiveQty(updatedQty);
                    return existing;
                })
                .orElseGet(() -> PlanDefectiveXrefs.builder()
                        .planDefectiveId(planDefectiveId)
                        .defectiveId(defectiveId)
                        .defectiveQty(reportedQty)
                        .build());
        planDefectiveXrefRepository.save(xref);
        lastReportedCache.save(planDefectiveId, defectiveId, equipmentKey, reportedQty);
        log.info("plan_defective_xref 저장 완료 planDefectiveId={}, defectiveId={}, qty={}",
                planDefectiveId, defectiveId, defectiveQty);
    }

    private Optional<ProductionPlans> findLatestPlan(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return Optional.empty();
        }
        Optional<ProductionPlans> runningPlan =
                productionPlanRepository.findFirstByDocumentNoAndStatusOrderByIdDesc(orderNo, PlanStatus.RUNNING);
        if (runningPlan.isPresent()) {
            return runningPlan;
        }
        return productionPlanRepository.findFirstByDocumentNoOrderByIdDesc(orderNo);
    }

    private BigDecimal sanitize(BigDecimal qty) {
        if (qty == null) {
            return BigDecimal.ZERO;
        }
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return qty;
    }

    private BigDecimal calculateUpdatedQty(BigDecimal currentTotal, BigDecimal reportedQty, BigDecimal lastReported) {
        BigDecimal current = Optional.ofNullable(currentTotal).orElse(BigDecimal.ZERO);
        BigDecimal reported = sanitize(reportedQty);
        if (lastReported == null) {
            return current.add(reported);
        }
        if (reported.compareTo(lastReported) >= 0) {
            return current.add(reported.subtract(lastReported));
        }
        // 누적값이 줄어들면 라인 컨트롤러가 리셋한 것으로 간주하고 최신 누적값으로 덮어쓴다.
        return reported;
    }

    private String resolveEquipmentKey(DefectiveTelemetryPayload payload) {
        if (payload == null) {
            return "default";
        }
        if (payload.equipmentId() != null) {
            return "id:" + payload.equipmentId();
        }
        if (StringUtils.hasText(payload.equipmentCode())) {
            return "code:" + payload.equipmentCode();
        }
        return "default";
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
