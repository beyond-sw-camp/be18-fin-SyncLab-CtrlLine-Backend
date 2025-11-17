package com.beyond.synclab.ctrlline.domain.productionperformance.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.request.SearchProductionPerformanceRequestDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.service.ProductionPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @ModelAttribute SearchProductionPerformanceRequestDto requestDto,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {

        Page<GetProductionPerformanceListResponseDto> result =
                productionPerformanceService.getProductionPerformanceList(requestDto, pageable);

        PageResponse<GetProductionPerformanceListResponseDto> response =
                PageResponse.from(result);

        return ResponseEntity.ok(ok(response));
    }
}
