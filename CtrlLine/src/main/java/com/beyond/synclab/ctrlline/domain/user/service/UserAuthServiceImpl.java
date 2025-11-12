package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.util.CookieUtil;
import com.beyond.synclab.ctrlline.domain.user.dto.ReissueResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import com.beyond.synclab.ctrlline.security.jwt.JwtStoreService;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import com.beyond.synclab.ctrlline.security.jwt.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtStoreService jwtStoreService;

    @Override
    @Transactional
    public UserSignupResponseDto enroll(UserSignupRequestDto request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        Users user = request.toEntity(generateEmpNo(request.getHiredDate()), passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return UserSignupResponseDto.fromEntity(user);
    }

    private String generateEmpNo(LocalDate hiredDate) {
        // 1️⃣ 입사일 기준 prefix 생성
        String prefix = String.format("%04d%02d", hiredDate.getYear(), hiredDate.getMonthValue());

        List<String> empNos = userRepository.findEmpNosByPrefix(prefix);
        int nextSeq = 1; // 기본 순번

        if (!empNos.isEmpty()) {
            String lastEmpNo = empNos.getFirst(); // 가장 최신
            String lastSeqStr = lastEmpNo.substring(6); // YYYYMMXXX 중 XXX
            nextSeq = Integer.parseInt(lastSeqStr) + 1;
        }

        return prefix + String.format("%03d", nextSeq);
    }

    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authorization = request.getHeader("Authorization");

        //  헤더 체크
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.debug(">> Authorization 헤더가 유효하지 않습니다.");
            throw new AppException(AuthErrorCode.UNAUTHORIZED);
        }

        //  Access Token 추출
        String accessToken = authorization.substring(7);

        //  토큰 만료 여부 확인
        if (jwtUtil.isExpired(accessToken, TokenType.ACCESS)) {
            log.debug("이미 만료된 Access Token 입니다.");
            throw new AppException(AuthErrorCode.ACCESS_TOKEN_EXPIRED);
        }

        //  사용자 식별자 추출
        String username = jwtUtil.getUsername(accessToken, TokenType.ACCESS);

        //  Refresh Token 삭제 (Redis)
        jwtStoreService.deleteRefreshToken(username);

        //  Refresh Token 쿠키 삭제
        response.addCookie(CookieUtil.deleteCookie("refresh_token"));

        //  Access Token 블랙리스트 등록
        String jti = jwtUtil.getJti(accessToken, TokenType.ACCESS); // Access Token의 jti 추출
        long ttl = (jwtUtil.getExpiration(accessToken, TokenType.ACCESS).getTime() - System.currentTimeMillis()) / 1000; // 남은 만료 시간
        jwtStoreService.blacklistAccessToken(jti, ttl);
    }

    @Override
    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
        log.debug("🔁 [ReissueService] 리프레시 요청 처리");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  RefreshToken 만료 검사
        try {
            jwtUtil.validateToken(refreshToken, TokenType.REFRESH); // ExpiredJwtException 던짐
        } catch (ExpiredJwtException e) {
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  category 확인
        if (!TokenType.REFRESH.name().equals(jwtUtil.getCategory(refreshToken,  TokenType.REFRESH))) {
            throw new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  username 추출
        String username = jwtUtil.getUsername(refreshToken,  TokenType.REFRESH);

        //  Redis 에서 RefreshToken 확인
        String savedRefresh = jwtStoreService.getRefreshToken(username);
        if (savedRefresh == null || !savedRefresh.equals(refreshToken)) {
            throw new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        //  DB에서 유저 다시 조회 → role, slug 확보
        Users user = userRepository.findByEmail(username)
            .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        UserRole role = user.getRole();

        //  새 토큰 발급
        String newAccess = jwtUtil.createAccessToken(username, role.name());
        String newRefresh = jwtUtil.createRefreshToken(username);

        //  Redis 갱신
        jwtStoreService.deleteRefreshToken(username);
        long refreshTtl = (jwtUtil.getExpiration(newRefresh, TokenType.REFRESH).getTime() - System.currentTimeMillis()) / 1000;
        jwtStoreService.saveRefreshToken(username, newRefresh, refreshTtl);

        return ReissueResponseDto.builder()
            .accessToken(newAccess)
            .refreshToken(newRefresh)
            .maxAge((int) refreshTtl)
            .build();
    }


}
