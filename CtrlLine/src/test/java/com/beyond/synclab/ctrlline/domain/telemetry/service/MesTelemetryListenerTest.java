package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.beyond.synclab.ctrlline.common.property.MesKafkaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MesTelemetryListenerTest {

    private final MesKafkaProperties properties = new MesKafkaProperties(
            List.of("localhost:29092"),
            "mes-machine-telemetry",
            "ctrlline-client",
            "ctrlline-group",
            "earliest",
            java.time.Duration.ofSeconds(5)
    );

    private final MesTelemetryListener listener = new MesTelemetryListener(new ObjectMapper(), properties);

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
