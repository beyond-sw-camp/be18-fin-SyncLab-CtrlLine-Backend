package com.beyond.synclab.ctrlline.domain.productionperformance.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchAllProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.*;
import com.beyond.synclab.ctrlline.domain.productionperformance.service.ProductionPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.beyond.synclab.ctrlline.common.dto.BaseResponse.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/production-performances")
@RequiredArgsConstructor
public class ProductionPerformanceController {

    private final ProductionPerformanceService productionPerformanceService;

    // 생산실적 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<GetProductionPerformanceListResponseDto>>> getProductionPerformanceList(
            @ModelAttribute SearchProductionPerformanceRequestDto condition,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug(condition.toString());
        Page<GetProductionPerformanceListResponseDto> result =
                productionPerformanceService.getProductionPerformanceList(
                        condition, pageable
                );
        PageResponse<GetProductionPerformanceListResponseDto> response =
                PageResponse.from(result);
        return ResponseEntity.ok(ok(response));
    }

    // 생산실적 상세 조회
    @GetMapping("/{productionPerformanceId}")
    public ResponseEntity<BaseResponse<GetProductionPerformanceDetailResponseDto>> getDetail(
            @PathVariable Long productionPerformanceId
    ) {
        GetProductionPerformanceDetailResponseDto result =
                productionPerformanceService.getProductionPerformanceDetail(productionPerformanceId);
        return ResponseEntity.ok(ok(result));
    }

    // 생산실적 현황 조회
    @GetMapping("/all")
    public ResponseEntity<List<GetAllProductionPerformanceResponseDto>> getAllPerformances(
            @ModelAttribute  SearchAllProductionPerformanceRequestDto condition
    ) {
        List<GetAllProductionPerformanceResponseDto> result =
                productionPerformanceService.getAllProductionPerformances(condition);
        return ResponseEntity.ok(result);
    }

    // 공장별 월 생산량 조회
    @GetMapping("/monthly")
    public ResponseEntity<BaseResponse<GetProductionPerformanceMonthlySumResponseDto.FactoryMonthlyPerformance>>
    getMonthlyProductionPerformance(
            @RequestParam String factoryCode,
            @RequestParam(required = false) String baseMonth
    ) {
        GetProductionPerformanceMonthlySumResponseDto.FactoryMonthlyPerformance result =
                productionPerformanceService.getMonthlySumProductionPerformances(factoryCode, baseMonth);

        return ResponseEntity.ok(ok(result));
    }

    // 공장별 월별 불량률 조회
    @GetMapping("/monthly-defective-rate")
    public ResponseEntity<BaseResponse<GetProductionPerformanceMonthlyDefectiveRateResponseDto.FactoryMonthlyDefectiveRate>>
    getMonthlyDefectiveRate(
            @RequestParam String factoryCode,
            @RequestParam(required = false) String baseMonth
    ) {
        GetProductionPerformanceMonthlyDefectiveRateResponseDto.FactoryMonthlyDefectiveRate result =
                productionPerformanceService.getMonthlyDefectiveRateProductionPerformances(factoryCode, baseMonth);

        return ResponseEntity.ok(ok(result));
    }
}
