package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.property.AppProperties;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnvironmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class FactoryEnvironmentServiceTest {

    @Mock
    private FactoryRepository factoryRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private AppProperties appProperties;
    @Mock
    private AppProperties.Redis redis;
    @Mock
    private AppProperties.Redis.Prefix prefix;

    private FactoryEnvironmentService factoryEnvironmentService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        factoryEnvironmentService = new FactoryEnvironmentService(
                factoryRepository,
                redisTemplate,
                objectMapper,
                appProperties
        );
        lenient().when(appProperties.getRedis()).thenReturn(redis);
        lenient().when(redis.prefix()).thenReturn(prefix);
        lenient().when(prefix.environmentLatest()).thenReturn("ctrlline:env:");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveReading_storesSnapshotInRedis() {
        Factories factory = Factories.builder()
                .id(1L)
                .factoryCode("F0001")
                .factoryName("Factory01")
                .isActive(true)
                .build();
        when(factoryRepository.findById(1L)).thenReturn(Optional.of(factory));

        factoryEnvironmentService.saveReading(
                1L,
                BigDecimal.valueOf(23.55),
                BigDecimal.valueOf(44.12),
                LocalDateTime.of(2025, 11, 30, 10, 0)
        );

        verify(valueOperations).set(anyString(), anyString());
    }

    @Test
    void getLatestReading_returnsSnapshotFromRedis() throws Exception {
        Factories factory = Factories.builder()
                .id(1L)
                .factoryCode("F0001")
                .factoryName("Factory01")
                .isActive(true)
                .build();
        when(factoryRepository.findByFactoryCode("F0001")).thenReturn(Optional.of(factory));

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        FactoryEnvironmentResponse snapshot = FactoryEnvironmentResponse.builder()
                .factoryCode("F0001")
                .factoryId(1L)
                .temperature(BigDecimal.valueOf(23.5))
                .humidity(BigDecimal.valueOf(45.0))
                .recordedAt(LocalDateTime.of(2025, 11, 30, 10, 0))
                .build();
        when(valueOperations.get("ctrlline:env:F0001")).thenReturn(mapper.writeValueAsString(snapshot));

        FactoryEnvironmentResponse result = factoryEnvironmentService.getLatestReading("F0001");

        assertThat(result.getTemperature()).isEqualByComparingTo("23.5");
        assertThat(result.getHumidity()).isEqualByComparingTo("45.0");
    }

    @Test
    void getLatestReading_throwsWhenMissing() {
        when(factoryRepository.findByFactoryCode("F0001")).thenReturn(
                Optional.of(Factories.builder().id(1L).factoryCode("F0001").build()));
        when(valueOperations.get("ctrlline:env:F0001")).thenReturn(null);

        assertThatThrownBy(() -> factoryEnvironmentService.getLatestReading("F0001"))
                .isInstanceOf(AppException.class);
    }
}
