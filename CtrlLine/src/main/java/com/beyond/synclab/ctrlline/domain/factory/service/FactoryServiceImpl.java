package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryCreateResponseDto;

public class FactoryServiceImpl implements FactoryService {
    public FactoryCreateResponseDto createFactory(FactoryCreateRequestDto requestDto) {

        return FactoryCreateResponseDto.builder()
                                       .build();
    }
}
