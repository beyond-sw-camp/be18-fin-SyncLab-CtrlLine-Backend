package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_CODE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_CODE_FIELD_ALT;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_CODE_FIELD_SNAKE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_NAME_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_NAME_FIELD_ALT;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_NAME_FIELD_SNAKE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_QTY_FIELD_ALT;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_QTY_FIELD_SNAKE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.DEFECTIVE_STATUS_VALUE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ENERGY_USAGE_TAG;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.EQUIPMENT_CODE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.EQUIPMENT_CODE_FIELD_SNAKE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.EQUIPMENT_ID_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.EQUIPMENT_ID_FIELD_SNAKE;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.STATUS_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.TIMESTAMP_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.VALUE_FIELD;

import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
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

    private final ObjectMapper objectMapper;
    private final MesPowerConsumptionService mesPowerConsumptionService;
    private final MesDefectiveService mesDefectiveService;

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
            JsonNode valueNode = recordNode.path(VALUE_FIELD);
            if (isEnergyUsageRecord(valueNode)) {
                double energyUsage = valueNode.path(VALUE_FIELD).asDouble(Double.NaN);
                long timestamp = valueNode.path(TIMESTAMP_FIELD).asLong(0L);
                accumulateEnergyUsage(timestamp, energyUsage);
                continue;
            }
            if (isNgDefectiveRecord(valueNode)) {
                persistDefectiveRecord(valueNode);
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
        if (!valueNode.hasNonNull(VALUE_FIELD) || !valueNode.hasNonNull(TIMESTAMP_FIELD)) {
            return false;
        }
        double energyUsage = valueNode.path(VALUE_FIELD).asDouble(Double.NaN);
        long timestamp = valueNode.path(TIMESTAMP_FIELD).asLong(0L);
        return !Double.isNaN(energyUsage) && timestamp > 0L;
    }

    private boolean isNgDefectiveRecord(JsonNode valueNode) {
        if (!valueNode.isObject()) {
            return false;
        }
        String status = valueNode.path(STATUS_FIELD).asText(null);
        if (status == null || !DEFECTIVE_STATUS_VALUE.equalsIgnoreCase(status)) {
            return false;
        }
        return hasAnyNonNull(valueNode,
                DEFECTIVE_CODE_FIELD,
                DEFECTIVE_CODE_FIELD_ALT,
                DEFECTIVE_CODE_FIELD_SNAKE);
    }

    private void persistDefectiveRecord(JsonNode valueNode) {
        DefectiveTelemetryPayload payload = buildDefectivePayload(valueNode);
        if (payload != null) {
            mesDefectiveService.saveNgTelemetry(payload);
        }
    }

    private DefectiveTelemetryPayload buildDefectivePayload(JsonNode valueNode) {
        BigDecimal quantity = firstDecimal(valueNode,
                DEFECTIVE_QTY_FIELD,
                DEFECTIVE_QTY_FIELD_ALT,
                DEFECTIVE_QTY_FIELD_SNAKE);
        String defectiveCode = firstNonEmptyText(valueNode,
                DEFECTIVE_CODE_FIELD,
                DEFECTIVE_CODE_FIELD_ALT,
                DEFECTIVE_CODE_FIELD_SNAKE);
        String defectiveName = firstNonEmptyText(valueNode,
                DEFECTIVE_NAME_FIELD,
                DEFECTIVE_NAME_FIELD_ALT,
                DEFECTIVE_NAME_FIELD_SNAKE);

        if (quantity == null || defectiveCode == null || defectiveName == null) {
            return null;
        }

        return DefectiveTelemetryPayload.builder()
                .equipmentId(firstLong(valueNode, EQUIPMENT_ID_FIELD, EQUIPMENT_ID_FIELD_SNAKE))
                .equipmentCode(firstNonEmptyText(valueNode, EQUIPMENT_CODE_FIELD, EQUIPMENT_CODE_FIELD_SNAKE))
                .defectiveCode(defectiveCode)
                .defectiveName(defectiveName)
                .defectiveQuantity(quantity)
                .status(valueNode.path(STATUS_FIELD).asText())
                .build();
    }

    private boolean hasAnyNonNull(JsonNode node, String... fieldNames) {
        return Arrays.stream(fieldNames)
                .anyMatch(field -> node.hasNonNull(field) && node.get(field).isValueNode());
    }

    private String firstNonEmptyText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && field.isTextual()) {
                String text = field.asText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Long firstLong(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.canConvertToLong()) {
                return field.asLong();
            }
            if (field.isTextual()) {
                try {
                    return Long.parseLong(field.asText().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isNumber()) {
                return field.decimalValue();
            }
            if (field.isTextual()) {
                String text = field.asText().trim();
                if (text.isEmpty()) {
                    continue;
                }
                try {
                    return new BigDecimal(text);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
