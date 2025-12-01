package com.beyond.synclab.ctrlline.domain.telemetry.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnvironmentResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.service.FactoryEnvironmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@Tag(name = "Factory Environment", description = "공장 온습도 API")
public class FactoryEnvironmentController {

    private final FactoryEnvironmentService factoryEnvironmentService;

    @Operation(summary = "공장 최신 온습도 조회")
    @GetMapping("/{factoryCode}/environment/latest")
    public ResponseEntity<BaseResponse<FactoryEnvironmentResponse>> getLatestEnvironment(
            @PathVariable("factoryCode") String factoryCode) {
        FactoryEnvironmentResponse response = factoryEnvironmentService.getLatestReading(factoryCode);
        return ResponseEntity.ok(BaseResponse.of(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "공장 온습도 이력 조회")
    @GetMapping("/{factoryCode}/environment")
    public ResponseEntity<BaseResponse<List<FactoryEnvironmentResponse>>> getEnvironmentHistory(
            @PathVariable("factoryCode") String factoryCode) {
        List<FactoryEnvironmentResponse> responses =
                factoryEnvironmentService.getReadings(factoryCode);
        return ResponseEntity.ok(BaseResponse.of(HttpStatus.OK.value(), responses));
    }
}
