package com.beyond.synclab.ctrlline.domain.production.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.domain.production.client.MiloClientException;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.production.service.ProductionOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductionOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductionOrderControllerTest.MockConfig.class)
class ProductionOrderControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        ProductionOrderService productionOrderService() {
            return Mockito.mock(ProductionOrderService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductionOrderService productionOrderService;

    @BeforeEach
    void setUp() {
        Mockito.reset(productionOrderService);
    }

    @Test
    @DisplayName("생산지시 API 성공 시 201 Created와 Milo 응답 본문을 반환한다")
    void dispatchOrder_success() throws Exception {
        ProductionOrderCommandRequest request = new ProductionOrderCommandRequest(
                "START",
                "2025-10-24-1",
                7000,
                "PRD-7782",
                null
        );
        ProductionOrderCommandResponse response = new ProductionOrderCommandResponse(
                "2025-10-24-1",
                "PS-001",
                "PRD-7782",
                7000,
                "2025-10-30T02:45:12Z"
        );

        when(productionOrderService.dispatchOrder(eq("FC-001"), eq("PS-001"), any(ProductionOrderCommandRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/cmd")
                        .param("factoryCode", "FC-001")
                        .param("lineCode", "PS-001")
                        .header("Authorization", "Bearer test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNo").value("2025-10-24-1"))
                .andExpect(jsonPath("$.lineCode").value("PS-001"))
                .andExpect(jsonPath("$.itemCode").value("PRD-7782"))
                .andExpect(jsonPath("$.qty").value(7000))
                .andExpect(jsonPath("$.acceptedAt").value("2025-10-30T02:45:12Z"));
    }

    @Test
    @DisplayName("Milo API가 오류를 반환하면 동일한 상태코드와 메시지를 전달한다")
    void dispatchOrder_miloError() throws Exception {
        ProductionOrderCommandRequest request = new ProductionOrderCommandRequest(
                "START",
                "2025-10-24-2",
                1255,
                "PRODUCT-002",
                120
        );
        String errorPayload = """
                {
                    "status": "NOT_FOUND",
                    "code": "404",
                    "message": "라인 또는 품목이 존재하지 않습니다."
                }
                """;

        when(productionOrderService.dispatchOrder(eq("FC-999"), eq("PS-999"), any(ProductionOrderCommandRequest.class)))
                .thenThrow(new MiloClientException(HttpStatus.NOT_FOUND, errorPayload));

        mockMvc.perform(post("/api/v1/orders/cmd")
                        .param("factoryCode", "FC-999")
                        .param("lineCode", "PS-999")
                        .header("Authorization", "Bearer test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json(errorPayload));
    }
}
