package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.common.property.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtStoreServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Redis redis;

    @Mock
    private AppProperties.Redis.Prefix prefix;

    @InjectMocks
    private JwtStoreServiceImpl jwtStoreService;

    @BeforeEach
    void setUp() {
        // record는 상속 불가이므로 Mock으로 반환 설정
        lenient().when(appProperties.getRedis()).thenReturn(redis);
        lenient().when(redis.prefix()).thenReturn(prefix);

        lenient().when(prefix.refresh()).thenReturn("refresh:");
        lenient().when(prefix.blacklist()).thenReturn("blacklist:");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveRefreshToken_savesValue() {
        jwtStoreService.saveRefreshToken("user1", "token123", 3600);

        // redisTemplate.opsForValue().set 호출 검증
        verify(valueOperations).set("refresh:user1", "token123", 3600, TimeUnit.SECONDS);
    }

    @Test
    void getRefreshToken_returnsValue() {
        when(valueOperations.get("refresh:user1")).thenReturn("token123");

        String result = jwtStoreService.getRefreshToken("user1");
        assertEquals("token123", result);
    }

    @Test
    void deleteRefreshToken_deletesKey() {
        jwtStoreService.deleteRefreshToken("user1");
        verify(redisTemplate).delete("refresh:user1");
    }

    @Test
    void blacklistAccessToken_savesValue() {
        jwtStoreService.blacklistAccessToken("jti123", 1800);
        verify(valueOperations).set("blacklist:jti123", "logout", 1800, TimeUnit.SECONDS);
    }

    @Test
    void isBlacklisted_returnsTrue() {
        when(redisTemplate.hasKey("blacklist:jti123")).thenReturn(true);

        boolean result = jwtStoreService.isBlacklisted("jti123");
        assertTrue(result);
    }

    @Test
    void isBlacklisted_returnsFalse() {
        when(redisTemplate.hasKey("blacklist:jti456")).thenReturn(false);

        boolean result = jwtStoreService.isBlacklisted("jti456");
        assertFalse(result);
    }
}