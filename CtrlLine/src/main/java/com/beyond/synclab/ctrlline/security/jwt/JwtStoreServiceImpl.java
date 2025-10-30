package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.common.property.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtStoreServiceImpl implements JwtStoreService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    // ================== Refresh Token 관리 ==================

    // 저장 (username 기준)
    @Override
    public void saveRefreshToken(String username, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                appProperties.getRedis().prefix().refresh() + username,
                refreshToken,
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(
                appProperties.getRedis().prefix().refresh() + username
        );
    }

    // 삭제
    @Override
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(
                appProperties.getRedis().prefix().refresh() + username
        );
    }

    // ================== Access Token 블랙리스트 관리 ==================

    // 블랙리스트 등록 (jti 기준)
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                appProperties.getRedis().prefix().blacklist() + jti,
                "logout",
                ttlSeconds,
                TimeUnit.SECONDS
        );
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(appProperties.getRedis().prefix().blacklist() + jti);
    }
}
