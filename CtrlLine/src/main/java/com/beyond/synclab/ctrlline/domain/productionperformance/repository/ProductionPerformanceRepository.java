package com.beyond.synclab.ctrlline.domain.productionperformance.repository;

import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceQueryRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
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

    Optional<ProductionPerformances> findByProductionPlanId(Long productionPlanId);

    Optional<ProductionPerformances> findById(Long id);
}
