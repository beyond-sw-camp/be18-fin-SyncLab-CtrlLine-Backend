package com.beyond.synclab.ctrlline.security.handler;

import com.beyond.synclab.ctrlline.common.util.CookieUtil;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import com.beyond.synclab.ctrlline.security.jwt.JwtStoreService;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.beyond.synclab.ctrlline.security.jwt.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtStoreService jwtStoreService; // Redis 사용

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        log.debug(">> onAuthenticationSuccess : {} ", authentication.getName());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // 🔹 인증 대상 분기 처리
        String email = userDetails.getUsername();
        String role = userDetails.getUser().getRole().name();

        // 1. 토큰 생성
        String accessToken = jwtUtil.createAccessToken(email, role);
        String refreshToken = jwtUtil.createRefreshToken(email);

        // 3. 기존 세션/토큰 제거
        jwtStoreService.deleteRefreshToken(email);

        // 4. 새로운 Refresh 저장
        Date refreshExpiration = jwtUtil.getExpiration(refreshToken, TokenType.REFRESH);
        long refreshTtl = (refreshExpiration.getTime() - System.currentTimeMillis()) / 1000;
        jwtStoreService.saveRefreshToken(email, refreshToken, refreshTtl);

        // 5. Access Token → Authorization 헤더
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 6. Refresh Token → HttpOnly, Secure, SameSite=None 쿠키
        int maxAge = (int) refreshTtl;
        response.addCookie(
                CookieUtil.createHttpOnlyCookie("refresh_token", refreshToken, maxAge)
        );

        // 7. 상태 코드만 반환
        response.setStatus(HttpStatus.OK.value());
    }
}
