package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;

public interface FactoryService {
    FactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto);

    FactoryResponseDto getFactory(String factoryCode);

    PageResponse<FactoryResponseDto> getFactoryList(Users user, FactorySearchDto searchDto, Pageable pageable);

    FactoryResponseDto updateFactoryStatus(Users user, UpdateFactoryRequestDto request, String factoryCode);

}
