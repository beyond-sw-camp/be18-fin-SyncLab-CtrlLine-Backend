package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

public interface FactoryService {
    FactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto);

    FactoryResponseDto getFactory(String factoryCode);

    FactoryResponseDto updateFactoryStatus(Users user, UpdateFactoryRequestDto request, String factoryCode);
}
