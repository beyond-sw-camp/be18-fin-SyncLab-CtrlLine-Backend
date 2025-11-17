package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
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

import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;


    // 설비 등록
    public EquipmentRegisterResponseDto register(Users users, EquipmentRegisterRequestDto requestDto) {

        // (1) 설비코드 중복 검사 (409)
        if (equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode())) {
            throw new AppException(EquipmentErrorCode.EQUIPMENT_CONFLICT);
        }

        Lines line = lineRepository.findById(requestDto.getLine())
                .orElseThrow(() -> new AppException(LineErrorCode.LINE_NOT_FOUND));

        EquipmentStatuses status = equipmentStatusRepository.findByequipmentStatusCode(requestDto.getEquipmentStatus())
                .orElseThrow(() -> new AppException(EquipmentStatusErrorCode.EQUIPMENT_STATUS_NOT_FOUND));

        // (2) 사용자 존재 여부 검사 (404)
        Users user = userRepository.findByEmpNo(requestDto.getEmpNo())
                .orElseThrow(() -> new AppException(CommonErrorCode.USER_NOT_FOUND));

        // 설비 엔티티 생성
        Equipments equipments = requestDto.toEntity(user, line, status);

        // DB 저장
        equipmentRepository.save(equipments);

        return EquipmentRegisterResponseDto.fromEntity(equipments, user);
    }

    // 설비 상세 조회
    @Override
    public EquipmentDetailResponseDto getEquipmentDetail(String equipmentCode){
        Equipments equipment = equipmentRepository.findByEquipmentCode(equipmentCode)
                .orElseThrow(() -> new AppException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));
        Users user = equipment.getUser();
        return EquipmentDetailResponseDto.fromEntity(equipment, user);
    }

    // 설비 목록 조회
    @Override
    @Transactional(readOnly = true)
    public PageResponse<EquipmentSearchResponseDto> getEquipmentsList(Users users, EquipmentSearchDto searchDto, Pageable pageable) {
        Page<Equipments> page = equipmentRepository.searchEquipmentList(searchDto, pageable);

        Page<EquipmentSearchResponseDto> dtoPage = page.map(equipment ->
                EquipmentSearchResponseDto.fromEntity(equipment, equipment.getUser())
        );
        return PageResponse.from(dtoPage);
    }

}