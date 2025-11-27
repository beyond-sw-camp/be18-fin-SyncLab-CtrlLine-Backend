package com.beyond.synclab.ctrlline.domain.telemetry.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnergyUsageResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.service.FactoryEnergyUsageService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FactoryEnergyUsageControllerTest {

    @Mock
    private FactoryEnergyUsageService factoryEnergyUsageService;

    @InjectMocks
    private FactoryEnergyUsageController factoryEnergyUsageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(factoryEnergyUsageController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

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
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.factoryCode").value("F0001"))
                .andExpect(jsonPath("$.data.powerConsumption").value(12.34))
                .andExpect(jsonPath("$.data.recordedAt").value(notNullValue()));

        Mockito.verify(factoryEnergyUsageService).getLatestEnergyUsage("F0001");
    }

    @Test
    @DisplayName("GET /api/v1/factories/{code}/energy/today-max 가 금일 최고 전력 사용량을 반환한다")
    void getTodayPeakEnergyUsage() throws Exception {
        FactoryEnergyUsageResponse response = FactoryEnergyUsageResponse.builder()
                .factoryCode("F0002")
                .powerConsumption(new BigDecimal("20.50"))
                .recordedAt(LocalDateTime.of(2025, 11, 27, 15, 30))
                .build();
        given(factoryEnergyUsageService.getTodayPeakEnergyUsage(eq("F0002"))).willReturn(response);

        mockMvc.perform(get("/api/v1/factories/{factoryCode}/energy/today-max", "F0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.factoryCode").value("F0002"))
                .andExpect(jsonPath("$.data.powerConsumption").value(20.50))
                .andExpect(jsonPath("$.data.recordedAt").value(notNullValue()));

        Mockito.verify(factoryEnergyUsageService).getTodayPeakEnergyUsage("F0002");
    }
}
