package com.beyond.synclab.ctrlline.domain.log.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.log.dto.LogListResponseDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogSearchDto;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import com.beyond.synclab.ctrlline.domain.log.service.LogServiceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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

@WebMvcTest(controllers = LogController.class)
@Import(TestSecurityConfig.class)
class LogControllerTest {
    @TestConfiguration
    static class LogControllerTestContextConfiguration {
        @Bean
        public LogServiceImpl logService() {
            return mock(LogServiceImpl.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private LogServiceImpl logsServiceImpl;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getLogsList() throws Exception {
        // given
        LocalDate localDate = LocalDate.ofInstant(Instant.parse("2099-01-01T00:00:00Z"),  ZoneId.systemDefault());
        LogListResponseDto logListResponseDto = LogListResponseDto
            .builder()
            .logId(1L)
            .actionType(ActionType.CREATE)
            .entityId(1L)
            .entityName("user")
            .userId(1L)
            .createdAt(localDate.plusDays(1).atStartOfDay())
            .build();

        when(logsServiceImpl.getLogsList(any(LogSearchDto.class))).thenReturn(List.of(logListResponseDto));

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/logs")
            .param("entityName", "user")
            .param("userId", "1")
            .param("fromDate", localDate.toString())
            .param("toDate", localDate.plusDays(2).toString())
            .param("actionType", ActionType.CREATE.name())
            .contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].logId").value(1))
            .andExpect(jsonPath("$.data[0].entityName").value("user"))
            .andExpect(jsonPath("$.data[0].entityId").value(1))
            .andExpect(jsonPath("$.data[0].createdAt").value("2099-01-02T00:00:00"))
            .andExpect(jsonPath("$.data[0].actionType").value(ActionType.CREATE.name()));
    }
}