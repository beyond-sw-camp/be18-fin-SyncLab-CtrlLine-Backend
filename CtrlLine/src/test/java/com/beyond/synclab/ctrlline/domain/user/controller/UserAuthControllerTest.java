package com.beyond.synclab.ctrlline.domain.user.controller;

import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 Security 필터 무시
class UserAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAuthService userAuthService;

    // ======== Fixture Builder ========
    private UserSignupRequestDto buildSignupRequest(String name) {
        return UserSignupRequestDto.builder()
                .name(name)
                .email("hong@test.com")
                .password("12341234")
                .passwordConfirm("12341234")
                .status(Users.UserStatus.ACTIVE)
                .phoneNumber("010-1234-1234")
                .address("화산로")
                .department("영업1팀")
                .position(Users.UserPosition.ASSISTANT)
                .role(Users.UserRole.USER)
                .hiredDate(LocalDate.of(2025, 10, 20))
                .build();
    }

    // ======== Test Case: 회원가입 성공 ========
    @Test
    @DisplayName("회원가입 성공 - 201 CREATED")
    void signup_success() throws Exception {
        // given
        String name = "홍길동";
        UserSignupRequestDto requestDto = buildSignupRequest(name);

        UserSignupResponseDto responseDto = UserSignupResponseDto.builder()
                .id(1L)
                .name(name)
                .email("hong@test.com")
                .build();

        when(userAuthService.signup(any(UserSignupRequestDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value("hong@test.com"));
    }

    // ======== Test Case: Validation 실패 ========
    @Test
    @DisplayName("회원가입 실패 - 이름 누락 시 400 BAD_REQUEST")
    void signup_validation_fail() throws Exception {
        // given
        UserSignupRequestDto invalidRequest = buildSignupRequest(null);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}