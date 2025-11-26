package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetAllProductionPerformanceResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductionPerformanceService {

    // 생산실적 목록 조회
    Page<GetProductionPerformanceListResponseDto> getProductionPerformanceList(
            SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    );

    // 생산실적 상세 조회
    GetProductionPerformanceDetailResponseDto getProductionPerformanceDetail(Long id);

    // 생산실적 현황 조회
    List<GetAllProductionPerformanceResponseDto> getAllProductionPerformances(
            SearchAllProductionPerformanceRequestDto condition
    );
}
