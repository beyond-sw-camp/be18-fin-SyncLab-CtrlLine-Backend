package com.beyond.synclab.ctrlline.domain.productionperformance.repository;

import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceQueryRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionPerformanceRepository
        extends JpaRepository<ProductionPerformances, Long>, ProductionPerformanceQueryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pp.performanceDocumentNo
        FROM ProductionPerformances pp
        WHERE pp.performanceDocumentNo LIKE :prefix%
        ORDER BY pp.performanceDocumentNo DESC
    """)
    List<String> findDocumentNosByPrefix(@Param("prefix") String prefix);

    Optional<ProductionPerformances> findByProductionPlanIdAndIsDeletedFalse(Long productionPlanId);

    Optional<ProductionPerformances> findById(Long id);

    @Query("""
        SELECT pp
        FROM ProductionPerformances pp
        JOIN pp.productionPlan plan
        JOIN plan.itemLine itemLine
        JOIN itemLine.line line
        WHERE line.id = :lineId
        ORDER BY pp.endTime DESC
    """)
    List<ProductionPerformances> findRecentByLineId(@Param("lineId") Long lineId, Pageable pageable);

    @Query("""
    SELECT p.productionPlanId AS planId, MAX(p.endTime) AS actualEnd
    FROM ProductionPerformances p
    WHERE p.productionPlanId IN :planIds
    GROUP BY p.productionPlanId
""")
    List<Tuple> findLatestActualEndTimeTuples(@Param("planIds") List<Long> planIds);
}
