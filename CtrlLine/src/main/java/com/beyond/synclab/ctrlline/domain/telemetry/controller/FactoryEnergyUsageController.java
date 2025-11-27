package com.beyond.synclab.ctrlline.domain.telemetry.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnergyUsageResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.service.FactoryEnergyUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/factories")
@RequiredArgsConstructor
@Tag(name = "Factory Energy", description = "공장별 전력 사용량 API")
public class FactoryEnergyUsageController {

    private final FactoryEnergyUsageService factoryEnergyUsageService;

    @Operation(summary = "공장 최신 전력 사용량 조회")
    @GetMapping("/{factoryCode}/energy/latest")
    public ResponseEntity<BaseResponse<FactoryEnergyUsageResponse>> getLatestEnergyUsage(
            @PathVariable("factoryCode") String factoryCode) {
        FactoryEnergyUsageResponse response = factoryEnergyUsageService.getLatestEnergyUsage(factoryCode);
        return ResponseEntity.ok(BaseResponse.of(HttpStatus.OK.value(), response));
    }
}
