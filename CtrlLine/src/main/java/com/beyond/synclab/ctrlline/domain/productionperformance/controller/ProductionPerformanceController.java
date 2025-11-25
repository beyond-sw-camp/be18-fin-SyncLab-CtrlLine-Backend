package com.beyond.synclab.ctrlline.domain.productionperformance.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.service.ProductionPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
            @RequestParam(required = false) String documentNo,
            @RequestParam(required = false) String factoryCode,
            @RequestParam(required = false) String lineCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String productionPlanDocumentNo,
            @RequestParam(required = false) BigDecimal minTotalQty,
            @RequestParam(required = false) BigDecimal maxTotalQty,
            @RequestParam(required = false) BigDecimal minPerformanceQty,
            @RequestParam(required = false) BigDecimal maxPerformanceQty,
            @RequestParam(required = false) BigDecimal minDefectRate,
            @RequestParam(required = false) BigDecimal maxDefectRate,
            @RequestParam(required = false) String salesManagerName,
            @RequestParam(required = false) String producerManagerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        Page<GetProductionPerformanceListResponseDto> result =
                productionPerformanceService.getProductionPerformanceList(
                        documentNo,
                        factoryCode,
                        lineCode,
                        itemCode,
                        productionPlanDocumentNo,
                        minTotalQty,
                        maxTotalQty,
                        minPerformanceQty,
                        maxPerformanceQty,
                        minDefectRate,
                        maxDefectRate,
                        salesManagerName,
                        producerManagerName,
                        startDate,
                        endDate,
                        dueDate,
                        remark,
                        isDeleted,
                        pageable
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
}
