package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryCreateResponseDto;

public interface FactoryService {
    FactoryCreateResponseDto createFactory(FactoryCreateRequestDto requestDto);
}
