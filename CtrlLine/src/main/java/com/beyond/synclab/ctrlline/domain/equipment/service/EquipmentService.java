package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public EquipmentRegisterResponseDto register(EquipmentRegisterRequestDto requestDto) {

        // (1) 설비코드 중복검사
        if (equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode())) {
            throw new IllegalStateException("이미 존재하는 설비 코드입니다.");
        }

        // (2) 인증 토큰 유효성 확인
        if (requestDto.getEmpNo() == null || requestDto.getEmpNo().isEmpty()) {
            throw new IllegalStateException("인증 토큰이 없거나 유효하지 않습니다.");
        }

        // (3) 사번 중복 검사
        if(userRepository.existsByEmpNo(requestDto.getEmpNo())){
            throw new IllegalStateException("이미 등록된 사번입니다.");
        }

        // (4) 사용자 조회
        Users user = userRepository.findByEmpNo(requestDto.getEmpNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사번입니다."));

        // (3) 설비 엔티티 생성 및 저장
        Equipments equipment = Equipments.builder()
                .equipmentCode(requestDto.getEquipmentCode())
                .equipmentName(requestDto.getEquipmentName())
                .equipmentType(requestDto.getEquipmentType())
                .equipmentPpm(requestDto.getEquipmentPpm())
                .userId(user.getId())
                //         private String userName;
                //        private String userDepartment;
                //        private String empNo;
                // .userId(requestDto.getUserId()) // FK 연결
                .isActive(requestDto.getIsActive())
                .build();

        Equipments saved = equipmentRepository.save(equipment);

        // (4) 응답 DTO 생성
        return EquipmentRegisterResponseDto.builder()
                .equipmentCode(saved.getEquipmentCode())
                .equipmentName(saved.getEquipmentName())
                .equipmentType(saved.getEquipmentType())
                .equipmentPpm(saved.getEquipmentPpm())
                .userName(user.getName())
                .userDepartment(user.getDepartment())
                .empNo(user.getEmpNo())
                .isActive(saved.getIsActive())
                .build();
    }
}
