package com.beyond.synclab.ctrlline.domain.optimization.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.annotation.WithCustomUser;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizeCommitResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto;
import com.beyond.synclab.ctrlline.domain.optimization.dto.OptimizePreviewResponseDto.PreviewPlanDto;
import com.beyond.synclab.ctrlline.domain.optimization.service.ProductionPlanOptimizationService;
import com.beyond.synclab.ctrlline.domain.optimization.service.ProductionPlanOptimizationServiceImpl;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = OptimizationController.class)
class OptimizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("mockProductionPlanOptimizationService")
    private ProductionPlanOptimizationService optimizationService;

    @TestConfiguration
    static class ProductionPlanControllerTestContext{
        @Bean
        public ProductionPlanOptimizationService mockProductionPlanOptimizationService(){
            return mock(ProductionPlanOptimizationServiceImpl.class);
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
    @DisplayName("생산계획 최적화 미리보기 성공 - 200")
    @WithCustomUser
    void optimizePreview_success() throws Exception {

        // given
        String lineCode = "LINE01";

        OptimizePreviewResponseDto responseDto = OptimizePreviewResponseDto.builder()
            .previewKey("opt:preview:1234")
            .lineCode(lineCode)
            .plans(List.of(
                PreviewPlanDto.builder()
                    .planId(10L)
                    .documentNo("2025/12/01-1")
                    .originalStartTime(LocalDateTime.of(2025, 12, 1, 9, 0))
                    .optimizedStartTime(LocalDateTime.of(2025, 12, 1, 9, 10))
                    .optimizedEndTime(LocalDateTime.of(2025, 12, 1, 11, 0))
                    .dueDateTime(LocalDateTime.of(2025, 12, 2, 12, 0))
                    .confirmed(false)
                    .locked(false)
                    .build()
            ))
            .build();

        Mockito.when(optimizationService.previewOptimization(eq(lineCode), any(Users.class)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/production-plans/{lineCode}/optimize/preview", lineCode)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.previewKey").value("opt:preview:1234"))
            .andExpect(jsonPath("$.data.lineCode").value(lineCode))
            .andExpect(jsonPath("$.data.plans[0].planId").value(10L));
    }

    @Test
    @DisplayName("생산계획 최적화 확정 성공 - 200")
    @WithCustomUser
    void optimizeCommit_success() throws Exception {

        // given
        String lineCode = "LINE01";
        String previewKey = "opt:preview:1234";

        OptimizeCommitResponseDto responseDto = OptimizeCommitResponseDto.builder()
            .lineCode(lineCode)
            .updatedCount(3)
            .build();

        Mockito.when(optimizationService.commitOptimization(eq(lineCode), eq(previewKey), any(Users.class)))
            .thenReturn(responseDto);

        String body = """
        {
          "previewKey": "opt:preview:1234"
        }
        """;

        // when & then
        mockMvc.perform(
                post("/api/v1/production-plans/{lineCode}/optimize/commit", lineCode)
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.lineCode").value(lineCode))
            .andExpect(jsonPath("$.data.updatedCount").value(3));
    }
}