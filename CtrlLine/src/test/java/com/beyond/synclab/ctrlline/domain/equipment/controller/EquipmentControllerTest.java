package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 Security 필터 무시
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentService equipmentService;

    @Test
    @DisplayName("설비 등록 성공 - 201 CREATED 반환")
    void registerEquipment_success() throws Exception {
        // given
        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .userName("김철수")
                .userDepartment("영업 2팀")
                .empNo("0957746KJLY")
                .isActive(false)
                .build();

        EquipmentRegisterResponseDto responseDto = EquipmentRegisterResponseDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .userName("김철수")
                .userDepartment("영업 2팀")
                .empNo("0957746KJLY")
                .isActive(false)
                .build();

        // when
        when(equipmentService.register(any(EquipmentRegisterRequestDto.class)))
                .thenReturn(responseDto);

        // then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipmentCode").value("EQP-0001"))
                .andExpect(jsonPath("$.equipmentName").value("각형전지 조립라인"))
                .andExpect(jsonPath("$.equipmentType").value("생산설비"))
                .andExpect(jsonPath("$.equipmentPpm").value("35"))
                .andExpect(jsonPath("$.userName").value("김철수"))
                .andExpect(jsonPath("$.userDepartment").value("영업 2팀"))
                .andExpect(jsonPath("$.empNo").value("0957746KJLY"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("설비 등록 실패 - 설비 코드 누락, 400 BAD_REQUEST 반환")
    void registerEquipment_fail_invalidRequest() throws Exception {
        // given : 필수값 누락
        EquipmentRegisterRequestDto invalidRequest = EquipmentRegisterRequestDto.builder()
                .equipmentCode(null) //설비 코드 누락의 경우, 설비 등록 실패를 던져야 함.
                .build();

        // then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("설비 등록 실패 - 존재하지 않는 사번, 400 BAD_REQUEST")
    void registerEquipment_fail_nonExistingEmpNo() throws Exception {
        // given
        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .userName("김철수")
                .userDepartment("영업 2팀")
                .empNo("123456")
                .isActive(false)
                .build();

        when(equipmentService.register(any(EquipmentRegisterRequestDto.class)))
                .thenThrow(new IllegalArgumentException("존재하지 않는 사번입니다."));  // 존재하지 않는 사번 예외

        // when & then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())  // 400 Bad Request 응답을 기대
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사번입니다."));
    }

    @Test
    @DisplayName("설비 등록 실패 - 중복 설비 코드 시 409 Conflict")
    void registerEquipment_fail_conflict() throws Exception {
        // given
        EquipmentRegisterRequestDto requestDto = EquipmentRegisterRequestDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .userName("김철수")
                .userDepartment("영업 2팀")
                .empNo("0957746KJLY")
                .isActive(false)
                .build();

        when(equipmentService.register(any(EquipmentRegisterRequestDto.class)))
                .thenThrow(new IllegalArgumentException("이미 존재하는 설비 코드입니다."));  // 중복 설비 코드 예외

        // when & then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())  // 409 Conflict 응답을 기대
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("EQUIPMENT_CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 설비 코드입니다."));
    }

    // @WithMockUser를 아예 제거해도, 401이 아닌 201로 던짐... USER로 수정해도 같음.
    // 401은 기능 작성하면서, 처리 추가하겠습니다.
}