package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.errorcode.EquipmentErrorCode;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.UpdateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.errorcode.EquipmentErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;

import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.errorcode.EquipmentStatusErrorCode;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.repository.EquipmentStatusRepository;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.errorcode.LineErrorCode;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
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

    // 공정 상세 조회
    @Override
    public ProcessResponseDto getProcess(String processCode){
        Processes process = processRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new AppException(ProcessErrorCode.))


    }

}
