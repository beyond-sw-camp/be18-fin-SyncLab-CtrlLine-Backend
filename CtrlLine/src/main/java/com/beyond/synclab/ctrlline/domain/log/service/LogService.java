package com.beyond.synclab.ctrlline.domain.log.service;


import com.beyond.synclab.ctrlline.domain.log.dto.LogCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogListResponseDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogSearchDto;
import java.util.List;

public interface LogService {

    List<LogListResponseDto> getLogsList(LogSearchDto logSearchDto);

    void createLog(LogCreateRequestDto logCreateRequestDto);
}
