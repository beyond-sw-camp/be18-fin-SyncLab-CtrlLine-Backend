package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefective;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanDefectiveRepository extends JpaRepository<PlanDefective, Long> {

    boolean existsByProductionPlanId(Long productionPlanId);

    Optional<PlanDefective> findTopByDefectiveDocumentNoStartingWithOrderByIdDesc(String defectiveDocumentNo);

    Optional<PlanDefective> findByProductionPlanId(Long productionPlanId);
}
