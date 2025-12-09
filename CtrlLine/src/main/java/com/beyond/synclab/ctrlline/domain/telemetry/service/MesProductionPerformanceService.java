package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.ProductionPerformanceTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.production.service.ProductionOrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesProductionPerformanceService {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionPerformanceRepository productionPerformanceRepository;
    private final ProductionOrderService productionOrderService;
    private final Clock clock;

    @Transactional
    public void saveProductionPerformance(ProductionPerformanceTelemetryPayload payload) {
        if (payload == null) {
            log.warn("생산실적 텔레메트리 페이로드가 비어있어 저장하지 않습니다.");
            return;
        }
        if (!StringUtils.hasText(payload.orderNo())) {
            log.warn("order_no가 없어 생산실적을 저장하지 않습니다. payload={}", payload);
            return;
        }
        if (payload.orderProducedQty() == null) {
            log.warn("order_produced_qty가 없어 생산실적을 저장하지 않습니다. payload={}", payload);
            return;
        }
        if (payload.executeAt() == null || payload.waitingAckAt() == null) {
            log.warn("시작/종료 시간이 없어 생산실적을 저장하지 않습니다. payload={}", payload);
            return;
        }

        ProductionPlans productionPlan = resolveProductionPlan(payload.orderNo());
        if (productionPlan == null) {
            log.warn("전표번호에 해당하는 생산계획을 찾을 수 없어 생산실적을 저장하지 않습니다. orderNo={}", payload.orderNo());
            return;
        }
        BigDecimal producedQty = normalizeQuantity(payload.orderProducedQty());
        BigDecimal ngQty = normalizeQuantity(Optional.ofNullable(payload.ngCount()).orElse(BigDecimal.ZERO));
        BigDecimal totalQty = producedQty.add(ngQty);
        BigDecimal defectiveRate = calculateDefectiveRate(totalQty, ngQty);

        ProductionPerformances performance = productionPerformanceRepository
                .findByProductionPlanId(productionPlan.getId())
                .map(existing -> {
                    existing.updatePerformance(totalQty, producedQty, defectiveRate,
                            payload.executeAt(), payload.waitingAckAt());
                    return existing;
                })
                .orElseGet(() -> ProductionPerformances.builder()
                        .productionPlan(productionPlan)
                        .productionPlanId(productionPlan.getId())
                        .performanceDocumentNo(createDocumentNo())
                        .totalQty(totalQty)
                        .performanceQty(producedQty)
                        .performanceDefectiveRate(defectiveRate)
                        .startTime(payload.executeAt())
                        .endTime(payload.waitingAckAt())
                        .remark(null)
                        .isDeleted(Boolean.FALSE)
                        .build());

        productionPerformanceRepository.save(performance);
        log.info("생산실적 저장 완료. performanceDocumentNo={}, planDocumentNo={}",
                performance.getPerformanceDocumentNo(),
                productionPlan.getDocumentNo());

        productionOrderService.sendLineAck(productionPlan);
    }

    @Transactional
    public void updateRunningProgress(String orderNo, BigDecimal producedQty, BigDecimal ngQty) {
        if (!StringUtils.hasText(orderNo)) {
            log.warn("orderNo가 없어 진행중 생산실적을 갱신하지 않습니다.");
            return;
        }

        ProductionPlans productionPlan = resolveProductionPlan(orderNo);
        if (productionPlan == null) {
            log.warn("전표번호에 해당하는 생산계획을 찾을 수 없어 진행중 생산실적을 갱신하지 않습니다. orderNo={}", orderNo);
            return;
        }

        BigDecimal normalizedProduced = normalizeQuantity(producedQty);
        BigDecimal normalizedNg = normalizeQuantity(Optional.ofNullable(ngQty).orElse(BigDecimal.ZERO));
        BigDecimal totalQty = normalizeQuantity(productionPlan.getPlannedQty());
        BigDecimal defectiveRate = calculateDefectiveRate(totalQty, normalizedNg);
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startTime = Optional.ofNullable(productionPlan.getStartTime()).orElse(now);

        ProductionPerformances performance = productionPerformanceRepository
                .findByProductionPlanId(productionPlan.getId())
                .map(existing -> {
                    existing.updatePerformance(
                            totalQty,
                            normalizedProduced,
                            defectiveRate,
                            startTime,
                            now
                    );
                    return existing;
                })
                .orElseGet(() -> ProductionPerformances.builder()
                        .productionPlan(productionPlan)
                        .productionPlanId(productionPlan.getId())
                        .performanceDocumentNo(createDocumentNo())
                        .totalQty(totalQty)
                        .performanceQty(normalizedProduced)
                        .performanceDefectiveRate(defectiveRate)
                        .startTime(startTime)
                        .endTime(now)
                        .remark(null)
                        .isDeleted(Boolean.FALSE)
                        .build());

        productionPerformanceRepository.save(performance);
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return quantity.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDefectiveRate(BigDecimal totalQty, BigDecimal ngQty) {
        if (totalQty == null || totalQty.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (ngQty == null || ngQty.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return ngQty
                .divide(totalQty, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String createDocumentNo() {
        LocalDate today = LocalDate.now(clock);
        String prefix = String.format("%04d/%02d/%02d", today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        List<String> documentNos = productionPerformanceRepository.findDocumentNosByPrefix(prefix);
        int maxSeq = documentNos.stream()
                .map(docNo -> {
                    int delimiterIdx = docNo.indexOf("-");
                    if (delimiterIdx < 0 || delimiterIdx == docNo.length() - 1) {
                        return 0;
                    }
                    try {
                        return Integer.parseInt(docNo.substring(delimiterIdx + 1));
                    } catch (NumberFormatException e) {
                        log.warn("전표번호 시퀀스 파싱 실패 documentNo={}", docNo, e);
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        int nextSeq = maxSeq + 1;
        return prefix + String.format("-%d", nextSeq);
    }

    private ProductionPlans resolveProductionPlan(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        Optional<ProductionPlans> runningPlan =
                productionPlanRepository.findFirstByDocumentNoAndStatusOrderByIdDesc(orderNo, PlanStatus.RUNNING);
        if (runningPlan.isPresent()) {
            return runningPlan.get();
        }
        return productionPlanRepository.findFirstByDocumentNoOrderByIdDesc(orderNo).orElse(null);
    }
}
