package com.beyond.synclab.ctrlline.domain.log.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.log.dto.LogListResponseDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogSearchDto;
import com.beyond.synclab.ctrlline.domain.log.service.LogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<LogListResponseDto>>> getLogsList(
        @ModelAttribute LogSearchDto logSearchDto
    ) {
        List<LogListResponseDto> logListResponseDtos = logService.getLogsList(logSearchDto);

        return ResponseEntity.ok(BaseResponse.ok(logListResponseDtos));
    }

}
