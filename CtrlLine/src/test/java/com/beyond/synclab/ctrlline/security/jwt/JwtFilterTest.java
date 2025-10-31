package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import com.beyond.synclab.ctrlline.security.exception.AuthException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtStoreService jwtStoreService;
    private AuthenticationEntryPoint entryPoint;
    private JwtFilter jwtFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        jwtStoreService = mock(JwtStoreService.class);
        entryPoint = mock(AuthenticationEntryPoint.class);
        jwtFilter = new JwtFilter(jwtUtil, jwtStoreService, entryPoint);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("화이트리스트 경로는 필터를 적용하지 않는다")
    void shouldNotFilter_whitelist() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        boolean result = jwtFilter.shouldNotFilter(request);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 그대로 통과한다")
    void noAuthorizationHeader() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("정상 토큰이면 SecurityContextHolder에 인증이 설정된다")
    void validToken_setsAuthentication() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(jwtUtil.getJti(anyString(), any())).thenReturn("jti123");
        when(jwtUtil.getCategory(anyString(), any())).thenReturn("ACCESS");
        when(jwtUtil.getUsername(anyString(), any())).thenReturn("user@example.com");
        when(jwtStoreService.isBlacklisted("jti123")).thenReturn(false);
        when(jwtStoreService.getUserDetails("user@example.com")).thenReturn(userDetails);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).validateToken("valid.jwt.token", TokenType.ACCESS);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    @Test
    @DisplayName("만료된 토큰이면 entryPoint가 호출된다")
    void expiredToken_callsEntryPoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer expired.token");
        doThrow(new ExpiredJwtException(null, null, "token expired"))
                .when(jwtUtil).validateToken(anyString(), any());

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(entryPoint, times(1)).commence(eq(request), eq(response), any(AuthException.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("블랙리스트 토큰 검출 시 entryPoint 호출")
    void blacklistedToken_callsEntryPoint() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer fake.jwt.token");

        when(jwtUtil.getJti(anyString(), any())).thenReturn("jti-black123");
        when(jwtStoreService.isBlacklisted("jti-black123")).thenReturn(true);
        doNothing().when(jwtUtil).validateToken(anyString(), any());
        when(jwtUtil.getCategory(anyString(), any())).thenReturn("ACCESS");

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(entryPoint, times(1)).commence(
                eq(request),
                eq(response),
                any(AuthException.class)
        );
        verify(filterChain, never()).doFilter(request, response);
    }
}