package com.beyond.synclab.ctrlline.domain.productionperformance.repository;

import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPerformanceRepository
        extends JpaRepository<ProductionPerformances, Long>, ProductionPerformanceQueryRepository {

}
