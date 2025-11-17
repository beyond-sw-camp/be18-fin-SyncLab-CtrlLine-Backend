package com.beyond.synclab.ctrlline.domain.production.repository;

import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlans, Long> {

    List<ProductionPlans> findAllByStatusAndStartTimeLessThanEqual(PlanStatus status, LocalDateTime startTime);

    List<ProductionPlans> findAllByLine_lineCode(String lineCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pp.documentNo FROM ProductionPlans pp WHERE pp.documentNo LIKE :prefix% ORDER BY pp.documentNo DESC")
    List<String> findByDocumentNoByPrefix(@Param("prefix") String prefix);
}
