package com.beyond.synclab.ctrlline.domain.serial.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.serial.dto.response.GetLotSerialListResponseDto;
import com.beyond.synclab.ctrlline.domain.serial.service.SerialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lots")
public class SerialController {

    private final SerialService serialService;

    @GetMapping("/{lotId}/serials")
    public ResponseEntity<BaseResponse<GetLotSerialListResponseDto>> getSerialList(
            @PathVariable Long lotId
    ) {
        GetLotSerialListResponseDto responseDto =  serialService.getSerialListByLotId(lotId);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }
}
