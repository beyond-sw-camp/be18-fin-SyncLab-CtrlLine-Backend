package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<BaseResponse<ProductionPlanResponseDto>> createProductionPlan(
        @RequestBody @Valid CreateProductionPlanRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Users user = userDetails.getUser();

        ProductionPlanResponseDto responseDto = productionPlanService.createProductionPlan(requestDto, user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                BaseResponse.of(HttpStatus.CREATED.value(), responseDto)
            );
    }
}
