package com.beyond.synclab.ctrlline.domain.productionperformance.repository.query;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetAllProductionPerformanceResponseDto;

import java.util.List;

public interface ProductionPerformanceAllQueryRepository {

    List<GetAllProductionPerformanceResponseDto> searchAll(
            SearchAllProductionPerformanceRequestDto condition);
}
