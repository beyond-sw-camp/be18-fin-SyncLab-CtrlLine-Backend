package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanDefectiveRepository extends JpaRepository<PlanDefectives, Long> {

    boolean existsByProductionPlanId(Long productionPlanId);

    Optional<PlanDefectives> findTopByDefectiveDocumentNoStartingWithOrderByIdDesc(String defectiveDocumentNo);

    Optional<PlanDefectives> findByProductionPlanId(Long productionPlanId);

    Optional<PlanDefectives> findByDefectiveDocumentNo(String documentNo);
}
