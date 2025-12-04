package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentStatusStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/equipments/status-stream")
@RequiredArgsConstructor
public class EquipmentStatusStreamController {

    private final EquipmentStatusStreamService statusStreamService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam(required = false) String factoryCode,
            @RequestParam(required = false) String lineCode
    ) {
        return statusStreamService.registerEmitter(factoryCode, lineCode);
    }
}
