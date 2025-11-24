package com.beyond.synclab.ctrlline.domain.productionplan.service;

import com.beyond.synclab.ctrlline.common.property.AppProperties;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanDefectiveLastReportedCache {

    private static final Duration TTL = Duration.ofDays(2);

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    public Optional<BigDecimal> get(Long planDefectiveId, Long defectiveId, String equipmentKey) {
        String key = cacheKey(planDefectiveId, defectiveId, equipmentKey);
        if (key == null) {
            return Optional.empty();
        }
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(value)) {
                return Optional.empty();
            }
            return Optional.of(new BigDecimal(value));
        } catch (Exception ex) {
            log.warn("Failed to read last reported qty from Redis. key={}", key, ex);
            return Optional.empty();
        }
    }

    public void save(Long planDefectiveId, Long defectiveId, String equipmentKey, BigDecimal qty) {
        String key = cacheKey(planDefectiveId, defectiveId, equipmentKey);
        if (key == null || qty == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, qty.toPlainString(), TTL);
        } catch (Exception ex) {
            log.warn("Failed to save last reported qty to Redis. key={} qty={}", key, qty, ex);
        }
    }

    public void evict(Long planDefectiveId, Long defectiveId, String equipmentKey) {
        String key = cacheKey(planDefectiveId, defectiveId, equipmentKey);
        if (key == null) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Failed to delete last reported qty from Redis. key={}", key, ex);
        }
    }

    private String cacheKey(Long planDefectiveId, Long defectiveId, String equipmentKey) {
        if (planDefectiveId == null || defectiveId == null) {
            return null;
        }
        String prefix = appProperties.getRedis().prefix().planDefectiveLastReported();
        String equipmentPart = StringUtils.hasText(equipmentKey) ? equipmentKey : "default";
        return String.format("%s%s:%s:%s", prefix, planDefectiveId, defectiveId, equipmentPart);
    }
}
