package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;

    // 공정 상세 조회
    // ErrorCode 401, 404, 409
    @Override
    public ProcessResponseDto getProcess(String processCode){
        // 401 UNAUTHORIZED

        // 404 PROCESS_NOT_FOUND
        Processes process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new AppException(ProcessErrorCode.PROCESS_NOT_FOUND));

            Equipments  equipment = equipment.getEquipmentCode();
            Users user = process.getUser();

        return ProcessResponseDto.fromEntity(process, equipment, user);

    }

}
