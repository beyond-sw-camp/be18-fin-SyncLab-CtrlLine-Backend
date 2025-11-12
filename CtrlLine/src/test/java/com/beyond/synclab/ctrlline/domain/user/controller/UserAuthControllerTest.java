package com.beyond.synclab.ctrlline.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.ReissueResponseDto;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthServiceImpl;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserAuthController.class)
@Import(TestSecurityConfig.class)
class UserAuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class UserAuthControllerTestContextConfiguration {
        @Bean
        UserAuthServiceImpl userAuthService() {return mock(UserAuthServiceImpl.class);}
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAuthServiceImpl userAuthServiceImpl;

    @Test
    @DisplayName("로그아웃 성공 - 200")
    @WithMockUser
    void logout_success() throws Exception {
        // given
        doNothing().when(userAuthServiceImpl).logout(any(HttpServletRequest.class), any(
            HttpServletResponse.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/logout")
            .header("Authorization", "Bearer accessToken")
            .cookie(new Cookie("refresh_token", "refreshToken")));

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("로그아웃 실패 - 401")
    void logout_unAuthorized() throws Exception {
        // given
        doThrow(new AppException(AuthErrorCode.UNAUTHORIZED))
            .when(userAuthServiceImpl)
            .logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/logout")
            .header("Authorization", "Bearer accessToken")
            .cookie(new Cookie("refresh_token", "refreshToken")));

        // then
        resultActions
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("액세스토큰 재발급 - 204 No Content")
    void refresh_noContent() throws Exception {
        // given
        ReissueResponseDto responseDto = ReissueResponseDto.builder()
            .accessToken("accessToken")
            .refreshToken("refreshToken")
            .maxAge(100)
            .build();

        when(userAuthServiceImpl.reissue("refreshToken")).thenReturn(responseDto);

        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/token/refresh")
            .cookie(new Cookie("refresh_token", "refreshToken"))
            .header("Authorization", "Bearer accessToken"));

        resultActions
            .andExpect(status().isNoContent())
            .andExpect(header().string("Authorization", "Bearer accessToken"))
            .andExpect(cookie().maxAge("refresh_token", 100))
            .andExpect(cookie().value("refresh_token", "refreshToken"));
    }
}