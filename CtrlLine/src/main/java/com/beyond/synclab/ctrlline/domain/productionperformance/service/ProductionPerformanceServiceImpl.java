package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
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
    public Page<GetProductionPerformanceListResponseDto> getProductionPerformanceList(
            final String documentNo,
            final String factoryCode,
            final String lineCode,
            final String itemCode,
            final String productionPlanDocumentNo,
            final Double minTotalQty,
            final Double maxTotalQty,
            final Double minPerformanceQty,
            final Double maxPerformanceQty,
            final Double minDefectRate,
            final Double maxDefectRate,
            final String salesManagerName,
            final String producerManagerName,
            final String startDate,
            final String endDate,
            final String dueDate,
            final String remark,
            final Boolean isDeleted,
            final Pageable pageable
    ) {

        final SearchProductionPerformanceRequestDto condition =
                SearchProductionPerformanceRequestDto.builder()
                        .documentNo(documentNo)
                        .factoryCode(factoryCode)
                        .lineCode(lineCode)
                        .itemCode(itemCode)
                        .productionPlanDocumentNo(productionPlanDocumentNo)
                        .minTotalQty(minTotalQty)
                        .maxTotalQty(maxTotalQty)
                        .minPerformanceQty(minPerformanceQty)
                        .maxPerformanceQty(maxPerformanceQty)
                        .minDefectRate(minDefectRate)
                        .maxDefectRate(maxDefectRate)
                        .salesManagerName(salesManagerName)
                        .producerManagerName(producerManagerName)
                        .startDate(startDate)
                        .endDate(endDate)
                        .dueDate(dueDate)
                        .remark(remark)
                        .isDeleted(isDeleted)
                        .build();

        return performanceRepository.searchProductionPerformanceList(condition, pageable);
    }
}
