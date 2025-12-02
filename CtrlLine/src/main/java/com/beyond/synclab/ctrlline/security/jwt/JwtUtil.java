package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.common.property.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.beyond.synclab.ctrlline.security.jwt.JwtConstants.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {
    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final long accessTokenTtl;
    private final long refreshTokenTtl;

    private SecretKey buildKey(String secret) {
        return new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public JwtUtil(JwtProperties jwtProperties) {
        this.accessSecretKey = buildKey(jwtProperties.getAccessToken().secretKey());
        this.refreshSecretKey = buildKey(jwtProperties.getRefreshToken().secretKey());
        this.accessTokenTtl = jwtProperties.getAccessToken().expired();
        this.refreshTokenTtl = jwtProperties.getRefreshToken().expired();
    }

    // ================== Access Token ==================
    public String createAccessToken(String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_CATEGORY, TokenType.ACCESS)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenTtl))
                .signWith(accessSecretKey)
                .compact();
    }

    // ================== Refresh Token ==================
    public String createRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_CATEGORY, TokenType.REFRESH)
                .claim(CLAIM_USERNAME, username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenTtl))
                .signWith(refreshSecretKey)
                .compact();
    }

    private SecretKey resolveKey(TokenType type) {
        return type == TokenType.ACCESS ? accessSecretKey : refreshSecretKey;
    }

    // ================== Claim 추출 ==================
    public String getUsername(String token, TokenType tokenType) {
        return Jwts.parser().verifyWith(resolveKey(tokenType)).build()
                .parseSignedClaims(token).getPayload()
                .get(CLAIM_USERNAME, String.class);
    }

    public String getRole(String token) {
        // access token 에만 역할 저장
        return Jwts.parser().verifyWith(accessSecretKey).build()
                .parseSignedClaims(token).getPayload()
                .get(CLAIM_ROLE, String.class);
    }

    public String getCategory(String token, TokenType tokenType) {
        return Jwts.parser().verifyWith(resolveKey(tokenType)).build()
                .parseSignedClaims(token).getPayload()
                .get(CLAIM_CATEGORY, String.class);
    }

    // ================== jti 추출 ==================
    public String getJti(String token, TokenType tokenType) {
        return Jwts.parser().verifyWith(resolveKey(tokenType)).build()
                .parseSignedClaims(token).getPayload()
                .getId();
    }

    // ================== Expiration ==================
    public boolean isExpired(String token, TokenType tokenType) {
        return Jwts.parser().verifyWith(resolveKey(tokenType)).build()
                .parseSignedClaims(token).getPayload()
                .getExpiration()
                .before(new Date());
    }

    public Date getExpiration(String token, TokenType tokenType) {
        return Jwts.parser().verifyWith(resolveKey(tokenType)).build()
                .parseSignedClaims(token).getPayload()
                .getExpiration();
    }

    public void validateToken(String token, TokenType tokenType) {
        try {
            Jwts.parser()
                    .verifyWith(resolveKey(tokenType))
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            log.info("Token expired: {}", token);
            throw e; // 필요시 재발급 흐름으로 전달
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", token);
            throw e;
        }
    }
}
