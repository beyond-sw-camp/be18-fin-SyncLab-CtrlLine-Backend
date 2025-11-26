package com.beyond.synclab.ctrlline.domain.defective.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto.DefectiveItem;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveService;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(DefectiveController.class)
class DefectiveControllerTest {
    @Autowired
    private DefectiveService defectiveService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock clock;

    @TestConfiguration
    static class DefectiveControllerTestConfiguration {
        @Bean
        public DefectiveService defectiveService() {
            return mock(DefectiveServiceImpl.class);
        }

        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2099-01-01T00:00:00.000Z"), ZoneId.systemDefault());
        }
    }

    @Test
    @WithMockUser
    @DisplayName("불량 상세 조회 성공 - 200")
    void getDefective() throws Exception {
        // given
        String documentNo = "12345";

        GetDefectiveDetailResponseDto getDefectiveDetailResponseDto = GetDefectiveDetailResponseDto.builder()
            .defectiveDocNo("2099/01/01-1")
            .factoryName("공장1")
            .itemCode("I001")
            .itemName("품목1")
            .itemUnit("EA.")
            .itemSpecification("규격1")
            .lineName("라인1")
            .totalQty(BigDecimal.valueOf(10000))
            .defectiveItems(List.of(
                DefectiveItem.builder()
                    .equipmentName("설비1")
                    .equipmentCode("E001")
                    .defectiveCode("D001")
                    .defectiveType("d1")
                    .defectiveName("불량1")
                    .defectiveQty(BigDecimal.valueOf(100))
                    .defectiveRate(0.01)
                    .build()
            ))
            .build();

        when(defectiveService.getDefective(anyString())).thenReturn(getDefectiveDetailResponseDto);

        ResultActions resultActions = mockMvc.perform(get("/api/v1/defectives/{docNo}", documentNo)
            .contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.defectiveDocNo").value("2099/01/01-1"))
            .andExpect(jsonPath("$.data.defectives[0].defectiveCode").value("D001"))
            .andExpect(jsonPath("$.data.defectives[0].defectiveType").value("d1"));
    }

}