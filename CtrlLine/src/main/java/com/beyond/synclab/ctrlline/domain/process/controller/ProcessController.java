package com.beyond.synclab.ctrlline.domain.process.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor

public class ProcessController {

    private final ProcessService processService;

    // 공정 상세 조회
    @GetMapping("/{processCode}")
    public ResponseEntity<BaseResponse<ProcessResponseDto>> getProcess(
            @PathVariable("processCode") String processCode) {

        ProcessResponseDto responseDto = processService.getProcess(processCode);
        BaseResponse<ProcessResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);
        return ResponseEntity.ok(response);
    }
}
