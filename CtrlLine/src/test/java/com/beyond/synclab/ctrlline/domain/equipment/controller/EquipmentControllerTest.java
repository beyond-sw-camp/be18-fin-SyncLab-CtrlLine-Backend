package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.UpdateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
// addFilters = true로 둬야함.
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 현원에몽이 WithCustomUser와 WithCustiomUserSecurityContextFactory 추가로 작성해서 해결됨.
    // 설비 등록
    // 201
    @Test
    @DisplayName("설비 등록 성공 - 201 CREATED 반환")
    @WithCustomUser(username = "admin", roles = {"ADMIN"})
    void registerEquipment_success() throws Exception {
        // given
        CreateEquipmentRequestDto requestDto = CreateEquipmentRequestDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .empNo("202511123")
                .isActive(true)
                .build();

        EquipmentResponseDto responseDto = EquipmentResponseDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("각형전지 조립라인")
                .equipmentType("생산설비")
                .equipmentPpm(new BigDecimal("35"))
                .userName("김철수")
                .userDepartment("영업 2팀")
                .empNo("202511123")
                .isActive(true)
                .build();

        // when
        when(equipmentService.register(any(Users.class), any(CreateEquipmentRequestDto.class)))
                .thenReturn(responseDto);

        // then
        mockMvc.perform(post("/api/v1/equipments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(jsonPath("$.data.equipmentCode").value("EQP-0001"))
                .andExpect(jsonPath("$.data.equipmentName").value("각형전지 조립라인"))
                .andExpect(jsonPath("$.data.equipmentType").value("생산설비"))
                .andExpect(jsonPath("$.data.equipmentPpm").value(35))
                .andExpect(jsonPath("$.data.userName").value("김철수"))
                .andExpect(jsonPath("$.data.userDepartment").value("영업 2팀"))
                .andExpect(jsonPath("$.data.empNo").value("202511123"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    // 400
    @Test
    @DisplayName("설비 등록 실패 - 설비 코드 누락 시 400 BAD_REQUEST 반환")
    @WithCustomUser(username = "admin", roles = {"ADMIN"})
    void registerEquipment_fail_invalidRequest() throws Exception {
        // given : 필수값 누락
        CreateEquipmentRequestDto invalidRequest = CreateEquipmentRequestDto.builder()
                .equipmentCode(null) // 설비 코드 누락
                .equipmentName("생산설비")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/equipments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // 설비 상세 조회
    // 200
    @Test
    @DisplayName("설비코드로 상세 조회 호출에 성공한다.")
    void getEquipmentDetail_success() throws Exception {
        // given
        EquipmentDetailResponseDto dto = EquipmentDetailResponseDto.builder()
                .equipmentCode("EQ001")
                .equipmentName("절단기-01")
                .equipmentType("절단기")
                .equipmentPpm(BigDecimal.valueOf(98.5))
                .userDepartment("생산1팀")
                .userName("홍길동")
                .empNo("A1001")
                .operatingDate(LocalDateTime.of(2025, 11, 10, 9, 0))
                .maintenanceDate(LocalDateTime.of(2025, 11, 9, 9, 0))
                .totalCount(BigDecimal.valueOf(5000))
                .defectiveCount(BigDecimal.valueOf(5))
                .build();

        given(equipmentService.getEquipmentDetail("EQ001"))
                .willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/equipments/EQ001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.equipmentCode", is("EQ001")))
                .andExpect(jsonPath("$.data.equipmentName", is("절단기-01")))
                .andExpect(jsonPath("$.data.userName", is("홍길동")));
    }

    @Test
    @DisplayName("설비 목록 조회 성공")
    @WithCustomUser(username = "user", roles = {"USER"})
    void get_equipment_list_success() throws Exception {
        // given - 서비스가 반환할 가짜 DTO 준비
        EquipmentSearchResponseDto dto1 = EquipmentSearchResponseDto.builder()
                .equipmentCode("EQP-0001")
                .equipmentName("절단기-01")
                .equipmentType("공정설비")
                .userName("홍길동")
                .userDepartment("생산1팀")
                .isActive(true)
                .build();

        EquipmentSearchResponseDto dto2 = EquipmentSearchResponseDto.builder()
                .equipmentCode("EQP-0002")
                .equipmentName("포장기-03")
                .equipmentType("포장설비")
                .userName("이영희")
                .userDepartment("품질팀")
                .isActive(true)
                .build();

        PageResponse<EquipmentSearchResponseDto> pageResponse =
                PageResponse.from(new PageImpl<>(
                        List.of(dto1, dto2),
                        PageRequest.of(0, 10),
                        2
                ));

        // service mock
        Mockito.when(equipmentService.getEquipmentsList(any(), any(), any()))
                .thenReturn(pageResponse);

        // when & then - GET 요청 후 JSON 응답 검증
        mockMvc.perform(get("/api/v1/equipments")
                        .param("equipmentCode", "EQP")   // 검색 조건
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].equipmentCode").value("EQP-0001"))
                .andExpect(jsonPath("$.data.content[1].equipmentCode").value("EQP-0002"));
    }

    // 설비 업데이트
    @Test
    @WithCustomUser(username = "user", roles = {"ADMIN"})
    @DisplayName("ADMIN 역할은 설비를 수정할 수 있다.")
    void updateEquipment_success() throws Exception {

        String equipmentCode = "EQ001";

        // 요청 DTO
        UpdateEquipmentRequestDto request = UpdateEquipmentRequestDto.builder()
                .userName("박민수")
                .isActive(false)
                .build();

        // 응답 DTO
        EquipmentResponseDto response = EquipmentResponseDto.builder()
                .equipmentCode("EQ001")
                .equipmentName("절단기-01")
                .equipmentType("CUTTER")
                .equipmentPpm(BigDecimal.valueOf(210))
                .userName("박민수")
                .userDepartment("생산1팀")
                .empNo("20240001")
                .isActive(false)
                .build();

        // Mocking
        when(equipmentService.updateEquipment(any(Users.class), any(UpdateEquipmentRequestDto.class), eq(equipmentCode)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                patch("/api/v1/equipments/{equipmentCode}", equipmentCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.equipmentCode").value("EQ001"))
                .andExpect(jsonPath("$.data.userName").value("박민수"))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andDo(print());
    }

}
