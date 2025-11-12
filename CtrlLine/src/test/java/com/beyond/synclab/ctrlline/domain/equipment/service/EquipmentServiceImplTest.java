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
import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    @DisplayName("설비 ID로 상세 조회에 성공한다.")
    void getEquipmentDetail_success() {
        // given
        Users user = buildTestUser("홍길동", Users.UserRole.ADMIN);
        Equipments equipment = buildTestEquipment(user, true);

        when(equipmentRepository.findByEquipmentCode("E001"))
                .thenReturn(Optional.of(equipment));

        // when
        var result = equipmentService.getEquipmentDetail("E001");

        // then
        assertThat(result.getEquipmentCode()).isEqualTo("E001");
        assertThat(result.getEquipmentName()).isEqualTo("절단기-01");
        assertThat(result.getUserName()).isEqualTo("홍길동");
    }

}
