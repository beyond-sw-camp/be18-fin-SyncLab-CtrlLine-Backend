package com.beyond.synclab.ctrlline.domain.process.service;

import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchDto;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.process.errorcode.ProcessErrorCode;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessQueryRepository;
import com.beyond.synclab.ctrlline.domain.process.repository.ProcessRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
    void success_getProcess() {
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
    @DisplayName("공정에 해당하는 설비 없음 → PROCESS_EQUIPMENT_NOT_FOUND")
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

    // 공정 업데이트 테스트 코드
    @Test
    @DisplayName("공정을 정상적으로 업데이트한다.")
    void updateProcess_success() {
        // 로그인 유저
        Users loginUser = Users.builder()
                .name("이인화")
                .role(Users.UserRole.ADMIN)
                .build();

        // 기존 공정의 담당자
        Users oldManager = Users.builder()
                .empNo("M100")
                .name("기존담당자")
                .build();

        // 새로운 담당자
        Users newManager = Users.builder()
                .empNo("M200")
                .name("새담당자")
                .build();

        // 설비
        Equipments equipment = Equipments.builder()
                .equipmentCode("EQ-01")
                .equipmentName("설비A")
                .build();

        // 기존 공정 상태
        Processes process = Processes.builder()
                .processCode("P001")
                .equipment(equipment)
                .user(oldManager)  // 기존 담당자
                .isActive(true)
                .build();

        UpdateProcessRequestDto request = UpdateProcessRequestDto.builder()
                .userName("새담당자")
                .empNo("M200")
                .isActive(false)
                .build();

        // 공정 코드로 공정을 찾아옴.
        when(processRepository.findByProcessCode("P001"))
                .thenReturn(Optional.of(process));

        // 사번으로 담당자를 찾음.
        when(userRepository.findByEmpNo("M200"))
                .thenReturn(Optional.of(newManager));

        ProcessResponseDto result = processService.updateProcess(loginUser, request,"P001");

        // then
        assertThat(process.getUser().getEmpNo()).isEqualTo("M200");
        assertThat(result.getProcessCode()).isEqualTo("P001");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getUserName()).isEqualTo("새담당자");
    }

    // 공정 목록 조회
    @Test
    @DisplayName("공정 목록 조회. 1페이지에, 공정 10개를 보여줌.")
    void success_get_process_list() {

        // given
        Users user = Users.builder()
                .id(1L)
                .name("홍길동")
                .department("생산부")
                .empNo("E001")
                .build();

        ProcessSearchDto searchDto = ProcessSearchDto.builder()
                .build();  // 검색 조건 없음 → 전체 조회

        Pageable pageable = PageRequest.of(0, 10);

        Processes process1 = Processes.builder()
                .processCode("PRC-0001")
                .processName("프레스 1라인")
                .isActive(true)
                .user(user)
                .build();

        Processes process2 = Processes.builder()
                .processCode("PRC-0002")
                .processName("프레스 2라인")
                .isActive(true)
                .user(user)
                .build();

        Page<Processes> page = new PageImpl<>(
                List.of(process1, process2),
                pageable,
                2
        );

        Mockito.when(processRepository.searchProcessList(searchDto, pageable))
                .thenReturn(page);

        // when
        PageResponse<ProcessSearchResponseDto> response =
                processService.getProcessList(user, searchDto, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getProcessCode()).isEqualTo("PRC-0001");
        assertThat(response.getContent().get(1).getProcessCode()).isEqualTo("PRC-0002");
        assertThat(response.getPageInfo().getCurrentPage()).isEqualTo(1);

        Mockito.verify(processRepository, times(1))
                .searchProcessList(searchDto, pageable);
    }



}
