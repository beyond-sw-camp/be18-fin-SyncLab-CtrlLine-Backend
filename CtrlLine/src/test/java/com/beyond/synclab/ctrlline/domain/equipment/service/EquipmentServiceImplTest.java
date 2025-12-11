package com.beyond.synclab.ctrlline.domain.equipment.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRuntimeStatusLevel;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentStatusResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.UpdateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.equipmentstatus.entity.EquipmentStatuses;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceImplTest {

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EquipmentRuntimeStatusService equipmentRuntimeStatusService;


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

    @Test
    @DisplayName("설비코드가 중복되면 등록에 실패한다.")
    void createEquipment_fail_duplicateEquipmentCode() {
        // given
        Users user = buildTestUser("홍길동", Users.UserRole.ADMIN);

        CreateEquipmentRequestDto requestDto = CreateEquipmentRequestDto.builder()
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
        EquipmentStatuses status = EquipmentStatuses.builder()
                .equipmentStatusCode("RUNNING")
                .equipmentStatusName("가동중")
                .build();

        Lines line = Lines.builder()
                .lineCode("L001")
                .lineName("1라인")
                .build();

        Equipments equipment = Equipments.builder()
                .id(1L)
                .equipmentCode("E001")    // <--- 테스트 Assertion과 일치해야 함
                .equipmentName("절단기-01")
                .equipmentType("Type-A")
                .equipmentPpm(BigDecimal.ZERO)
                .user(user)
                .equipmentStatus(status)
                .line(line)
                .isActive(true)
                .totalCount(BigDecimal.ZERO)
                .defectiveCount(BigDecimal.ZERO)
                .build();

        when(equipmentRepository.findByEquipmentCode("E001"))
                .thenReturn(Optional.of(equipment));
        when(equipmentRuntimeStatusService.getLevelOrDefault("E001"))
                .thenReturn(EquipmentRuntimeStatusLevel.RUNNING);

        // when
        var result = equipmentService.getEquipmentDetail("E001");

        // then
        assertThat(result.getEquipmentCode()).isEqualTo("E001");
        assertThat(result.getEquipmentName()).isEqualTo("절단기-01");
        assertThat(result.getUserName()).isEqualTo("홍길동");
        assertThat(result.getEquipmentStatusCode()).isEqualTo("RUNNING");
        assertThat(result.getLineCode()).isEqualTo("L001");
    }

    // 설비 목록 조회
    @Test
    @DisplayName("설비 목록 조회. 1페이지에, 설비 10개를 보여줌.")
    void success_get_equipment_list() {

        // given
        Users user = buildTestUser("홍길동", Users.UserRole.USER);
        EquipmentSearchDto searchDto = EquipmentSearchDto.builder()
                .build();

        // 한 페이지에 10개씩 조회됨. 현재 페이지는 0번
        Pageable pageable = PageRequest.of(0, 10);

        Equipments equipment1 = Equipments.builder()
                .equipmentCode("EQP-0001")
                .user(user)
                .build();

        Equipments equipment2 = Equipments.builder()
                .equipmentCode("EQP-0002")
                .user(user)
                .build();

        Page<Equipments> page = new PageImpl<>(
                List.of(equipment1, equipment2),
                pageable,
                2
        );

        Mockito.when(equipmentRepository.searchEquipmentList(searchDto, pageable))
                .thenReturn(page);
        Mockito.when(equipmentRuntimeStatusService.getLevelOrDefault("EQP-0001"))
                .thenReturn(EquipmentRuntimeStatusLevel.RUNNING);
        Mockito.when(equipmentRuntimeStatusService.getLevelOrDefault("EQP-0002"))
                .thenReturn(EquipmentRuntimeStatusLevel.STOPPED);

        // when
        PageResponse<EquipmentSearchResponseDto> response =
                equipmentService.getEquipmentsList(user, searchDto, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getEquipmentCode()).isEqualTo("EQP-0001");
        assertThat(response.getContent().get(1).getEquipmentCode()).isEqualTo("EQP-0002");
        assertThat(response.getPageInfo().getCurrentPage()).isEqualTo(1);


        Mockito.verify(equipmentRepository, times(1))
                .searchEquipmentList(searchDto, pageable);
    }

    @Test
    @DisplayName("업데이트 했을 때, 다른 값들은 그대로 반환된다.")
    void updateEquipment_changeOnlyManager_otherValuesRemain() {

        // given: 관리자
        Users admin = Users.builder()
                .name("관리자")
                .empNo("M001")
                .role(Users.UserRole.ADMIN)
                .build();

        // 기존 담당자
        Users oldManager = Users.builder()
                .name("이인화")
                .department("생산 1팀")
                .empNo("M001")
                .build();

        // 새로운 담당자
        Users newManager = Users.builder()
                .name("박민수")
                .department("관리팀")
                .role(Users.UserRole.USER)
                .empNo("20230001")
                .build();

        String equipmentCode = "EQ002";

        Equipments equipment = Equipments.builder()
                .equipmentCode(equipmentCode)
                .equipmentName("포장기-01")
                .equipmentType("PACK")
                .equipmentPpm(BigDecimal.valueOf(210))
                .user(oldManager)
                .isActive(true)
                .build();

        UpdateEquipmentRequestDto request = UpdateEquipmentRequestDto.builder()
                .userName("박민수")
                .empNo("20230001")
                .isActive(false)
                .build();

        when(equipmentRepository.findByEquipmentCode(equipmentCode))
                .thenReturn(Optional.of(equipment));

        when(userRepository.findByEmpNo("20230001"))
                .thenReturn(Optional.of(newManager));

        // when
        CreateEquipmentResponseDto response =
                equipmentService.updateEquipment(admin, request, equipmentCode);

        // then
        assertNotNull(response);
        assertEquals("박민수", response.getUserName());
        assertEquals(false, response.getIsActive(), "isActive는 기존 값 유지");
        assertEquals("포장기-01", response.getEquipmentName());
        assertEquals("PACK", response.getEquipmentType());
        assertEquals(BigDecimal.valueOf(210), response.getEquipmentPpm());
    }

    @Test
    @DisplayName("전 설비 상태를 조회하면 runtimeStatusLevel을 함께 반환한다.")
    void getEquipmentStatuses_returnsLevels() {
        Equipments equipment = Equipments.builder()
                .equipmentCode("EQ-ALL-01")
                .equipmentName("성형기-01")
                .build();
        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        when(equipmentRuntimeStatusService.getLevelOrDefault("EQ-ALL-01"))
                .thenReturn(EquipmentRuntimeStatusLevel.LOW_WARNING);

        List<EquipmentStatusResponseDto> result = equipmentService.getEquipmentStatuses(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).runtimeStatusLevel()).isEqualTo(EquipmentRuntimeStatusLevel.LOW_WARNING);
        assertThat(result.get(0).equipmentCode()).isEqualTo("EQ-ALL-01");
    }
}
