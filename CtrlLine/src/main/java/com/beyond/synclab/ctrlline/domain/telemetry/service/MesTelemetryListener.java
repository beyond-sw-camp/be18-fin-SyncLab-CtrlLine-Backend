package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.common.property.MesKafkaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesTelemetryListener {

    private final ObjectMapper objectMapper;
    private final MesKafkaProperties mesKafkaProperties;

    @KafkaListener(
            topics = "${mes.kafka.topic}",
            containerFactory = "telemetryKafkaListenerContainerFactory"
    )
    public void onTelemetry(ConsumerRecord<String, String> record) {
        JsonNode payload = parsePayload(record.value());
        log.info("Telemetry received topic={}, partition={}, offset={}, key={}, payload={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                payload != null ? payload : record.value());
        // TODO: downstream processing once telemetry contract is finalized
    }

    JsonNode parsePayload(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (IOException ex) {
            log.warn("Failed to parse telemetry payload as JSON. value={}", value, ex);
            return null;
        }
    }
}
