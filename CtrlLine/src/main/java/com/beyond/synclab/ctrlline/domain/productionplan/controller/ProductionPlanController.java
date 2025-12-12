package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.DeleteProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanBoundaryResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.PlanScheduleChangeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.SearchProductionPlanCommand;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanCommitRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanStatusResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.UpdateProductionPlanStatusRequestDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/production-plans")
public class ProductionPlanController {
    private final ProductionPlanService productionPlanService;


    @PostMapping
    public ResponseEntity<BaseResponse<PlanScheduleChangeResponseDto>> createProductionPlan(
        @RequestBody @Valid CreateProductionPlanRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();

        PlanScheduleChangeResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, user);

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
    public ResponseEntity<BaseResponse<PlanScheduleChangeResponseDto>> updateProductionPlan(
        @RequestBody UpdateProductionPlanRequestDto requestDto,
        @PathVariable Long planId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();
        PlanScheduleChangeResponseDto responseDto = productionPlanService.updateProductionPlan(requestDto, planId, user);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @PostMapping("/update/{planId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<PlanScheduleChangeResponseDto>> updateProductionPlanPreview(
        @RequestBody UpdateProductionPlanRequestDto requestDto,
        @PathVariable Long planId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();
        PlanScheduleChangeResponseDto responseDto = productionPlanService.updateProductionPlanPreview(requestDto, planId, user);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @PatchMapping("/update/commit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<PlanScheduleChangeResponseDto>> updateProductionPlanCommit(
        @RequestBody UpdateProductionPlanCommitRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();
        PlanScheduleChangeResponseDto responseDto = productionPlanService.updateProductionPlanCommit(requestDto, user);

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

    @PostMapping("/endtime")
    public ResponseEntity<BaseResponse<GetProductionPlanEndTimeResponseDto>> getEndTime(
        @RequestBody GetProductionPlanEndTimeRequestDto requestDto
    ) {
        GetProductionPlanEndTimeResponseDto responseDto = productionPlanService.getProductionPlanEndTime(requestDto);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<UpdateProductionPlanStatusResponseDto>> updateStatues(
        @Valid @RequestBody UpdateProductionPlanStatusRequestDto requestDto
    ) {
        UpdateProductionPlanStatusResponseDto responseDto = productionPlanService.updateProductionPlanStatus(requestDto);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<Void>> deleteProductionPlan(
        @PathVariable Long planId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user =  userDetails.getUser();
        productionPlanService.deleteProductionPlan(planId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<Void>> deleteProductionPlans(
        @RequestBody @Valid DeleteProductionPlanRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user =  userDetails.getUser();
        productionPlanService.deleteProductionPlans(requestDto, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/boundary")
    public ResponseEntity<BaseResponse<GetProductionPlanBoundaryResponseDto>> getPlanBoundaries(
        @RequestParam String factoryCode,
        @RequestParam String lineCode
    ) {
        GetProductionPlanBoundaryResponseDto response =
            productionPlanService.getPlanBoundaries(factoryCode, lineCode);

        return ResponseEntity.ok(BaseResponse.ok(response));
    }

}
