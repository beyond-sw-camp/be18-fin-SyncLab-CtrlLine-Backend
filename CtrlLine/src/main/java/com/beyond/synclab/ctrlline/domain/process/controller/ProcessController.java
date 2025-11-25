package com.beyond.synclab.ctrlline.domain.process.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.SearchProcessDto;
import com.beyond.synclab.ctrlline.domain.process.dto.SearchProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.process.service.ProcessService;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    // 공정 업데이트 (공정 담당자, 사용여부만 수정 가능함.)
    @PatchMapping("/{processCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<ProcessResponseDto>> updateProcess(
        @AuthenticationPrincipal CustomUserDetails users,
        @PathVariable String processCode,
        @RequestBody UpdateProcessRequestDto request){
        ProcessResponseDto responseDto = processService.updateProcess(
                users.getUser(), request, processCode);
        return ResponseEntity.ok(BaseResponse.of(HttpStatus.OK.value(), responseDto));
    }

    // 공정 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<SearchProcessResponseDto>>> getProcessList (
          @AuthenticationPrincipal CustomUserDetails user,
          SearchProcessDto searchDto,
          @PageableDefault(size = 10, sort = "processCode", direction = Sort.Direction.ASC)
          Pageable pageable
    ){
        PageResponse<SearchProcessResponseDto> responseDto
                = processService.getProcessList(user.getUser(), searchDto, pageable);
        BaseResponse<PageResponse<SearchProcessResponseDto>> baseResponse = BaseResponse.of(HttpStatus.OK.value(), responseDto);
        return ResponseEntity.ok(baseResponse);
    }

}
