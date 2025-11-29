package com.beyond.synclab.ctrlline.domain.defective.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto.DefectiveItem;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveService;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
        Long id = 12345L;

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

        when(defectiveService.getDefective(anyLong())).thenReturn(getDefectiveDetailResponseDto);

        ResultActions resultActions = mockMvc.perform(get("/api/v1/defectives/{id}", id)
            .contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.defectiveDocNo").value("2099/01/01-1"))
            .andExpect(jsonPath("$.data.defectives[0].defectiveCode").value("D001"))
            .andExpect(jsonPath("$.data.defectives[0].defectiveType").value("d1"));
    }

    @Test
    @WithMockUser
    @DisplayName("불량 목록 조회 성공 - 200")
    void getDefectiveList_success() throws Exception {
        LocalDate testDate = LocalDate.now(clock);
        // given
        SearchDefectiveListRequestDto requestDto = SearchDefectiveListRequestDto.builder()
            .fromDate(testDate)
            .toDate(testDate.plusDays(1))
            .productionPerformanceDocNo("2099/01/01-1")
            .build();

        GetDefectiveListResponseDto responseDto = GetDefectiveListResponseDto.builder()
            .planDefectiveId(123L)
            .defectiveDocNo("2099/01/01-1")
            .itemId(123L)
            .itemCode("I001")
            .itemName("품목1")
            .lineId(123L)
            .lineCode("L001")
            .lineName("라인1")
            .defectiveTotalQty(BigDecimal.valueOf(1000))
            .defectiveTotalRate(BigDecimal.valueOf(0.12))
            .productionPerformanceDocNo("2099/01/01-1")
            .createdAt(LocalDateTime.now(clock))
            .build();

        Page<GetDefectiveListResponseDto> pageResponse =
            new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1);

        when(defectiveService.getDefectiveList(any(), any())).thenReturn(pageResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/defectives")
                .param("fromDate", requestDto.fromDate().toString())
                .param("toDate", requestDto.fromDate().toString())
                .param("productionPerformanceDocNo", requestDto.productionPerformanceDocNo())
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
            .andExpect(status().isOk())
            // data.content[0] 값 검증
            .andExpect(jsonPath("$.data.content[0].planDefectiveId").value(123L))
            .andExpect(jsonPath("$.data.content[0].defectiveDocNo").value("2099/01/01-1"))
            .andExpect(jsonPath("$.data.content[0].itemCode").value("I001"))
            // 페이징 메타 검증
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
            .andExpect(jsonPath("$.data.pageInfo.currentPage").value(1))
            .andExpect(jsonPath("$.data.pageInfo.pageSize").value(10));
    }

    @Test
    @WithMockUser
    @DisplayName("불량 전체 목록 조회 성공 - 200")
    void getDefectiveAll_success() throws Exception {
        // given
        LocalDate testDate = LocalDate.now(clock);
        SearchDefectiveAllRequestDto requestDto = SearchDefectiveAllRequestDto.builder()
            .fromDate(testDate)
            .toDate(testDate.plusDays(1))
            .factoryCode("FAC-001")
            .lineCode("L001")
            .itemId(100L)
            .productionManagerNo("PM-001")
            .salesManagerNo("SM-001")
            .productionPerformanceDocNo("2099/01/01-1")
            .build();

        GetDefectiveAllResponseDto responseDto = GetDefectiveAllResponseDto.builder()
            .planDefectiveId(123L)
            .defectiveDocNo("2099/01/01-1")
            .itemId(100L)
            .itemCode("I001")
            .itemName("품목1")
            .itemSpecification("Spec A")
            .lineId(1L)
            .lineCode("L001")
            .lineName("라인1")
            .factoryId(1L)
            .factoryCode("FAC-001")
            .factoryName("공장1")
            .productionManagerName("생산담당자")
            .productionManagerNo("PM-001")
            .salesManagerName("영업담당자")
            .salesManagerNo("SM-001")
            .defectiveTotalQty(BigDecimal.valueOf(1000))
            .defectiveTotalRate(BigDecimal.valueOf(0.12))
            .productionPerformanceDocNo("2099/01/01-1")
            .createdAt(LocalDateTime.now(clock))
            .build();

        when(defectiveService.getAllDefective(any())).thenReturn(List.of(responseDto));

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/defectives/all")
                .param("fromDate", requestDto.fromDate().toString())
                .param("toDate", requestDto.toDate().toString())
                .param("factoryCode", requestDto.factoryCode())
                .param("lineCode", requestDto.lineCode())
                .param("itemId", requestDto.itemId().toString())
                .param("productionManagerNo", requestDto.productionManagerNo())
                .param("salesManagerNo", requestDto.salesManagerNo())
                .param("productionPerformanceDocNo", requestDto.productionPerformanceDocNo())
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
            // data content 검증
            .andExpect(jsonPath("$.data[0].planDefectiveId").value(123L))
            .andExpect(jsonPath("$.data[0].defectiveDocNo").value("2099/01/01-1"))
            .andExpect(jsonPath("$.data[0].itemCode").value("I001"))
            .andExpect(jsonPath("$.data[0].factoryCode").value("FAC-001"))
            .andExpect(jsonPath("$.data[0].lineCode").value("L001"))
            .andExpect(jsonPath("$.data[0].productionManagerNo").value("PM-001"))
            .andExpect(jsonPath("$.data[0].salesManagerNo").value("SM-001"))
            .andExpect(jsonPath("$.data[0].defectiveTotalQty").value(1000))
            .andExpect(jsonPath("$.data[0].defectiveTotalRate").value(0.12))
            .andExpect(jsonPath("$.data[0].productionPerformanceDocNo").value("2099/01/01-1"))
            // payload size 검증
            .andExpect(jsonPath("$.data.length()").value(1));
    }

}