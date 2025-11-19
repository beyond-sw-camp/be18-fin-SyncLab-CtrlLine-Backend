package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

public interface ProcessService {
    // 공정 상세 조회
    ProcessResponseDto getProcess(String processCode);

    // 공정 업데이트
    ProcessResponseDto updateProcess(Users user, UpdateProcessRequestDto request, String ProcessCode);
}
