package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
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

        ProductionPlanResponseDto responseDto = ProductionPlanResponseDto.builder()
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
        ProductionPlanDetailResponseDto dto = ProductionPlanDetailResponseDto.builder()
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
        ProductionPlanListResponseDto dto1 = ProductionPlanListResponseDto.builder()
            .documentNo("DOC001")
            .dueDate(LocalDate.now(testClock).plusDays(1))
            .status(ProductionPlans.PlanStatus.PENDING)
            .factoryName("1공장")
            .itemName("좋은 배터리")
            .plannedQty(BigDecimal.valueOf(100))
            .build();

        ProductionPlanListResponseDto dto2 = ProductionPlanListResponseDto.builder()
            .documentNo("DOC002")
            .dueDate(LocalDate.now(testClock).plusDays(1))
            .status(ProductionPlans.PlanStatus.CONFIRMED)
            .factoryName("2공장")
            .itemName("좋은 셀")
            .plannedQty(BigDecimal.valueOf(200))
            .build();

        Page<ProductionPlanListResponseDto> page = new PageImpl<>(List.of(dto1, dto2));

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
        ProductionPlanListResponseDto dto = ProductionPlanListResponseDto.builder()
            .documentNo("DOC001")
            .factoryName("1공장")
            .status(ProductionPlans.PlanStatus.PENDING)
            .build();

        Page<ProductionPlanListResponseDto> page = new PageImpl<>(List.of(dto));

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
        Page<ProductionPlanListResponseDto> emptyPage = new PageImpl<>(Collections.emptyList());

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
}