package com.beyond.synclab.ctrlline.domain.telemetry.controller;

import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryProgressDto;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.LineProgressDto;
import com.beyond.synclab.ctrlline.domain.telemetry.service.LineFinalInspectionProgressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/production-progress")
@RequiredArgsConstructor
public class ProductionProgressController {

    private final LineFinalInspectionProgressService progressService;

    @GetMapping("/lines")
    public ResponseEntity<List<LineProgressDto>> getLineProgress(@RequestParam(required = false) String factoryCode) {
        List<LineProgressDto> progress = progressService.listProgress(factoryCode);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/factories")
    public ResponseEntity<List<FactoryProgressDto>> getFactoryProgress(
            @RequestParam(required = false) String factoryCode
    ) {
        List<FactoryProgressDto> progress = progressService.listFactoryProgress(factoryCode);
        return ResponseEntity.ok(progress);
    }
}
