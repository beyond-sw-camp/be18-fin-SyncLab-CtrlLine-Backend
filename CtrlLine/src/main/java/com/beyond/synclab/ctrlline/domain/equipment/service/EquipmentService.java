package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface EquipmentService {

    // 설비 등록
    EquipmentRegisterResponseDto register(Users users, EquipmentRegisterRequestDto requestDto);

    // 설비 상세 조회
    EquipmentDetailResponseDto getEquipmentDetail(String equipmentCode);


    // Page<EquipmentListResponseDto> getEquipments(PageRequest pageRequest);
}
