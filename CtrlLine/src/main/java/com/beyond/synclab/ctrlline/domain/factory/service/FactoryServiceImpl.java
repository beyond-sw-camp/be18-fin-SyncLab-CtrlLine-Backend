package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FactoryServiceImpl implements FactoryService {
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    public FactoryResponseDto createFactory(Users user, CreateFactoryRequestDto requestDto) {

        if(factoryRepository.findByFactoryCodeAndIsActiveTrue(requestDto.getFactoryCode()).isPresent()) {
            throw new AppException(FactoryErrorCode.FACTORY_CONFLICT);
        }

        Users manager = userRepository.findByEmpNo(requestDto.getEmpNo())
                                      .orElseThrow(()-> new AppException(CommonErrorCode.USER_NOT_FOUND));

        Factories factory = requestDto.toEntity(manager);

        factoryRepository.save(factory);

        return FactoryResponseDto.fromEntity(factory, manager);
    }

    @Override
    @Transactional(readOnly = true)
    public FactoryResponseDto getFactory(String factoryCode) {

        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                                             .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));

        return FactoryResponseDto.fromEntity(factory, factory.getUsers());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FactoryResponseDto> getFactoryList(Users user, FactorySearchDto searchDto, Pageable pageable) {
        Page<Factories> page = factoryRepository.searchFactoryList(searchDto, pageable);

        Page<FactoryResponseDto> dtoPage = page.map(factory ->
                                                            FactoryResponseDto.fromEntity(factory, factory.getUsers())
        );

        return PageResponse.from(dtoPage);
    }


    @Override
    @Transactional
    public FactoryResponseDto updateFactoryStatus(Users user, UpdateFactoryRequestDto request,
                                                  String factoryCode) {

        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                                             .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));


        factory.updateStatus(request.isActive());

        return FactoryResponseDto.fromEntity(factory, factory.getUsers());
    }
}
