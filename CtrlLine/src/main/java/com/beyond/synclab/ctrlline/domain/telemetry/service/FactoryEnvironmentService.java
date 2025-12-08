package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.property.AppProperties;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnvironmentResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.errorcode.TelemetryErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryEnvironmentService {

    private final FactoryRepository factoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public void saveReading(Long factoryId, BigDecimal temperature, BigDecimal humidity, LocalDateTime recordedAt) {
        if (factoryId == null || temperature == null || humidity == null) {
            return;
        }
        Factories factory = factoryRepository.findById(factoryId)
                .orElse(null);
        if (factory == null) {
            log.warn("Factory not found for environment reading. factoryId={}", factoryId);
            return;
        }
        FactoryEnvironmentResponse snapshot = FactoryEnvironmentResponse.builder()
                .factoryCode(factory.getFactoryCode())
                .factoryId(factory.getId())
                .temperature(temperature.setScale(2, RoundingMode.HALF_UP))
                .humidity(humidity.setScale(2, RoundingMode.HALF_UP))
                .recordedAt(Optional.ofNullable(recordedAt).orElse(LocalDateTime.now()))
                .build();
        try {
            redisTemplate.opsForValue().set(redisKey(factory.getFactoryCode()), objectMapper.writeValueAsString(snapshot));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize environment snapshot for factoryCode={}", factory.getFactoryCode(), e);
        }
    }

    public FactoryEnvironmentResponse getLatestReading(String factoryCode) {
        Factories factory = findFactory(factoryCode);
        return readSnapshot(factory);
    }

    public List<FactoryEnvironmentResponse> getReadings(String factoryCode) {
        Factories factory = findFactory(factoryCode);
        return List.of(readSnapshot(factory));
    }

    private Factories findFactory(String factoryCode) {
        return factoryRepository.findByFactoryCode(factoryCode)
                .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));
    }

    private FactoryEnvironmentResponse readSnapshot(Factories factory) {
        String raw = (String) redisTemplate.opsForValue().get(redisKey(factory.getFactoryCode()));
        if (!StringUtils.hasText(raw)) {
            throw new AppException(TelemetryErrorCode.ENVIRONMENT_DATA_NOT_FOUND);
        }
        try {
            FactoryEnvironmentResponse response = objectMapper.readValue(raw, FactoryEnvironmentResponse.class);
            if (response.getFactoryCode() == null) {
                return response.toBuilder()
                        .factoryCode(factory.getFactoryCode())
                        .factoryId(factory.getId())
                        .build();
            }
            return response;
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize environment snapshot for factoryCode={}", factory.getFactoryCode(), e);
            throw new AppException(TelemetryErrorCode.ENVIRONMENT_DATA_NOT_FOUND);
        }
    }

    private String redisKey(String factoryCode) {
        return appProperties.getRedis().prefix().environmentLatest() + factoryCode;
    }
}
