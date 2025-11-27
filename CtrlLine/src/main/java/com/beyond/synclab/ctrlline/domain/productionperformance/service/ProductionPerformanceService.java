package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.*;
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
    GetProductionPerformanceDetailResponseDto getProductionPerformanceDetail(
            Long id
    );

    // 생산실적 현황 조회
    List<GetAllProductionPerformanceResponseDto> getAllProductionPerformances(
            SearchAllProductionPerformanceRequestDto condition
    );

    // 공장별 월별 생산량 조회 (대시보드)
    GetProductionPerformanceMonthlySumResponseDto.FactoryMonthlyPerformance
    getMonthlySumProductionPerformances(String factoryCode, String baseMonth);

    // 공장별 월별 불량률 조회 (대시보드)
    GetProductionPerformanceMonthlyDefectiveRateResponseDto.FactoryMonthlyDefectiveRate
    getMonthlyDefectiveRateProductionPerformances(String factoryCode, String baseMonth);
}
