package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;

public interface ProcessService {
    // 설비 상세 조회
    ProcessResponseDto getProcess(String processCode);
}
