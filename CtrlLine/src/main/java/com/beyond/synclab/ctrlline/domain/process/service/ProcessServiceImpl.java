package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessQueryRepository;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final ProcessQueryRepository processQueryRepository;

    // 공정 상세 조회
    // ErrorCode 404, 409
    @Override
    @Transactional(readOnly = true)
    public ProcessResponseDto getProcess(String processCode) {

        // 404 PROCESS_NOT_FOUND
        Processes process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new AppException(ProcessErrorCode.PROCESS_NOT_FOUND));

        // 404 PROCESS_EQUIPMENT_NOT_FOUND
        Equipments equipment = equipmentRepository.findById(process.getEquipmentId())
                .orElseThrow(() -> new AppException(ProcessErrorCode.PROCESS_EQUIPMENT_NOT_FOUND));

        // 404 USER_NOT_FOUND
        Users user = userRepository.findById(process.getUserId())
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        return ProcessResponseDto.fromEntity(process, equipment, user);
    }

    // 공정 업데이트
    @Override
    @Transactional
    public ProcessResponseDto updateProcess(Users user, UpdateProcessRequestDto request, String processCode) {
        Processes process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new AppException(ProcessErrorCode.PROCESS_NOT_FOUND));

        Users newManagerRequested = userRepository.findByEmpNo(request.getEmpNo())
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        // 사원명과 사번 매핑 검증
        if (!newManagerRequested.getName().equals(request.getUserName())) {
            throw new AppException(UserErrorCode.USER_INFO_MISMATCH);
        }

        // 공정 사용여부
        if (request.getIsActive() != null) {
            process.updateStatus(request.getIsActive());
        }

        // 공정 담당자
        if (request.getUserName() != null) {
            process.updateManager(newManagerRequested);
        }
        return ProcessResponseDto.fromEntity(process, process.getEquipment(), process.getUser());
    }

    // 공정 목록 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProcessSearchResponseDto> getProcessList(Users users, ProcessSearchDto searchDto, Pageable pageable) {
        Page<Processes> page = processQueryRepository.searchProcessList(searchDto, pageable);
        Page<ProcessSearchResponseDto> dtoPage = page.map(process ->
                ProcessSearchResponseDto.fromEntity(process, process.getUser())
        );
        return PageResponse.from(dtoPage);
    }
}
