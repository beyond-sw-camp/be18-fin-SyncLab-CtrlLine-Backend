package com.beyond.synclab.ctrlline.domain.line.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.line.dto.LineResponseDto;
import com.beyond.synclab.ctrlline.domain.line.dto.LineSearchCommand;
import com.beyond.synclab.ctrlline.domain.line.service.LineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/lines")
@RequiredArgsConstructor
public class LineController {
    private final LineService lineService;

    @GetMapping("/{lineCode}")
    public ResponseEntity<BaseResponse<LineResponseDto>> getLine(@PathVariable("lineCode") String lineCode) {

        LineResponseDto responseDto = lineService.getLine(lineCode);

        BaseResponse<LineResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<LineResponseDto>>> getLineList(
            @ModelAttribute LineSearchCommand lineSearchCommand,
            @PageableDefault(sort = "lineCode", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<LineResponseDto> users = lineService.getLineList(lineSearchCommand, pageable);
        return ResponseEntity.ok(BaseResponse.ok(PageResponse.from(users)));
    }

}
