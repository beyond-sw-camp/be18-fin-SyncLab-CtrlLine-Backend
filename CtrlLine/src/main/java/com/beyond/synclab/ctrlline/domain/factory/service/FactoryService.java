package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;

public interface FactoryService {
    CreateFactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto);
}
