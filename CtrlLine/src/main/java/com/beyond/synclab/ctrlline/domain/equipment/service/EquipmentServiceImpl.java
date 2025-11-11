package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentListResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.errorcode.EquipmentErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    // 설비 등록
    public EquipmentRegisterResponseDto register(Users users, EquipmentRegisterRequestDto requestDto) {

        // (1) 설비코드 중복 검사 (409)
        if (equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode())) {
            throw new AppException(EquipmentErrorCode.EQUIPMENT_CONFLICT);
        }

        // (2) 필수 입력값 누락 검사 (400)
        if (requestDto.getEquipmentCode() == null || requestDto.getEquipmentName() == null ||
                requestDto.getEquipmentType() == null || requestDto.getEquipmentPpm() == null ||
                requestDto.getEmpNo() == null) {
            throw new AppException(EquipmentErrorCode.BAD_REQUEST);
        }

        // (3) 사용자 존재 여부 검사 (404)
        Users user = userRepository.findByEmpNo(requestDto.getEmpNo())
                .orElseThrow(() -> new AppException(CommonErrorCode.USER_NOT_FOUND));

        // (4) 관리자가 아닌 경우 등록 제한
        if (user.getRole() != Users.UserRole.ADMIN) {
            throw new AppException(EquipmentErrorCode.UNAUTHORIZED);
        }

        // 설비 엔티티 생성
        Equipments equipments = requestDto.toEntity(user);

        // DB 저장
        equipmentRepository.save(equipments);

        return EquipmentRegisterResponseDto.fromEntity(equipments, user);
    }

    // 설비 목록조회
    @Transactional(readOnly = true)
    @Override
    public Page<EquipmentListResponseDto> getEquipments(PageRequest pageRequest) {
        // TODO: 실제 구현 필요 (지금은 임시로 null 리턴)
        return null;
    }

}