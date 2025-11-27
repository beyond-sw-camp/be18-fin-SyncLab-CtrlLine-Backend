package com.beyond.synclab.ctrlline.domain.telemetry.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnergyUsageResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.service.FactoryEnergyUsageService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FactoryEnergyUsageController.class)
@AutoConfigureMockMvc(addFilters = false)
class FactoryEnergyUsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FactoryEnergyUsageService factoryEnergyUsageService;

    @Test
    @DisplayName("GET /api/v1/factories/{code}/energy/latest 가 최신 전력 사용량을 반환한다")
    void getLatestEnergyUsage() throws Exception {
        // given
        FactoryEnergyUsageResponse response = FactoryEnergyUsageResponse.builder()
                .factoryCode("F0001")
                .powerConsumption(new BigDecimal("12.34"))
                .recordedAt(LocalDateTime.of(2025, 11, 27, 12, 34))
                .build();
        given(factoryEnergyUsageService.getLatestEnergyUsage(eq("F0001"))).willReturn(response);

        // when / then
        mockMvc.perform(get("/api/v1/factories/{factoryCode}/energy/latest", "F0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(BaseResponse.ok(response).getCode()))
                .andExpect(jsonPath("$.data.factoryCode").value("F0001"))
                .andExpect(jsonPath("$.data.powerConsumption").value(12.34))
                .andExpect(jsonPath("$.data.recordedAt").value("2025-11-27T12:34:00"));

        Mockito.verify(factoryEnergyUsageService).getLatestEnergyUsage("F0001");
    }
}
