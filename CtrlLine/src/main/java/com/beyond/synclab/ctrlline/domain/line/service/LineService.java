package com.beyond.synclab.ctrlline.domain.line.service;

import com.beyond.synclab.ctrlline.domain.line.dto.LineResponseDto;
import com.beyond.synclab.ctrlline.domain.line.dto.LineSearchCommand;
import com.beyond.synclab.ctrlline.domain.line.dto.UpdateLineActRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LineService {
    LineResponseDto getLine(String lineCode);

    Page<LineResponseDto> getLineList(LineSearchCommand lineSearchCommand, Pageable pageable);

    Boolean updateLineAct(UpdateLineActRequestDto request);
}
