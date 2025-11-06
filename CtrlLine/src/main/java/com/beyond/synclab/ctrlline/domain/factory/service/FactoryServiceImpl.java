package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FactoryServiceImpl implements FactoryService {
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    public FactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto) {

        if(user.getRole() != Users.UserRole.ADMIN) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        if(factoryRepository.findByFactoryCode(requestDto.getFactoryCode()).isPresent()) {
            throw new AppException(CommonErrorCode.FACTORY_CONFLICT);
        }

        Users manager = userRepository.findByEmpNo(requestDto.getEmpNo())
                                      .orElseThrow(()-> new AppException(CommonErrorCode.USER_NOT_FOUND));

        Factories factory = requestDto.toEntity(manager);

        factoryRepository.save(factory);

        return FactoryResponseDto.fromEntity(factory, manager);
    }

    @Override
    public FactoryResponseDto getFactory(String factoryCode) {

        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                                             .orElseThrow(() -> new AppException(CommonErrorCode.FACTORY_NOT_FOUND));

        return FactoryResponseDto.fromEntity(factory, factory.getUsers());
    }


    @Override
    @Transactional
    public FactoryResponseDto updateFactoryStatus(Users user, UpdateFactoryRequestDto request,
                                                  String factoryCode) {

        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                                             .orElseThrow(() -> new AppException(CommonErrorCode.FACTORY_NOT_FOUND));

        if(user.getRole() != Users.UserRole.ADMIN) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        factory.updateStatus(request.isActive());

        return FactoryResponseDto.fromEntity(factory, factory.getUsers());
    }
}
