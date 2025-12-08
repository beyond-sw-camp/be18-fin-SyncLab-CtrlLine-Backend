package com.beyond.synclab.ctrlline.domain.serial.controller;

import com.beyond.synclab.ctrlline.domain.serial.dto.response.GetLotSerialListResponseDto;
import com.beyond.synclab.ctrlline.domain.serial.service.SerialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lots")
public class SerialController {

    private final SerialService serialService;

    @GetMapping("/{lotId}/serials")
    public GetLotSerialListResponseDto getSerialList(
            @PathVariable Long lotId
    ) {
        return serialService.getSerialListByLotId(lotId);
    }
}
