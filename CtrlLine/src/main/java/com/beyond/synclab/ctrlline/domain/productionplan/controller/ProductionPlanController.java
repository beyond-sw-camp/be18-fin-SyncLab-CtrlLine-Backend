package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/production-plans")
public class ProductionPlanController {
    private final ProductionPlanService productionPlanService;

    @PostMapping
    public ResponseEntity<BaseResponse<GetProductionPlanResponseDto>> createProductionPlan(
        @RequestBody @Valid CreateProductionPlanRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();

        GetProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                BaseResponse.of(HttpStatus.CREATED.value(), responseDto)
            );
    }

    @GetMapping("/{planId}")
    public ResponseEntity<BaseResponse<GetProductionPlanDetailResponseDto>> getProductionPlan(
        @PathVariable Long planId
    ) {
        GetProductionPlanDetailResponseDto responseDto = productionPlanService.getProductionPlan(planId);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<GetProductionPlanListResponseDto>>> getProductionPlanList(
        @ModelAttribute SearchProductionPlanCommand searchCommand,
        @PageableDefault(sort = "documentNo", direction = Direction.DESC) Pageable pageable
    ) {
        Page<GetProductionPlanListResponseDto> listResponseDto = productionPlanService.getProductionPlanList(searchCommand, pageable);

        return ResponseEntity.ok(BaseResponse.ok(PageResponse.from(listResponseDto)));
    }

    @PatchMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<GetProductionPlanResponseDto>> updateProductionPlan(
        @RequestBody UpdateProductionPlanRequestDto requestDto,
        @PathVariable Long planId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();
        GetProductionPlanResponseDto responseDto = productionPlanService.updateProductionPlan(requestDto, planId, user);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<GetAllProductionPlanResponseDto>>> getAllProductionPlan(
        @ModelAttribute GetAllProductionPlanRequestDto requestDto
    ) {
        List<GetAllProductionPlanResponseDto> responseDto = productionPlanService.getAllProductionPlan(requestDto);

        return  ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @GetMapping("/schedules")
    public ResponseEntity<BaseResponse<List<GetProductionPlanScheduleResponseDto>>> getProductionPlanSchedule(
        @ModelAttribute @Valid GetProductionPlanScheduleRequestDto requestDto
    ) {
        List<GetProductionPlanScheduleResponseDto> responseDto = productionPlanService.getProductionPlanSchedule(requestDto);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }
}
