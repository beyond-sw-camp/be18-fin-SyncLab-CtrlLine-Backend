package com.beyond.synclab.ctrlline.domain.productionplan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.CreateProductionPlanRequestDto;
import com.beyond.synclab.ctrlline.domain.productionplan.dto.ProductionPlanResponseDto;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanService;
import com.beyond.synclab.ctrlline.domain.productionplan.service.ProductionPlanServiceImpl;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

    @TestConfiguration
    static class ProductionPlanControllerTestContext{
        @Bean
        public ProductionPlanService productionPlanService(){
            return mock(ProductionPlanServiceImpl.class);
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
            .endTime(LocalDateTime.now())
            .startTime(LocalDateTime.now())
            .remark("testRemark")
            .status(PlanStatus.PENDING)
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
}