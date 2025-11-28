package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetAllProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanEndTimeResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.GetProductionPlanScheduleResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.UpdateProductionPlanStatusResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.UpdateProductionPlanStatusRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanService;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanServiceImpl;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Import(TestSecurityConfig.class)
@WebMvcTest(ProductionPlanController.class)
@DisplayName("생산 계획 API 테스트")
class ProductionPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductionPlanService productionPlanService;

    @Autowired
    private Clock testClock;

    @TestConfiguration
    static class ProductionPlanControllerTestContext{
        @Bean
        public ProductionPlanService productionPlanService(){
            return mock(ProductionPlanServiceImpl.class);
        }

        @Bean
        public Clock testClock() {
            return Clock.fixed(
                Instant.parse("2099-01-01T00:00:00Z"),
                ZoneId.systemDefault()
            );
        }
    }

    @Test
    @DisplayName("생산 계획 등록에 성공합니다 - 201 Created")
    @WithCustomUser(roles = {"ROLE_ADMIN"})
    void enrollProductionPlan_success() throws Exception {
        // given
        CreateProductionPlanRequestDto createRequestDto = CreateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now())
            .plannedQty(BigDecimal.valueOf(100))
            .productionManagerNo("209910001")
            .salesManagerNo("209910002")
            .factoryCode("F001")
            .lineCode("L001")
            .remark("testRemark")
            .itemCode("I001")
            .build();

        GetProductionPlanResponseDto responseDto = GetProductionPlanResponseDto.builder()
            .productionManagerNo("209910001")
            .build();

        when(productionPlanService.createProductionPlan(any(CreateProductionPlanRequestDto.class), any(Users.class))).thenReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/production-plans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDto))
        );

        // then
        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.data.productionManagerNo").value("209910001"));
    }

    @Test
    @DisplayName("생산 계획 조회에 성공합니다 - 200 OK")
    @WithMockUser
    void getProductionPlan_success() throws Exception {
        // given
        Long planId = 123L;
        GetProductionPlanDetailResponseDto dto = GetProductionPlanDetailResponseDto.builder()
            .id(planId)
            .dueDate(LocalDate.of(2025, 1, 10))
            .status(ProductionPlans.PlanStatus.PENDING)
            .salesManagerNo("S001")
            .salesManagerName("홍길동")
            .productionManagerNo("P001")
            .productionManagerName("이순신")
            .startTime(LocalDateTime.now(testClock))
            .endTime(LocalDateTime.now(testClock))
            .factoryCode("F001")
            .factoryName("서울1공장")
            .itemCode("ITEM01")
            .itemName("샘플품목")
            .plannedQty(BigDecimal.valueOf(100))
            .lineCode("L01")
            .lineName("1호라인")
            .remark("테스트 비고")
            .build();

        when(productionPlanService.getProductionPlan(planId)).thenReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/production-plans/{planDocumentId}", planId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(planId))
            .andExpect(jsonPath("$.data.salesManagerNo").value("S001"))
            .andExpect(jsonPath("$.data.factoryCode").value("F001"))
            .andExpect(jsonPath("$.data.itemName").value("샘플품목"))
            .andExpect(jsonPath("$.data.lineCode").value("L01"))
            .andExpect(jsonPath("$.data.remark").value("테스트 비고"));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 성공 - 기본 페이지/정렬")
    @WithMockUser
    void getProductionPlanList_success_defaultPageable() throws Exception {
        // given
        GetProductionPlanListResponseDto dto1 = GetProductionPlanListResponseDto.builder()
            .documentNo("DOC001")
            .dueDate(LocalDate.now(testClock).plusDays(1))
            .status(ProductionPlans.PlanStatus.PENDING)
            .factoryName("1공장")
            .itemName("좋은 배터리")
            .plannedQty(BigDecimal.valueOf(100))
            .build();

        GetProductionPlanListResponseDto dto2 = GetProductionPlanListResponseDto.builder()
            .documentNo("DOC002")
            .dueDate(LocalDate.now(testClock).plusDays(1))
            .status(ProductionPlans.PlanStatus.CONFIRMED)
            .factoryName("2공장")
            .itemName("좋은 셀")
            .plannedQty(BigDecimal.valueOf(200))
            .build();

        Page<GetProductionPlanListResponseDto> page = new PageImpl<>(List.of(dto1, dto2));

        when(productionPlanService.getProductionPlanList(any(), any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/production-plans")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].documentNo").value("DOC001"))
            .andExpect(jsonPath("$.data.content[1].documentNo").value("DOC002"))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 성공 - 검색 필터 적용")
    @WithMockUser
    void getProductionPlanList_success_withSearch() throws Exception {
        // given
        GetProductionPlanListResponseDto dto = GetProductionPlanListResponseDto.builder()
            .documentNo("DOC001")
            .factoryName("1공장")
            .status(ProductionPlans.PlanStatus.PENDING)
            .build();

        Page<GetProductionPlanListResponseDto> page = new PageImpl<>(List.of(dto));

        when(productionPlanService.getProductionPlanList(any(), any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/production-plans")
                .param("factoryName", "1공장")
                .param("status", "PENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].factoryName").value("1공장"))
            .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - 결과 없음")
    @WithMockUser
    void getProductionPlanList_emptyResult() throws Exception {
        // given
        Page<GetProductionPlanListResponseDto> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productionPlanService.getProductionPlanList(any(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/v1/production-plans")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));
    }

    @Test
    @DisplayName("생산 계획 목록 조회 - 잘못된 페이지 파라미터 처리")
    @WithMockUser
    void getProductionPlanList_invalidPageParam() throws Exception {
        mockMvc.perform(get("/api/v1/production-plans")
                .param("status", "notexisting")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("생산 계획 수정 - 수정 성공")
    @WithCustomUser(roles = {"ROLE_ADMIN"})
    void updateProductionPlan_success() throws Exception {
        // given
        UpdateProductionPlanRequestDto updateDto = UpdateProductionPlanRequestDto.builder()
            .dueDate(LocalDate.now(testClock))
            .status(PlanStatus.PENDING)
            .salesManagerNo("209901001")
            .productionManagerNo("209901002")
            .startTime(LocalDateTime.now(testClock))
            .remark("new remark")
            .factoryCode("F001")
            .lineCode("L001")
            .itemCode("I001")
            .build();

        Long planId = 1L;

        GetProductionPlanResponseDto responseDto = GetProductionPlanResponseDto.builder()
                .id(planId)
                .lineCode("L001")
                .salesManagerNo("209901001")
                .productionManagerNo("209901002")
                .documentNo("2099/01/01-1")
                .status(PlanStatus.PENDING)
                .dueDate(LocalDate.now(testClock))
                .plannedQty(new BigDecimal("100"))
                .startTime(LocalDateTime.now(testClock))
                .endTime(LocalDateTime.now(testClock))
                .remark("new remark")
                .factoryCode("F001")
                .itemCode("I001")
                .build();

        when(productionPlanService.updateProductionPlan(any(UpdateProductionPlanRequestDto.class), eq(planId), any(Users.class)))
            .thenReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/production-plans/{planId}", planId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)));

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(planId))
            .andExpect(jsonPath("$.data.lineCode").value("L001"))
            .andExpect(jsonPath("$.data.factoryCode").value("F001"))
            .andExpect(jsonPath("$.data.dueDate").value(LocalDate.now(testClock).toString()))
            .andExpect(jsonPath("$.data.startTime").value(startsWith("2099-01-01T09:00")))
            .andExpect(jsonPath("$.data.endTime").value(startsWith("2099-01-01T09:00")));
    }

    @Test
    @DisplayName("생산 계획 현황 조회 성공 - 검색 필터 적용")
    @WithMockUser
    void getAllProductionPlan_success_withSearch() throws Exception {
        // given
        GetAllProductionPlanResponseDto testDto = GetAllProductionPlanResponseDto.builder()
            .id(1L)
            .documentNo("2025/11/22-1")
            .status(ProductionPlans.PlanStatus.PENDING)
            .factoryName("A공장")
            .lineName("1호라인")
            .itemCode("ITEM-1001")
            .itemName("샘플제품")
            .itemSpecification("SPEC-01")
            .plannedQty(new BigDecimal("1500"))
            .startTime(LocalDateTime.now(testClock))
            .endTime(LocalDateTime.now(testClock))
            .dueDate(LocalDate.now(testClock))
            .salesManagerName("김영업")
            .salesManagerNo("202511001")
            .productionManagerName("박생산")
            .productionManagerNo("202511001")
            .remark("테스트 생산계획")
            .build();

        when(productionPlanService.getAllProductionPlan(any(GetAllProductionPlanRequestDto.class))).thenReturn(List.of(testDto));

        // when & then
        mockMvc.perform(get("/api/v1/production-plans/all")
                .param("factoryName", "A공장")
                .param("itemName", "샘플제품")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].factoryName").value("A공장"))
            .andExpect(jsonPath("$.data[0].itemName").value("샘플제품"));
    }


    @Test
    @DisplayName("생산 계획 일정 조회 성공 - 검색 필터 적용")
    @WithMockUser
    void getProductionPlanSchedule_success_withSearch() throws Exception {
        // given
        GetProductionPlanScheduleResponseDto testDto = GetProductionPlanScheduleResponseDto.builder()
            .id(1L)
            .lineName("1호라인")
            .lineCode("LINE-001")
            .factoryName("A공장")
            .factoryCode("FAC-001")
            .salesManagerNo("202511001")
            .productionManagerNo("202511001")
            .documentNo("2025/11/22-1")
            .itemCode("ITEM-1001")
            .status(ProductionPlans.PlanStatus.PENDING)
            .dueDate(LocalDate.now(testClock))
            .plannedQty(new BigDecimal("1500"))
            .startTime(LocalDateTime.now(testClock))
            .endTime(LocalDateTime.now(testClock))
            .remark("테스트 생산계획")
            .build();

        when(productionPlanService.getProductionPlanSchedule(any(GetProductionPlanScheduleRequestDto.class))).thenReturn(List.of(testDto));

        // when & then
        mockMvc.perform(get("/api/v1/production-plans/schedules")
                .param("startTime", LocalDateTime.now(testClock).toString())
                .param("endTime", LocalDateTime.now(testClock).toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].factoryName").value("A공장"))
            .andExpect(jsonPath("$.data[0].startTime").value(startsWith(LocalDateTime.now(testClock).toString())))
            .andExpect(jsonPath("$.data[0].endTime").value(startsWith(LocalDateTime.now(testClock).toString())));
    }

    @Test
    @DisplayName("생산 계획 일정 조회 실패 - startTime 누락")
    @WithMockUser
    void getProductionPlanSchedule_fail_missingStartTime() throws Exception {
        LocalDateTime now = LocalDateTime.now(testClock);

        mockMvc.perform(get("/api/v1/production-plans/schedules")
                .param("endTime", now.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("생산 계획 일정 조회 실패 - endTime 누락")
    @WithMockUser
    void getProductionPlanSchedule_fail_missingEndTime() throws Exception {
        LocalDateTime now = LocalDateTime.now(testClock);

        mockMvc.perform(get("/api/v1/production-plans/schedules")
                .param("startTime", now.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("생산 계획 종료 시간 반환 성공 - 200 OK")
    @WithMockUser(roles = {"MANAGER"})
    void getProductionPlanEndTime_success_200() throws Exception {
        // given
        LocalDateTime endTime = LocalDateTime.now(testClock).plusDays(1);
        LocalDateTime startTime = LocalDateTime.now(testClock);
        GetProductionPlanEndTimeRequestDto requestDto = GetProductionPlanEndTimeRequestDto.builder()
            .startTime(startTime)
            .plannedQty(new BigDecimal("1500"))
            .lineCode("LINE-001")
            .build();

        GetProductionPlanEndTimeResponseDto testDto = GetProductionPlanEndTimeResponseDto.builder()
            .endTime(endTime)
            .build();

        when(productionPlanService.getProductionPlanEndTime(any(GetProductionPlanEndTimeRequestDto.class)))
            .thenReturn(testDto);

        mockMvc.perform(post("/api/v1/production-plans/endtime")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.endTime").value(startsWith(endTime.toString())))
        ;
    }

    @Test
    @DisplayName("생산 계획 상태 수정 성공 - 200 OK")
    @WithMockUser(roles = {"ADMIN"})
    void updateProductionPlanStatus_success_200() throws Exception {
        // given
        UpdateProductionPlanStatusRequestDto requestDto = UpdateProductionPlanStatusRequestDto.builder()
            .planIds(List.of(1L, 2L))
            .planStatus(ProductionPlans.PlanStatus.PENDING)
            .build();

        UpdateProductionPlanStatusResponseDto responseDto = UpdateProductionPlanStatusResponseDto.builder()
            .planIds(List.of(1L, 2L))
            .planStatus(ProductionPlans.PlanStatus.PENDING)
            .build();

        when(productionPlanService.updateProductionPlanStatus(any(UpdateProductionPlanStatusRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/production-plans/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.planIds[0]").value(1L))
            .andExpect(jsonPath("$.data.planStatus").value("PENDING"))
        ;
    }
}