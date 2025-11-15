package com.beyond.synclab.ctrlline.domain.production.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlans, Long> {

    List<ProductionPlans> findAllByStatusAndStartTimeLessThanEqual(PlanStatus status, LocalDateTime startTime);

    List<ProductionPlans> findAllByLine_lineCode(String lineCode);
}
