package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.UpdateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {

    // 설비 등록
    EquipmentResponseDto register(Users users, CreateEquipmentRequestDto requestDto);

    // 설비 상세 조회
    ProcessResponseDto getEquipmentDetail(String equipmentCode);

    // 설비 목록 조회
    // PageResponse를 반환해야하는데, Page로 적어서. 충돌 오류 발생했었음..
    PageResponse<EquipmentSearchResponseDto> getEquipmentsList(Users users, EquipmentSearchDto searchDto, Pageable pageable);

    // 설비 업데이트
    EquipmentResponseDto updateEquipment(Users users, UpdateEquipmentRequestDto request, String equipmentCode);
}
