package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryServiceImpl implements FactoryService {
    private final FactoryRepository factoryRepository;

    public CreateFactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto) {

        if(user.getRole() != Users.UserRole.ADMIN) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        if(factoryRepository.findByFactoryCode(requestDto.getFactoryCode()).isPresent()) {
            throw new AppException(CommonErrorCode.FACTORY_CONFLICT);
        }

        Factories factory = requestDto.toEntity(user);

        factoryRepository.save(factory);

        return CreateFactoryResponseDto.fromEntity(factory, user);
    }
}
