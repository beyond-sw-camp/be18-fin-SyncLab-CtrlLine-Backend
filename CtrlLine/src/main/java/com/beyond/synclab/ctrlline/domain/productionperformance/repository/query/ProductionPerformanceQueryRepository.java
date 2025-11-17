package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionPerformanceQueryRepository {

    Page<ProductionPerformances> searchProductionPerformances(
            final SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    );
}