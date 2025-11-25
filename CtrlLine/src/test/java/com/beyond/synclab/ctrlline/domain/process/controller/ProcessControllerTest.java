package com.beyond.synclab.ctrlline.domain.process.controller;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.process.dto.ProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.SearchProcessResponseDto;
import com.beyond.synclab.ctrlline.domain.process.dto.UpdateProcessRequestDto;
import com.beyond.synclab.ctrlline.domain.process.service.ProcessService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessService processService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 공정 상세 조회
    @Test
    @DisplayName("공정코드로 상세 조회 호출에 성공한다.")
    void getProcess_success() throws Exception {
        // given
        ProcessResponseDto responseDto = ProcessResponseDto.builder()
                .equipmentCode("EQ-001")
                .processCode("P001")
                .processName("테스트공정")
                .userDepartment("생산팀")
                .userName("홍길동")
                .empNo("2025001")
                .isActive(true)
                .updatedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        given(processService.getProcess(anyString()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/processes/P001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processCode").value("P001"))
                .andExpect(jsonPath("$.data.processName").value("테스트공정"))
                .andExpect(jsonPath("$.data.userName").value("홍길동"))
                .andExpect(jsonPath("$.data.userDepartment").value("생산팀"))
                .andExpect(jsonPath("$.data.empNo").value("2025001"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.updatedAt").value("2025-01-01T12:00:00"));
    }

    // 공정 업데이트 테스트 코드
    @Test
    @WithCustomUser(username =  "user", roles = {"ADMIN"})
    @DisplayName("ADMIN 역할은 공정을 수정할 수 있다.")
    void updateEquipment_success() throws Exception {
        String processCode = "P001";

        UpdateProcessRequestDto request = UpdateProcessRequestDto.builder()
                .userName("홍길동")
                .isActive(false)
                .build();

        ProcessResponseDto response = ProcessResponseDto.builder()
                .equipmentCode("E001")
                .processCode("P001")
                .processName("테스트공정")
                .userDepartment("생산팀")
                .userName("홍길동")
                .empNo("2025001")
                .isActive(true)
                .updatedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        when(processService.updateProcess(
                any(Users.class), any(UpdateProcessRequestDto.class), eq(processCode))
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/processes/{processCode}", processCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processCode").value("P001"))
                .andExpect(jsonPath("$.data.equipmentCode").value("E001"))
                .andExpect(jsonPath("$.data.processName").value("테스트공정"))
                .andExpect(jsonPath("$.data.userName").value("홍길동"))
                .andExpect(jsonPath("$.data.userDepartment").value("생산팀"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("공정 목록 조회 성공")
    @WithCustomUser(username = "김영업", roles = {"USER"})
    void get_process_list_success() throws Exception {
        SearchProcessResponseDto dto1 = SearchProcessResponseDto.builder()
                .processCode("PRO001")
                .processName("절단공정")
                .userDepartment("영업팀")
                .userName("김영업")
                .empNo("2025001")
                .isActive(true)
                .build();

        SearchProcessResponseDto dto2 = SearchProcessResponseDto.builder()
                .processCode("PRO002")
                .processName("식각공정")
                .userDepartment("설비팀")
                .userName("나설비")
                .empNo("2025002")
                .isActive(false)
                .build();

        PageResponse<SearchProcessResponseDto> pageResponse =
                PageResponse.from(new PageImpl<>(
                        List.of(dto1, dto2),
                        PageRequest.of(0,10),
                        2
                ));

        Mockito.when(processService.getProcessList(any(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/processes")
                .param("processCode", "PRO")
                .param("page", "0")
                .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].processCode").value("PRO001"))
                .andExpect(jsonPath("$.data.content[1].processCode").value("PRO002"));
    }
}
