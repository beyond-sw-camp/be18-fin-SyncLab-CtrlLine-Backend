package com.beyond.synclab.ctrlline.domain.lot.repository;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository<Lots, Long> {

    boolean existsByProductionPlanId(Long productionPlanId);

    Optional<Lots> findTopByLotNoStartingWithOrderByIdDesc(String lotNoPrefix);
}
