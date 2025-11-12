package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceImplTest {

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;


    // ===== 테스트용 유저 빌더 =====
    private Users buildTestUser(String name, Users.UserRole role) {
        return Users.builder()
                .name(name)
                .empNo("202411001")
                .email("hong@test.com")
                .password("1234")
                .status(Users.UserStatus.ACTIVE)
                .phoneNumber("010-1234-5678")
                .address("서울시 금천구")
                .department("생산1팀")
                .position(Users.UserPosition.DIRECTOR)
                .role(role)
                .hiredDate(LocalDate.of(2025, 10, 20))
                .build();
    }

    // ===== 테스트용 설비 빌더 =====
    private Equipments buildTestEquipment(Users user, boolean isActive) {
        return Equipments.builder()
                .equipmentCode("E001")
                .equipmentName("절단기-01")
                .equipmentType("절단기")
                .users(user)
                .equipmentPpm(BigDecimal.valueOf(108))
                .isActive(isActive)
                .build();
    }

    @Test
    @DisplayName("설비코드가 중복되면 등록에 실패한다.")
    void createEquipment_fail_duplicateEquipmentCode() {
        // given
        Users user = buildTestUser("홍길동", Users.UserRole.ADMIN);

        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
                .equipmentCode("E001")
                .equipmentName("절단기-01")
                .equipmentPpm(BigDecimal.valueOf(108))
                .empNo(user.getEmpNo())
                .isActive(true)
                .build();

        // when
        when(equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode()))
                .thenReturn(true);

        // then
        assertThatThrownBy(() -> equipmentService.register(user, requestDto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 존재하는 설비코드입니다.");

    }

//    @Test
//    @DisplayName("필수 입력값이 누락되었습니다.")
//    void createEquipment_fail_missingFields(){
//        // given
//        Users user = buildTestUser("홍길동", Users.UserRole.ADMIN);
//        EquipmentRegisterRequestDto invalidRequest = EquipmentRegisterRequestDto.builder()
//                .equipmentCode(null) // 누락
//                .equipmentName("절단기-01")
//                .equipmentType("절단기")
//                .equipmentPpm(BigDecimal.valueOf(108))
//                .empNo(user.getEmpNo())
//                .isActive(true)
//                .build();
//
//        // then
//        assertThatThrownBy(() -> equipmentService.register(user, invalidRequest))
//                .isInstanceOf(AppException.class)
//                .hasMessageContaining("필수 입력 값이 누락되었거나 요청 형식이 올바르지 않습니다.");
//    }
//
//    @Test
//    @DisplayName("사번이 존재하지 않으면 등록에 실패한다.")
//    void createEquipment_fail_userNotFound() {
//        // given
//        Users user = buildTestUser("홍길동", Users.UserRole.ADMIN);
//        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
//                .equipmentCode("E001")
//                .equipmentName("절단기-01")
//                .equipmentType("절단기")
//                .equipmentPpm(BigDecimal.valueOf(108))
//                .empNo("999999999") // 존재하지 않는 사번
//                .isActive(true)
//                .build();
//
//        // when
//        when(equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode()))
//                .thenReturn(false);
//        when(userRepository.findByEmpNo(requestDto.getEmpNo()))
//                .thenReturn(Optional.empty());
//
//        // then
//        assertThatThrownBy(() -> equipmentService.register(user, requestDto))
//                .isInstanceOf(AppException.class)
//                .hasMessageContaining("해당 사용자를 찾을 수 없습니다.");
//    }

//    @Test
//    @DisplayName("관리자 권한이 아닌 사용자가 등록을 시도하면 실패한다.")
//    void createEquipment_fail_unauthorized() {
//        // given
//        Users normalUser = buildTestUser("김철수", Users.UserRole.USER);
//        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
//                .equipmentCode("E001")
//                .equipmentName("절단기-01")
//                .equipmentType("절단기")
//                .equipmentPpm(BigDecimal.valueOf(108))
//                .empNo(normalUser.getEmpNo())
//                .isActive(true)
//                .build();
//
//        // when
//        when(equipmentRepository.existsByEquipmentCode(requestDto.getEquipmentCode()))
//                .thenReturn(false);
//        when(userRepository.findByEmpNo(requestDto.getEmpNo()))
//                .thenReturn(Optional.of(normalUser));
//
//        // then
//        assertThatThrownBy(() -> equipmentService.register(normalUser, requestDto))
//                .isInstanceOf(AppException.class)
//                .hasMessageContaining("관리자 권한이 아닙니다.");
//    }

}
