package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentListResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface EquipmentService {

    EquipmentRegisterResponseDto register(Users users, EquipmentRegisterRequestDto requestDto);

    Page<EquipmentListResponseDto> getEquipments(PageRequest pageRequest);
}
