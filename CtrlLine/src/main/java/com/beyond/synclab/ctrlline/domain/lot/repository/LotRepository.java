package com.beyond.synclab.ctrlline.domain.lot.repository;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.query.LotQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lots, Long>, LotQueryRepository {

    boolean existsByProductionPlanId(Long productionPlanId);

    Optional<Lots> findTopByLotNoStartingWithOrderByIdDesc(String lotNoPrefix);

    Optional<Lots> findByProductionPlanId(Long productionPlanId);
}