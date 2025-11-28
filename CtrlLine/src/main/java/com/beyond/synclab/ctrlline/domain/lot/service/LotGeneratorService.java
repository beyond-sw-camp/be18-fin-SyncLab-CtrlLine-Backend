package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.itemline.entity.ItemsLines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
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
public class LotGeneratorService {

    private static final DateTimeFormatter LOT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final LotRepository lotRepository;
    private final Clock clock;

    @Transactional
    public void createLot(ProductionPlans plan) {
        if (plan == null || plan.getId() == null) {
            log.warn("생산계획 정보가 없어 LOT을 생성하지 않습니다.");
            return;
        }
        if (lotRepository.existsByProductionPlanId(plan.getId())) {
            return;
        }
        Long itemId = resolveItemId(plan);
        if (itemId == null) {
            log.warn("아이템 정보를 확인할 수 없어 LOT을 생성하지 않습니다. planId={}", plan.getId());
            return;
        }
        String lotNo = generateLotNo();
        Lots lot = Lots.builder()
                .productionPlanId(plan.getId())
                .itemId(itemId)
                .lotNo(lotNo)
                .build();
        lotRepository.save(lot);
        log.info("Lot created planId={} lotNo={}", plan.getId(), lotNo);
    }

    private Long resolveItemId(ProductionPlans plan) {
        ItemsLines itemLine = plan.getItemLine();
        if (itemLine == null) {
            return null;
        }
        return itemLine.getItemId();
    }

    private String generateLotNo() {
        LocalDate today = LocalDate.now(clock);
        String prefix = today.format(LOT_DATE_FORMATTER) + "-";
        Optional<Lots> latest = lotRepository.findTopByLotNoStartingWithOrderByIdDesc(prefix);
        int sequence = latest
                .map(Lots::getLotNo)
                .map(this::extractSequence)
                .orElse(0) + 1;
        return prefix + sequence;
    }

    private int extractSequence(String lotNo) {
        int dashIndex = lotNo.lastIndexOf("-");
        if (dashIndex < 0 || dashIndex == lotNo.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(lotNo.substring(dashIndex + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
