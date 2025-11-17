package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPerformanceServiceImpl implements ProductionPerformanceService {

    private final ProductionPerformanceRepository performanceRepository;

    // 생산실적 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetProductionPerformanceListResponseDto> getPerformanceList(
            final SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    ) {
        Page<ProductionPerformances> result =
                performanceRepository.searchProductionPerformances(condition, pageable);

        log.info("[PERFORMANCE-LIST] 조회 완료 - count={}, filters={}",
                result.getTotalElements(), condition);

        return result.map(GetProductionPerformanceListResponseDto::fromEntity);
    }
}
