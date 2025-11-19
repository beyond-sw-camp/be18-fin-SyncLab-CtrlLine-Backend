package com.beyond.synclab.ctrlline.domain.productionperformance.repository;

import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPerformanceRepository extends JpaRepository<ProductionPerformances, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pp.performanceDocumentNo
        FROM ProductionPerformances pp
        WHERE pp.performanceDocumentNo LIKE :prefix%
        ORDER BY pp.performanceDocumentNo DESC
    """)
    List<String> findDocumentNosByPrefix(@Param("prefix") String prefix);
}
