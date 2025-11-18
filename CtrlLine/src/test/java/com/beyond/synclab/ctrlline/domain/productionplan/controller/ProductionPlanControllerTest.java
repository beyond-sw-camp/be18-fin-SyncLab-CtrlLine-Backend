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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
}