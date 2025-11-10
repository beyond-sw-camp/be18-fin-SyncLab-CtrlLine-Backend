package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import com.beyond.synclab.ctrlline.security.jwt.JwtStoreService;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.beyond.synclab.ctrlline.security.jwt.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthService 단위 테스트")
class UserAuthServiceTest {
    @Mock
    private JwtStoreService jwtStoreService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAuthService userAuthService;

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 시 AppException 발생")
    void signup_fail_duplicateEmail() {
        // given
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("test@test.com")
                .password("1234")
                .name("홍길동")
                .hiredDate(LocalDate.of(2025, 10, 1))
                .build();

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthService.signup(request))
                .isInstanceOf(AppException.class)
                .hasMessage(AuthErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 성공 - empNo 자동생성 및 암호화 저장")
    void signup_success() {
        // given
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("hong@test.com")
                .password("1234")
                .name("홍길동")
                .hiredDate(LocalDate.of(2025, 10, 20))
                .build();

        when(userRepository.existsByEmail("hong@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("1234"))
                .thenReturn("ENCODED");
        when(userRepository.findEmpNosByPrefix("202510"))
                .thenReturn(Collections.singletonList("202510003"));

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);

        // when
        userAuthService.signup(request);

        // then
        verify(userRepository).save(captor.capture());
        Users saved = captor.getValue();

        assertThat(saved.getEmpNo()).isEqualTo("202510004");
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
    }

    @Test
    @DisplayName("empNo 생성 로직 - 동일 월 내 순번 증가 확인")
    void generateEmpNo_incrementsCorrectly() {
        // given
        LocalDate hiredDate = LocalDate.of(2025, 10, 15);
        when(userRepository.findEmpNosByPrefix("202510"))
                .thenReturn(Arrays.asList("202510003", "202510002", "202510001"));

        // when
        String empNo = ReflectionTestUtils.invokeMethod(userAuthService, "generateEmpNo", hiredDate);

        // then
        assertThat(empNo).isEqualTo("202510004");
    }

    @Test
    @DisplayName("로그아웃 성공 - 200 OK")
    void logout_success() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer accessToken");
        when(jwtUtil.isExpired("accessToken", TokenType.ACCESS)).thenReturn(false);
        when(jwtUtil.getUsername("accessToken", TokenType.ACCESS)).thenReturn("hong123@test.com");
        doNothing().when(jwtStoreService).deleteRefreshToken("hong123@test.com");
        when(jwtUtil.getJti("accessToken", TokenType.ACCESS)).thenReturn("qwe123");
        when(jwtUtil.getExpiration("accessToken", TokenType.ACCESS)).thenReturn(Date.valueOf(LocalDate.now()));
        doNothing().when(jwtStoreService).blacklistAccessToken(eq("qwe123"), anyLong());

        // when
        userAuthService.logout(mockRequest, mockResponse);

        // then
        verify(jwtUtil, times(1)).isExpired("accessToken", TokenType.ACCESS);
        verify(jwtUtil, times(1)).getUsername("accessToken", TokenType.ACCESS);
        verify(jwtUtil, times(1)).getExpiration("accessToken", TokenType.ACCESS);
        verify(jwtUtil, times(1)).getJti("accessToken", TokenType.ACCESS);
        verify(jwtStoreService, times(1)).blacklistAccessToken(eq("qwe123"), anyLong());
        verify(jwtStoreService, times(1)).deleteRefreshToken("hong123@test.com");
    }

    @Test
    @DisplayName("로그아웃 실패 - Authorization 헤더 없음 (401 Unauthorized)")
    void logout_fail_noAuthorizationHeader() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userAuthService.logout(mockRequest, mockResponse))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.UNAUTHORIZED);

        verify(jwtUtil, never()).isExpired(anyString(), any());
        verify(jwtStoreService, never()).deleteRefreshToken(anyString());
    }

    @Test
    @DisplayName("로그아웃 실패 - Authorization 헤더 형식 오류 (401 Unauthorized)")
    void logout_fail_invalidAuthorizationHeader() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader("Authorization")).thenReturn("Invalid accessToken");

        // when & then
        assertThatThrownBy(() -> userAuthService.logout(mockRequest, mockResponse))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.UNAUTHORIZED);

        verify(jwtUtil, never()).isExpired(anyString(), any());
    }

    @Test
    @DisplayName("로그아웃 실패 - 만료된 Access Token (401 ACCESS_TOKEN_EXPIRED)")
    void logout_fail_expiredAccessToken() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(jwtUtil.isExpired("expiredToken", TokenType.ACCESS)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthService.logout(mockRequest, mockResponse))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.ACCESS_TOKEN_EXPIRED);

        verify(jwtUtil, times(1)).isExpired("expiredToken", TokenType.ACCESS);
        verify(jwtStoreService, never()).deleteRefreshToken(anyString());
    }

    @Test
    @DisplayName("로그아웃 실패 - 블랙리스트 등록 중 예외 발생")
    void logout_fail_blacklistError() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.isExpired("validToken", TokenType.ACCESS)).thenReturn(false);
        when(jwtUtil.getUsername("validToken", TokenType.ACCESS)).thenReturn("hong123@test.com");
        when(jwtUtil.getJti("validToken", TokenType.ACCESS)).thenReturn("jti123");
        when(jwtUtil.getExpiration("validToken", TokenType.ACCESS))
            .thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));

        doNothing().when(jwtStoreService).deleteRefreshToken("hong123@test.com");
        doThrow(new RuntimeException("Redis error"))
            .when(jwtStoreService).blacklistAccessToken(eq("jti123"), anyLong());

        // when & then
        assertThatThrownBy(() -> userAuthService.logout(mockRequest, mockResponse))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Redis error");

        verify(jwtStoreService, times(1)).deleteRefreshToken("hong123@test.com");
    }

}