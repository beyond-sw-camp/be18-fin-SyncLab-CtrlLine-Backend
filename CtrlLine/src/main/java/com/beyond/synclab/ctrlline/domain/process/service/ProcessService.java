package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;

public interface ProcessService {
    // 공정 상세 조회
    ProcessResponseDto getProcess(String processCode);

    // 공정 업데이트
    ProcessResponseDto updateProcess(Users user, UpdateProcessRequestDto request, String processCode);

    // 공정 목록 조회
    PageResponse<ProcessSearchResponseDto> getProcessList(Users users, ProcessSearchDto searchDto, Pageable pageable);
}
