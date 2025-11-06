package com.beyond.synclab.ctrlline.domain.production.repository;

import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.production.entity.ProductionPlans.PlanStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlans, Long> {

    List<ProductionPlans> findAllByStatusAndStartAtLessThanEqual(PlanStatus status, LocalDateTime startAt);
}
