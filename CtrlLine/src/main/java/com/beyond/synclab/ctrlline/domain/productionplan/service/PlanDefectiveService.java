package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanDefectiveService {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final PlanDefectiveRepository planDefectiveRepository;
    private final Clock clock;

    @Transactional
    public void createPlanDefective(ProductionPlans plan) {
        if (plan == null || plan.getId() == null) {
            log.warn("생산계획 정보가 없어 불량 전표를 생성하지 않습니다.");
            return;
        }
        if (planDefectiveRepository.existsByProductionPlanId(plan.getId())) {
            return;
        }
        String documentNo = generateDocumentNo();
        PlanDefectives planDefective = PlanDefectives.builder()
                .productionPlanId(plan.getId())
                .defectiveDocumentNo(documentNo)
                .build();
        planDefectiveRepository.save(planDefective);
        log.info("Plan defective document created planId={} documentNo={}", plan.getId(), documentNo);
    }

    private String generateDocumentNo() {
        LocalDate today = LocalDate.now(clock);
        String prefix = today.format(DOCUMENT_DATE_FORMATTER) + "-";
        Optional<PlanDefectives> latest = planDefectiveRepository
                .findTopByDefectiveDocumentNoStartingWithOrderByIdDesc(prefix);
        int sequence = latest
                .map(PlanDefectives::getDefectiveDocumentNo)
                .map(this::extractSequence)
                .orElse(0) + 1;
        return prefix + sequence;
    }

    private int extractSequence(String documentNo) {
        int dashIndex = documentNo.lastIndexOf("-");
        if (dashIndex < 0 || dashIndex == documentNo.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(documentNo.substring(dashIndex + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
