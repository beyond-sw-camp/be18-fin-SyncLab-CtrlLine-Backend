package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductionPerformanceService {

    // 생산실적 목록 조회
    Page<GetProductionPerformanceListResponseDto> getProductionPerformanceList(
            final String documentNo,
            final String factoryCode,
            final String lineCode,
            final String itemCode,
            final String productionPlanDocumentNo,
            final BigDecimal minTotalQty,
            final BigDecimal maxTotalQty,
            final BigDecimal minPerformanceQty,
            final BigDecimal maxPerformanceQty,
            final BigDecimal minDefectRate,
            final BigDecimal maxDefectRate,
            final String salesManagerName,
            final String producerManagerName,
            final String startDate,
            final String endDate,
            final String dueDate,
            final String remark,
            final Boolean isDeleted,
            final Pageable pageable
    );

    // 생산실적 상세 조회
    GetProductionPerformanceDetailResponseDto getProductionPerformanceDetail(Long id);

}
