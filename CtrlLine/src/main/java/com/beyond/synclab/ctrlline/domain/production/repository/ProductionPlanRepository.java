package com.beyond.synclab.ctrlline.domain.production.repository;

import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlan.PlanStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {

    List<ProductionPlan> findAllByStatusAndStartAtLessThanEqual(PlanStatus status, LocalDateTime startAt);
}
