package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    // 공정 상세 조회
    // ErrorCode 404, 409
    @Override
    public ProcessResponseDto getProcess(String processCode){

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

        // 공정 사용여부
        if (request.getIsActive() != null){
            process.updateStatus(request.getIsActive());
        }

        // 공정 담당자
        if(request.getUserName() != null){
            Users newManager = userRepository.findByEmpNo(request.getEmpNo())
                    .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
            process.updateManager(newManager);
        }
        return ProcessResponseDto.fromEntity(process, process.getEquipment(), process.getUser());
    }
}
