package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefective;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXref;
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

    @Transactional
    public void linkPlanDefective(Long defectiveId, DefectiveTelemetryPayload payload) {
        if (defectiveId == null || payload == null) {
            return;
        }
        String orderNo = payload.orderNo();
        BigDecimal defectiveQty = payload.defectiveQuantity();
        if (!StringUtils.hasText(orderNo) || defectiveQty == null) {
            log.debug("order_no 또는 defective_qty가 없어 plan_defective_xref를 생성하지 않습니다. orderNo={}, defectiveQty={}",
                    orderNo, defectiveQty);
            return;
        }
        Optional<Long> planDefectiveIdOptional = findPlanDefectiveId(orderNo);
        if (planDefectiveIdOptional.isEmpty()) {
            log.warn("order_no에 해당하는 plan_defective 정보를 찾을 수 없어 저장하지 않습니다. orderNo={}", orderNo);
            return;
        }
        Long planDefectiveId = planDefectiveIdOptional.get();
        PlanDefectiveXref xref = planDefectiveXrefRepository
                .findByPlanDefectiveIdAndDefectiveId(planDefectiveId, defectiveId)
                .map(existing -> {
                    existing.increaseDefectiveQty(defectiveQty);
                    return existing;
                })
                .orElseGet(() -> PlanDefectiveXref.builder()
                        .planDefectiveId(planDefectiveId)
                        .defectiveId(defectiveId)
                        .defectiveQty(defectiveQty)
                        .build());
        planDefectiveXrefRepository.save(xref);
        log.info("plan_defective_xref 저장 완료 planDefectiveId={}, defectiveId={}, qty={}",
                planDefectiveId, defectiveId, defectiveQty);
    }

    private Optional<Long> findPlanDefectiveId(String orderNo) {
        return productionPlanRepository.findByDocumentNo(orderNo)
                .flatMap(plan -> planDefectiveRepository.findByProductionPlanId(plan.getId()))
                .map(PlanDefective::getId);
    }
}
