package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
