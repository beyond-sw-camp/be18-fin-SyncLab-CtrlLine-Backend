package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesTelemetryListener {

    private static final String ENERGY_USAGE_TAG = "energy_usage";

    private final ObjectMapper objectMapper;
    private final MesPowerConsumptionService mesPowerConsumptionService;

    private final NavigableMap<Long, Double> energyUsageByBucket = new TreeMap<>();
    private final ReentrantLock aggregationLock = new ReentrantLock();

    @KafkaListener(
            topics = "${mes.kafka.topic}",
            containerFactory = "telemetryKafkaListenerContainerFactory"
    )
    public void onTelemetry(ConsumerRecord<String, String> telemetryRecord) {
        JsonNode payload = parsePayload(telemetryRecord.value());
        log.info("Telemetry received topic={}, partition={}, offset={}, key={}, payload={}",
                telemetryRecord.topic(),
                telemetryRecord.partition(),
                telemetryRecord.offset(),
                telemetryRecord.key(),
                payload != null ? payload : telemetryRecord.value());

        if (payload == null) {
            return;
        }
        JsonNode recordsNode = payload.path("records");
        if (!recordsNode.isArray()) {
            return;
        }

        for (JsonNode recordNode : recordsNode) {
            JsonNode valueNode = recordNode.path("value");
            if (isEnergyUsageRecord(valueNode)) {
                double energyUsage = valueNode.path("value").asDouble(Double.NaN);
                long timestamp = valueNode.path("timestamp").asLong(0L);
                accumulateEnergyUsage(timestamp, energyUsage);
            }
        }
    }

    private JsonNode parsePayload(String value) {
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

    private void accumulateEnergyUsage(long timestamp, double energyUsage) {
        long bucketTimestamp = bucketTimestamp(timestamp);
        aggregationLock.lock();
        try {
            energyUsageByBucket.merge(bucketTimestamp, energyUsage, Double::sum);
            flushCompletedAggregations(bucketTimestamp);
        } finally {
            aggregationLock.unlock();
        }
    }

    private void flushCompletedAggregations(long newestBucketTimestamp) {
        while (!energyUsageByBucket.isEmpty()) {
            Map.Entry<Long, Double> oldestEntry = energyUsageByBucket.firstEntry();
            if (oldestEntry.getKey() >= newestBucketTimestamp) {
                break;
            }
            persistPowerConsumption(oldestEntry.getValue());
            energyUsageByBucket.pollFirstEntry();
        }
    }

    private void persistPowerConsumption(double totalEnergyUsage) {
        BigDecimal powerConsumption = BigDecimal.valueOf(totalEnergyUsage)
                .setScale(2, RoundingMode.HALF_UP);
        mesPowerConsumptionService.savePowerConsumption(powerConsumption);
    }

    @PreDestroy
    public void flushRemainingAggregations() {
        aggregationLock.lock();
        try {
            energyUsageByBucket.values()
                    .forEach(this::persistPowerConsumption);
            energyUsageByBucket.clear();
        } finally {
            aggregationLock.unlock();
        }
    }

    private long bucketTimestamp(long timestamp) {
        return timestamp / 1000L;
    }

    private boolean isEnergyUsageRecord(JsonNode valueNode) {
        if (!valueNode.isObject()) {
            return false;
        }
        if (!ENERGY_USAGE_TAG.equals(valueNode.path("tag").asText())) {
            return false;
        }
        if (!valueNode.hasNonNull("value") || !valueNode.hasNonNull("timestamp")) {
            return false;
        }
        double energyUsage = valueNode.path("value").asDouble(Double.NaN);
        long timestamp = valueNode.path("timestamp").asLong(0L);
        return !Double.isNaN(energyUsage) && timestamp > 0L;
    }
}
