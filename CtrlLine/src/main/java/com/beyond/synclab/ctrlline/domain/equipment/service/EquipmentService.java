package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {

    // 설비 등록
    EquipmentRegisterResponseDto register(Users users, EquipmentRegisterRequestDto requestDto);

    // 설비 상세 조회
    EquipmentDetailResponseDto getEquipmentDetail(String equipmentCode);

    // 설비 목록 조회
    // PageResponse를 반환해야하는데, Page로 적어서. 충돌 오류 발생했었음..
    PageResponse<EquipmentSearchResponseDto> getEquipmentsList(Users users, EquipmentSearchDto searchDto, Pageable pageable);

}
