package com.beyond.synclab.ctrlline.domain.productionperformance.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.*;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceErrorCode;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceAllQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceMonthlyDefRateQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.query.ProductionPerformanceMonthlyQueryRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.PlanDefectives;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.PlanDefectiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPerformanceServiceImpl implements ProductionPerformanceService {

    private final ProductionPerformanceRepository performanceRepository;
    private final LotRepository lotRepository;
    private final ProductionPerformanceAllQueryRepository productionPerformanceAllQueryRepository;
    private final FactoryRepository factoryRepository;
    private final ProductionPerformanceMonthlyQueryRepository productionPerformanceMonthlyQueryRepository;
    private final ProductionPerformanceMonthlyDefRateQueryRepository productionPerformanceMonthlyDefectiveRateQueryRepository;
    private final PlanDefectiveRepository planDefectiveRepository;

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

        ProductionPlans plan = perf.getProductionPlan();

        Lots lot = lotRepository.findByProductionPlanId(plan.getId())
                .orElseThrow(LotNotFoundException::new);

        PlanDefectives planDefective = planDefectiveRepository
                .findByProductionPlanId(plan.getId())
                .orElse(null);

        return GetProductionPerformanceDetailResponseDto.fromEntity(
                perf,
                lot,
                planDefective
        );
    }

    // 생산실적 현황 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetAllProductionPerformanceResponseDto> getAllProductionPerformances(
            SearchAllProductionPerformanceRequestDto condition
    ) {
        List<GetAllProductionPerformanceResponseDto> results =
                productionPerformanceAllQueryRepository.searchAll(condition);

        if (results == null || results.isEmpty()) {
            throw new AppException(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
        }
        return productionPerformanceAllQueryRepository.searchAll(condition);
    }

    // 공장별 최근 6개월 누적 생산량
    @Override
    @Transactional(readOnly = true)
    public GetProductionPerformanceMonthlySumResponseDto.FactoryMonthlyPerformance
    getMonthlySumProductionPerformances(String factoryCode, String baseMonth) {

        // 공장 검증
        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                .orElseThrow(() -> new AppException(CommonErrorCode.INVALID_INPUT_VALUE));

        // 기준월 검증
        YearMonth base;
        try {
            base = (baseMonth == null || baseMonth.isBlank())
                    ? YearMonth.now()
                    : YearMonth.parse(baseMonth);
        } catch (Exception ex) {
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        List<YearMonth> months = IntStream.rangeClosed(0, 5)
                .mapToObj(base::minusMonths)
                .sorted()
                .toList();

        // 월별 누적 생산량 조회
        Map<YearMonth, Long> sumByMonth =
                productionPerformanceMonthlyQueryRepository.getMonthlySum(factoryCode, months);

        // 예외 처리: 조회 결과가 null 또는 empty면 404 발생
        if (sumByMonth == null || sumByMonth.isEmpty()) {
            throw new AppException(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
        }

        // 월별 DTO 변환
        List<GetProductionPerformanceMonthlySumResponseDto> performances = months.stream()
                .map(ym -> GetProductionPerformanceMonthlySumResponseDto.of(
                        ym.toString(),
                        sumByMonth.getOrDefault(ym, 0L)
                ))
                .toList();

        // 최종 Wrapper 반환
        return GetProductionPerformanceMonthlySumResponseDto.FactoryMonthlyPerformance.of(
                factory.getFactoryCode(),
                factory.getFactoryName(),
                performances
        );
    }

    // 공장별 최근 6개월 월별 불량률 조회
    @Override
    @Transactional(readOnly = true)
    public GetProductionPerformanceMonthlyDefRateResponseDto.FactoryMonthlyDefectiveRate
    getMonthlyDefectiveRateProductionPerformances(String factoryCode, String baseMonth) {

        // 공장 검증
        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                .orElseThrow(() -> new AppException(CommonErrorCode.INVALID_INPUT_VALUE));

        // 기준월 파싱 (예외 처리 포함)
        YearMonth base;
        try {
            base = (baseMonth == null || baseMonth.isBlank())
                    ? YearMonth.now()
                    : YearMonth.parse(baseMonth);
        } catch (Exception ex) {
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        // 기준월 + 이전 5개월 리스트 생성 (오름차순 정렬)
        List<YearMonth> months = IntStream.rangeClosed(0, 5)
                .mapToObj(base::minusMonths)
                .sorted()
                .toList();

        // 월별 totalQtySum, performanceQtySum 조회
        Map<YearMonth, ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum> qtySumMap =
                productionPerformanceMonthlyDefectiveRateQueryRepository.getMonthlyQtySum(factoryCode, months);

        // 월별 불량률 계산 후 DTO 변환
        List<GetProductionPerformanceMonthlyDefRateResponseDto> performances = months.stream()
                .map(ym -> {

                    ProductionPerformanceMonthlyDefRateQueryRepository.MonthlyQtySum qtySum =
                            qtySumMap.get(ym);

                    BigDecimal totalQty = (qtySum != null) ? qtySum.getTotalQtySum() : BigDecimal.ZERO;
                    BigDecimal performanceQty = (qtySum != null) ? qtySum.getPerformanceQtySum() : BigDecimal.ZERO;
                    log.info("YM={} totalQty={} performanceQty={}", ym, totalQty, performanceQty);

                    // (total - performance) / performance * 100
                    BigDecimal defectiveRate =
                            (performanceQty.compareTo(BigDecimal.ZERO) == 0)
                                    ? BigDecimal.ZERO
                                    : totalQty.subtract(performanceQty)
                                    .divide(performanceQty, 10, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP); // ← 소수점 둘째 자리 정확

                    return GetProductionPerformanceMonthlyDefRateResponseDto.of(
                            ym.toString(),
                            defectiveRate
                    );
                })
                .toList();

        // Wrapper DTO 생성 후 반환
        return GetProductionPerformanceMonthlyDefRateResponseDto.FactoryMonthlyDefectiveRate.of(
                factory.getFactoryCode(),
                factory.getFactoryName(),
                performances
        );
    }

    // 생산실적 remark 수정
    @Override
    @Transactional
    public GetProductionPerformanceDetailResponseDto updatePerformanceRemark(Long id, String remark) {

        ProductionPerformances perf = performanceRepository.findById(id)
                .orElseThrow(ProductionPerformanceNotFoundException::new);

        // remark 업데이트
        perf.updateRemark(remark);

        // DB 다시 조회
        ProductionPerformances updated = performanceRepository.findById(id)
                .orElseThrow(ProductionPerformanceNotFoundException::new);

        // LOT 조회
        Lots lot = lotRepository.findByProductionPlanId(updated.getProductionPlan().getId())
                .orElseThrow(LotNotFoundException::new);

        // 불량전표 조회
        PlanDefectives planDefective = planDefectiveRepository
                .findByProductionPlanId(updated.getProductionPlan().getId())
                .orElse(null);

        // DTO 조립
        return GetProductionPerformanceDetailResponseDto.fromEntity(
                updated,
                lot,
                planDefective
        );
    }
}
