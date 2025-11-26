package com.beyond.synclab.ctrlline.domain.productionplan.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectiveXrefs;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanDefectiveXrefRepository extends JpaRepository<PlanDefectiveXrefs, Long> {

    Optional<PlanDefectiveXrefs> findByPlanDefectiveIdAndDefectiveId(Long planDefectiveId, Long defectiveId);

    List<PlanDefectiveXrefs> findAllByPlanDefectiveId(Long id);
}
