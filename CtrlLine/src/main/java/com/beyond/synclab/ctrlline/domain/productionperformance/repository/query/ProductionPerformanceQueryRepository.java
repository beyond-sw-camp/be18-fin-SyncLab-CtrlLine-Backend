package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionPerformanceQueryRepository {

    Page<GetProductionPerformanceListResponseDto> searchProductionPerformanceList(
            final SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    );
}
