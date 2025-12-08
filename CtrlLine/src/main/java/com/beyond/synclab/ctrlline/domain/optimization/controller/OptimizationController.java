package com.beyond.synclab.ctrlline.domain.optimization.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitRequestDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.service.ProductionPlanOptimizationService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/production-plans")
public class OptimizationController {
    private final ProductionPlanOptimizationService optimizationService;

    /**
     * 생산계획 최적화 미리보기
     */
    @PostMapping("/{lineCode}/optimize/preview")
    public ResponseEntity<BaseResponse<OptimizePreviewResponseDto>> optimizePreview(
        @PathVariable String lineCode,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Users user = customUserDetails.getUser();
        return ResponseEntity.ok(
            BaseResponse.ok(
                optimizationService.previewOptimization(lineCode, user)
            )
        );
    }

    /**
     * 최적화 결과 확정(저장)
     */
    @PostMapping("/{lineCode}/optimize/commit")
    public ResponseEntity<BaseResponse<OptimizeCommitResponseDto>> optimizeCommit(
        @PathVariable String lineCode,
        @RequestBody OptimizeCommitRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Users user = customUserDetails.getUser();
        return ResponseEntity.ok(
            BaseResponse.ok(
                optimizationService.commitOptimization(lineCode, requestDto.getPreviewKey(), user)
            )
        );
    }
}
