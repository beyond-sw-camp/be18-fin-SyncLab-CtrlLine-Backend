package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXref;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanDefectiveXrefRepository extends JpaRepository<PlanDefectiveXref, Long> {

    Optional<PlanDefectiveXref> findByPlanDefectiveIdAndDefectiveId(Long planDefectiveId, Long defectiveId);
}
