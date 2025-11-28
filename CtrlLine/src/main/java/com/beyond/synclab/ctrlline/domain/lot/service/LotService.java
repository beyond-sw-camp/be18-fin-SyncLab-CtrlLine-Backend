package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotService {

    private final LotRepository lotRepository;

    public Optional<Lots> findByProductionPlanId(Long productionPlanId) {
        if (productionPlanId == null) {
            return Optional.empty();
        }
        return lotRepository.findByProductionPlanId(productionPlanId);
    }

    public Lots getByProductionPlanId(Long productionPlanId) {
        return findByProductionPlanId(productionPlanId)
                .orElseThrow(LotNotFoundException::new);
    }
}
