package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetAllProductionPerformanceResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceAllQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPerformanceServiceImpl implements ProductionPerformanceService {

    private final ProductionPerformanceRepository performanceRepository;
    private final LotRepository lotRepository;
    private final ProductionPerformanceAllQueryRepository productionPerformanceAllQueryRepository;

    // 생산실적 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetProductionPerformanceListResponseDto> getProductionPerformanceList(
            SearchProductionPerformanceRequestDto condition,
            final Pageable pageable
    ) {
        return performanceRepository.searchProductionPerformanceList(condition, pageable);
    }

    // 생산실적 상세 조회
    @Override
    @Transactional(readOnly = true)
    public GetProductionPerformanceDetailResponseDto getProductionPerformanceDetail(Long id) {

        ProductionPerformances perf = performanceRepository.findById(id)
                .orElseThrow(ProductionPerformanceNotFoundException::new);

        // 생산계획
        ProductionPlans plan = perf.getProductionPlan();
        // 품목
        Items item = plan.getItemLine().getItem();
        // 라인
        Lines line = plan.getItemLine().getLine();
        // 공장
        Factories factory = line.getFactory();
        // LOT
        Lots lot = lotRepository.findByProductionPlanId(plan.getId());

        return GetProductionPerformanceDetailResponseDto.builder()
                .documentNo(perf.getPerformanceDocumentNo())

                .factoryCode(factory.getFactoryCode())
                .lineCode(line.getLineCode())

                .salesManagerNo(plan.getSalesManager().getEmpNo())
                .productionManagerNo(plan.getProductionManager().getEmpNo())

                .lotNo(lot != null ? lot.getLotNo() : null)

                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpecification(item.getItemSpecification())
                .itemUnit(item.getItemUnit())

                .totalQty(perf.getTotalQty())
                .performanceQty(perf.getPerformanceQty())
                .defectiveQty(perf.getPerformanceDefectiveQty())
                .defectiveRate(perf.getPerformanceDefectiveRate())

                .startTime(perf.getStartTime())
                .endTime(perf.getEndTime())
                .dueDate(plan.getDueDate())

                .createdAt(perf.getCreatedAt())
                .updatedAt(perf.getUpdatedAt())

                .build();
    }

    // 생산실적 현황 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetAllProductionPerformanceResponseDto> getAllProductionPerformances(
            SearchAllProductionPerformanceRequestDto condition
    ) {
        log.debug(condition.toString());

        List<GetAllProductionPerformanceResponseDto> results =
                productionPerformanceAllQueryRepository.searchAll(condition);

        if (results == null || results.isEmpty()) {
            throw new AppException(CommonErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
        }
        return productionPerformanceAllQueryRepository.searchAll(condition);
    }
}
