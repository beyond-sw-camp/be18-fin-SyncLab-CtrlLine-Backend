package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MesTelemetryListenerTest {

    @Mock
    private MesPowerConsumptionService mesPowerConsumptionService;

    private MesTelemetryListener listener;

    @BeforeEach
    void setUp() {
        listener = new MesTelemetryListener(new ObjectMapper(), mesPowerConsumptionService);
    }

    @Test
    void parsePayload_returnsJsonNode_whenValidJson() {
        JsonNode node = ReflectionTestUtils.invokeMethod(listener, "parsePayload", "{\"machineId\":\"M-01\"}");

        assertThat(node.get("machineId").asText()).isEqualTo("M-01");
    }

    @Test
    void parsePayload_returnsNull_whenBlank() {
        JsonNode result = ReflectionTestUtils.invokeMethod(listener, "parsePayload", "  ");
        assertThat(result).isNull();
    }

    @Test
    void parsePayload_returnsNull_whenInvalidJson() {
        JsonNode result = ReflectionTestUtils.invokeMethod(listener, "parsePayload", "invalid-json");
        assertThat(result).isNull();
    }

    @Test
    void onTelemetry_handlesRecordWithoutException() {
        ConsumerRecord<String, String> telemetryRecord = new ConsumerRecord<>(
                "mes-machine-telemetry",
                0,
                10,
                "machine",
                "{\"status\":\"RUNNING\"}"
        );

        assertThatCode(() -> listener.onTelemetry(telemetryRecord))
                .doesNotThrowAnyException();
    }

    @Test
    void onTelemetry_aggregatesEnergyUsagePerTimestamp() {
        String firstPayload = """
                {"records":[
                    {"value":{"machine":"M-01","tag":"energy_usage","value":0.5,"timestamp":1762756823000}},
                    {"value":{"machine":"M-02","tag":"energy_usage","value":1.0,"timestamp":1762756823000}}
                ]}
                """;
        String triggerFlushPayload = """
                {"records":[
                    {"value":{"machine":"M-03","tag":"energy_usage","value":2.0,"timestamp":1762756825000}}
                ]}
                """;

        listener.onTelemetry(consumerRecord(firstPayload));
        listener.onTelemetry(consumerRecord(triggerFlushPayload));

        verify(mesPowerConsumptionService, times(1))
                .savePowerConsumption(BigDecimal.valueOf(1.50).setScale(2));
    }

    private ConsumerRecord<String, String> consumerRecord(String payload) {
        return new ConsumerRecord<>("mes-machine-telemetry", 0, 0L, "key", payload);
    }
}
