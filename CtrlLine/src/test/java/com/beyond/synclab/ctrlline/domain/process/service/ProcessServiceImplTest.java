package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProcessServiceImplTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProcessServiceImpl processService;


    @Test
    @DisplayName("공정 상세 조회 성공")
    void Sucess_getProcess() {
    // given
    Processes process = Processes.builder()
            .id(1L)
            .equipmentId(10L)
            .userId(20L)
            .processCode("P001")
            .processName("테스트공정")
            .updatedAt(LocalDateTime.of(2025,1,1,12,0,0))
            .isActive(true)
            .build();

    Equipments equipment = Equipments.builder()
            .id(10L)
            .equipmentCode("EQ-001")
            .build();

    Users user = Users.builder()
            .id(20L)
            .name("홍길동")
            .department("생산팀")
            .empNo("2025001")
            .build();

    given(processRepository.findByProcessCode("P001"))
            .willReturn(Optional.of(process));
    given(equipmentRepository.findById(10L))
            .willReturn(Optional.of(equipment));
    given(userRepository.findById(20L))
            .willReturn(Optional.of(user));

    // when
    ProcessResponseDto result = processService.getProcess("P001");

    // then
        assertThat(result.getProcessCode()).isEqualTo("P001");
        assertThat(result.getEquipmentCode()).isEqualTo("EQ-001");
        assertThat(result.getUserDepartment()).isEqualTo("생산팀");
        assertThat(result.getProcessName()).isEqualTo("테스트공정");
        assertThat(result.getUserName()).isEqualTo("홍길동");
        assertThat(result.getEmpNo()).isEqualTo("2025001");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025,1,1,12,0));
}

    @Test
    @DisplayName("없는 공정 코드로 조회 시, 실패.")
    void getProcess_PROCESS_NOT_FOUND() {
        given(processRepository.findByProcessCode("P001"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> processService.getProcess("P001"))
                .isInstanceOf(AppException.class)
                .hasMessage(ProcessErrorCode.PROCESS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("해당하는 설비가 없을 때, 조회 실패.")
    void getProcess_PROCESS_EQUIPMENT_NOT_FOUND() {
        // given
        Processes process = Processes.builder()
                .processCode("P001")
                .equipmentId(999L)
                .build();

        given(processRepository.findByProcessCode("P001"))
            .willReturn(Optional.of(process));

        given(equipmentRepository.findById(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> processService.getProcess("P001"))
                .isInstanceOf(AppException.class)
                .hasMessage(ProcessErrorCode.PROCESS_EQUIPMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("설비 없음 → PROCESS_EQUIPMENT_NOT_FOUND")
    void fail_processEquipmentNotFound() {

        // given
        Processes process = Processes.builder()
                .processCode("P001")
                .equipmentId(999L)
                .build();

        given(processRepository.findByProcessCode("P001"))
                .willReturn(Optional.of(process));

        given(equipmentRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> processService.getProcess("P001"))
                .isInstanceOf(AppException.class)
                .hasMessage(ProcessErrorCode.PROCESS_EQUIPMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사원 없음 -> USER_NOT_FOUND")
    void fail_userNotFound() {
        // given
        Processes process = Processes.builder()
                .processCode("P001")
                .equipmentId(1L)
                .userId(10L)
                .build();

        Equipments equipment = Equipments.builder()
                .id(1L)
                .build();

        given(processRepository.findByProcessCode("P001"))
                .willReturn(Optional.of(process));

        given(equipmentRepository.findById(1L))
                .willReturn(Optional.of(equipment));

        given(userRepository.findById(10L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> processService.getProcess("P001"))
                .isInstanceOf(AppException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

}
