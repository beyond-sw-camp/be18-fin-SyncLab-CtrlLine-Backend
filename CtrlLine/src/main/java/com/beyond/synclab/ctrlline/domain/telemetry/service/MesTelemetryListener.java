package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CAUSE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CAUSE_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CLEARED_AT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CLEARED_AT_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_LEVEL_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_LEVEL_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_NAME_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_NAME_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CODE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_CODE_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_OCCURRED_AT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_OCCURRED_AT_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_TYPE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_TYPE_FIELD_CAMEL;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ALARM_USER_FIELD;
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
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.NG_NAME_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.NG_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.NG_TYPE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_CODE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_EVENT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_NAME_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_STATUS_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_TYPE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_PRODUCED_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_SUMMARY_DEFECTIVE_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_SUMMARY_EQUIPMENT_CODE_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_SUMMARY_PAYLOAD_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_SUMMARY_PRODUCED_QTY_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.ORDER_NG_TYPES_PAYLOAD_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.PRODUCTION_PERFORMANCE_EXECUTE_AT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.PRODUCTION_PERFORMANCE_NG_COUNT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.PRODUCTION_PERFORMANCE_ORDER_NO_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.PRODUCTION_PERFORMANCE_PAYLOAD_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.PRODUCTION_PERFORMANCE_WAITING_ACK_AT_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.STATUS_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.TIMESTAMP_FIELD;
import static com.beyond.synclab.ctrlline.domain.telemetry.constant.TelemetryConstants.VALUE_FIELD;

import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.ProductionPerformanceTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MesTelemetryListener {

    private final ObjectMapper objectMapper;
    private static final String ALARM_EVENT_KEY_SUFFIX = "alarm_event_payload";

    private final FactoryRepository factoryRepository;
    private final MesPowerConsumptionService mesPowerConsumptionService;
    private final MesDefectiveService mesDefectiveService;
    private final MesAlarmService mesAlarmService;
    private final MesProductionPerformanceService mesProductionPerformanceService;

    private final NavigableMap<AggregationKey, Double> energyUsageByBucket = new TreeMap<>();
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
        if (recordsNode.isArray()) {
            for (JsonNode recordNode : recordsNode) {
                JsonNode valueNode = recordNode.has(VALUE_FIELD) ? recordNode.get(VALUE_FIELD) : recordNode;
                handleTelemetryValue(valueNode, telemetryRecord.key());
            }
            return;
        }
        handleTelemetryValue(payload, telemetryRecord.key());
    }

    private void handleTelemetryValue(JsonNode valueNode, String recordKey) {
        boolean isPotentialAlarm = recordKey != null && recordKey.contains(ALARM_EVENT_KEY_SUFFIX);
        valueNode = unwrapOrderNgEvent(valueNode);
        JsonNode summaryPayload = extractOrderSummaryPayload(valueNode);
        if (summaryPayload != null) {
            persistOrderSummary(summaryPayload);
            return;
        }
        JsonNode ngTypePayload = extractNgTypeCountersPayload(valueNode);
        if (ngTypePayload != null) {
            persistNgTypeCounters(ngTypePayload);
            return;
        }
        JsonNode productionPerformancePayload = extractProductionPerformancePayload(valueNode);
        if (productionPerformancePayload != null) {
            persistProductionPerformance(productionPerformancePayload);
            return;
        }
        JsonNode alarmPayloadNode = extractAlarmPayload(valueNode, recordKey);
        if (isAlarmTelemetryRecord(recordKey, alarmPayloadNode)) {
            persistAlarmRecord(alarmPayloadNode);
            return;
        }
        valueNode = convertTextNodeToObject(valueNode);
        if (!valueNode.isObject()) {
            if (isPotentialAlarm) {
                log.info("알람 키를 수신했으나 JSON 객체로 파싱되지 않았습니다. key={}, rawValue={}", recordKey, valueNode);
            }
            return;
        }
        if (isEnergyUsageRecord(valueNode)) {
            double energyUsage = valueNode.path(VALUE_FIELD).asDouble(Double.NaN);
            long timestamp = valueNode.path(TIMESTAMP_FIELD).asLong(0L);
            Long factoryId = resolveFactoryIdFromEnergyUsage(valueNode);
            if (factoryId == null) {
                log.warn("Factory ID를 확인할 수 없어 전력 소모량을 저장하지 않습니다. payload={}", valueNode);
                return;
            }
            accumulateEnergyUsage(timestamp, energyUsage, factoryId);
            return;
        }
        if (isNgDefectiveRecord(valueNode)) {
            persistDefectiveRecord(valueNode, recordKey);
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

    private void accumulateEnergyUsage(long timestamp, double energyUsage, Long factoryId) {
        long bucketTimestamp = bucketTimestamp(timestamp);
        aggregationLock.lock();
        try {
            AggregationKey key = new AggregationKey(bucketTimestamp, factoryId);
            energyUsageByBucket.merge(key, energyUsage, Double::sum);
            flushCompletedAggregations(bucketTimestamp);
        } finally {
            aggregationLock.unlock();
        }
    }

    private void flushCompletedAggregations(long newestBucketTimestamp) {
        while (!energyUsageByBucket.isEmpty()) {
            Map.Entry<AggregationKey, Double> oldestEntry = energyUsageByBucket.firstEntry();
            if (oldestEntry.getKey().bucketTimestamp() >= newestBucketTimestamp) {
                break;
            }
            persistPowerConsumption(oldestEntry.getKey().factoryId(), oldestEntry.getValue());
            energyUsageByBucket.pollFirstEntry();
        }
    }

    private void persistPowerConsumption(Long factoryId, double totalEnergyUsage) {
        BigDecimal powerConsumption = BigDecimal.valueOf(totalEnergyUsage)
                .setScale(2, RoundingMode.HALF_UP);
        mesPowerConsumptionService.savePowerConsumption(powerConsumption, factoryId);
    }

    @PreDestroy
    public void flushRemainingAggregations() {
        aggregationLock.lock();
        try {
            energyUsageByBucket.forEach((key, value) -> persistPowerConsumption(key.factoryId(), value));
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
        String tag = valueNode.path("tag").asText();
        if (!StringUtils.hasText(tag) || !tag.endsWith(ENERGY_USAGE_TAG)) {
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
        if (hasOrderNgFields(valueNode)) {
            return firstDecimal(valueNode,
                    DEFECTIVE_QTY_FIELD,
                    DEFECTIVE_QTY_FIELD_ALT,
                    DEFECTIVE_QTY_FIELD_SNAKE,
                    ORDER_NG_QTY_FIELD,
                    NG_QTY_FIELD) != null;
        }
        String status = firstTextualValue(valueNode, STATUS_FIELD);
        if (status == null || !DEFECTIVE_STATUS_VALUE.equalsIgnoreCase(status)) {
            return false;
        }
        return hasAnyNonNull(valueNode,
                DEFECTIVE_CODE_FIELD,
                DEFECTIVE_CODE_FIELD_ALT,
                DEFECTIVE_CODE_FIELD_SNAKE);
    }

    private void persistDefectiveRecord(JsonNode valueNode, String recordKey) {
        DefectiveTelemetryPayload payload = buildDefectivePayload(valueNode);
        if (payload != null) {
            log.info(
                    "NG telemetry received equipmentId={}, equipmentCode={}, defectiveCode={}, defectiveName={}, qty={}",
                    payload.equipmentId(),
                    payload.equipmentCode(),
                    payload.defectiveCode(),
                    payload.defectiveName(),
                    payload.defectiveQuantity());
            boolean linkPlanXref = !isNgEventRecord(recordKey);
            mesDefectiveService.saveNgTelemetry(payload, linkPlanXref);
        } else {
            log.warn("NG telemetry skipped due to missing required fields payload={}", valueNode);
        }
    }

    private void persistOrderSummary(JsonNode summaryNode) {
        OrderSummaryTelemetryPayload payload = buildOrderSummaryPayload(summaryNode);
        if (payload == null) {
            log.warn("Order summary payload가 유효하지 않아 저장하지 않습니다. payload={}", summaryNode);
            return;
        }
        log.info("Order summary telemetry received equipmentCode={}, producedQty={}, ngQty={}",
                payload.equipmentCode(),
                payload.producedQuantity(),
                payload.defectiveQuantity());
        mesDefectiveService.saveOrderSummaryTelemetry(payload);
    }

    private void persistProductionPerformance(JsonNode performanceNode) {
        ProductionPerformanceTelemetryPayload payload = buildProductionPerformancePayload(performanceNode);
        if (payload == null) {
            log.warn("Production performance payload가 유효하지 않아 저장하지 않습니다. payload={}", performanceNode);
            return;
        }
        log.info("생산실적 텔레메트리 수신 orderNo={}, producedQty={}, ngCount={}",
                payload.orderNo(),
                payload.orderProducedQty(),
                payload.ngCount());
        mesProductionPerformanceService.saveProductionPerformance(payload);
    }

    private void persistAlarmRecord(JsonNode valueNode) {
        AlarmTelemetryPayload payload = buildAlarmPayload(valueNode);
        if (payload == null) {
            log.warn("알람 페이로드에 필수 항목이 없어 저장하지 않습니다. payload={}", valueNode);
            return;
        }
        log.info("알람 텔레메트리 수신 equipmentCode={}, alarmName={}, alarmType={}, alarmLevel={}, occurredAt={}, clearedAt={}",
                payload.equipmentCode(),
                payload.alarmName(),
                payload.alarmType(),
                payload.alarmLevel(),
                payload.occurredAt(),
                payload.clearedAt());
        mesAlarmService.saveAlarmTelemetry(payload);
    }

    private AlarmTelemetryPayload buildAlarmPayload(JsonNode valueNode) {
        String equipmentCode = firstNonEmptyValue(valueNode, EQUIPMENT_CODE_FIELD, EQUIPMENT_CODE_FIELD_SNAKE);
        String alarmName = firstNonEmptyValue(valueNode, ALARM_NAME_FIELD_CAMEL, ALARM_NAME_FIELD);
        if (!StringUtils.hasText(equipmentCode) || !StringUtils.hasText(alarmName)) {
            return null;
        }
        String alarmCode = firstNonEmptyValue(valueNode, ALARM_CODE_FIELD_CAMEL, ALARM_CODE_FIELD);
        String alarmType = firstNonEmptyValue(valueNode, ALARM_TYPE_FIELD_CAMEL, ALARM_TYPE_FIELD);
        String alarmLevel = firstNonEmptyValue(valueNode, ALARM_LEVEL_FIELD_CAMEL, ALARM_LEVEL_FIELD);
        LocalDateTime occurredAt = parseDateTime(firstNonEmptyValue(valueNode,
                ALARM_OCCURRED_AT_FIELD_CAMEL,
                ALARM_OCCURRED_AT_FIELD));
        LocalDateTime clearedAt = parseDateTime(firstNonEmptyValue(valueNode,
                ALARM_CLEARED_AT_FIELD_CAMEL,
                ALARM_CLEARED_AT_FIELD));
        String user = firstNonEmptyValue(valueNode, ALARM_USER_FIELD);
        String alarmCause = firstNonEmptyValue(valueNode, ALARM_CAUSE_FIELD_CAMEL, ALARM_CAUSE_FIELD);

        return AlarmTelemetryPayload.builder()
                .equipmentCode(equipmentCode)
                .alarmCode(alarmCode)
                .alarmType(alarmType)
                .alarmName(alarmName)
                .alarmLevel(alarmLevel)
                .occurredAt(occurredAt)
                .clearedAt(clearedAt)
                .user(user)
                .alarmCause(alarmCause)
                .build();
    }

    private boolean isAlarmTelemetryRecord(String recordKey, JsonNode valueNode) {
        if (!valueNode.isObject()) {
            return false;
        }
        if (hasAnyNonNull(valueNode,
                ALARM_NAME_FIELD,
                ALARM_NAME_FIELD_CAMEL,
                ALARM_CODE_FIELD,
                ALARM_CODE_FIELD_CAMEL,
                ALARM_TYPE_FIELD,
                ALARM_TYPE_FIELD_CAMEL,
                ALARM_LEVEL_FIELD,
                ALARM_LEVEL_FIELD_CAMEL)) {
            return true;
        }
        return recordKey != null && recordKey.contains(ALARM_EVENT_KEY_SUFFIX);
    }

    private DefectiveTelemetryPayload buildDefectivePayload(JsonNode valueNode) {
        BigDecimal quantity = firstDecimal(valueNode,
                DEFECTIVE_QTY_FIELD,
                DEFECTIVE_QTY_FIELD_ALT,
                DEFECTIVE_QTY_FIELD_SNAKE,
                ORDER_NG_QTY_FIELD,
                NG_QTY_FIELD);
        String orderNo = firstNonEmptyValue(valueNode, PRODUCTION_PERFORMANCE_ORDER_NO_FIELD);
        String defectiveCode = firstNonEmptyValue(valueNode,
                DEFECTIVE_CODE_FIELD,
                DEFECTIVE_CODE_FIELD_ALT,
                DEFECTIVE_CODE_FIELD_SNAKE,
                ORDER_NG_CODE_FIELD,
                ORDER_NG_TYPE_FIELD,
                NG_TYPE_FIELD);
        String defectiveName = firstNonEmptyValue(valueNode,
                DEFECTIVE_NAME_FIELD,
                DEFECTIVE_NAME_FIELD_ALT,
                DEFECTIVE_NAME_FIELD_SNAKE,
                ORDER_NG_NAME_FIELD,
                NG_NAME_FIELD);
        String defectiveType = firstNonEmptyValue(valueNode,
                ORDER_NG_TYPE_FIELD,
                NG_TYPE_FIELD,
                STATUS_FIELD,
                DEFECTIVE_CODE_FIELD,
                DEFECTIVE_CODE_FIELD_ALT,
                DEFECTIVE_CODE_FIELD_SNAKE);
        if (!StringUtils.hasText(defectiveType)) {
            defectiveType = DEFECTIVE_STATUS_VALUE;
        }

        if (quantity == null || defectiveCode == null || defectiveName == null) {
            return null;
        }

        return DefectiveTelemetryPayload.builder()
                .equipmentId(firstLong(valueNode, EQUIPMENT_ID_FIELD, EQUIPMENT_ID_FIELD_SNAKE))
                .equipmentCode(firstNonEmptyValue(valueNode, EQUIPMENT_CODE_FIELD, EQUIPMENT_CODE_FIELD_SNAKE))
                .defectiveCode(defectiveCode)
                .defectiveName(defectiveName)
                .defectiveQuantity(quantity)
                .orderNo(orderNo)
                .status(resolveStatus(valueNode))
                .defectiveType(defectiveType)
                .build();
    }

    private OrderSummaryTelemetryPayload buildOrderSummaryPayload(JsonNode summaryNode) {
        String equipmentCode = firstNonEmptyValue(summaryNode,
                ORDER_SUMMARY_EQUIPMENT_CODE_FIELD,
                EQUIPMENT_CODE_FIELD,
                EQUIPMENT_CODE_FIELD_SNAKE);
        BigDecimal producedQuantity = firstDecimal(summaryNode,
                ORDER_SUMMARY_PRODUCED_QTY_FIELD,
                ORDER_PRODUCED_QTY_FIELD);
        BigDecimal defectiveQuantity = firstDecimal(summaryNode,
                ORDER_SUMMARY_DEFECTIVE_QTY_FIELD,
                ORDER_NG_QTY_FIELD,
                NG_QTY_FIELD);
        String orderNo = firstNonEmptyValue(summaryNode, PRODUCTION_PERFORMANCE_ORDER_NO_FIELD, "order_no");
        String status = firstNonEmptyValue(summaryNode, STATUS_FIELD, ORDER_NG_STATUS_FIELD);
        String compressedSerials = extractCompressedSerials(summaryNode);
        List<String> goodSerials = extractGoodSerials(summaryNode, compressedSerials);
        if (!StringUtils.hasText(equipmentCode) || producedQuantity == null) {
            return null;
        }
        return OrderSummaryTelemetryPayload.builder()
                .equipmentCode(equipmentCode)
                .producedQuantity(producedQuantity)
                .defectiveQuantity(defectiveQuantity)
                .orderNo(orderNo)
                .status(status)
                .goodSerials(goodSerials)
                .goodSerialsGzip(compressedSerials)
                .build();
    }

    private String extractCompressedSerials(JsonNode summaryNode) {
        JsonNode compressedNode = summaryNode.get("good_serials_gzip");
        if (compressedNode != null && compressedNode.isTextual()) {
            return compressedNode.asText();
        }
        return null;
    }

    private List<String> extractGoodSerials(JsonNode summaryNode, String compressedValue) {
        if (StringUtils.hasText(compressedValue)) {
            List<String> serials = decompressSerials(compressedValue);
            if (!serials.isEmpty()) {
                return serials;
            }
        }
        JsonNode serialsNode = summaryNode.get("good_serials");
        if (serialsNode == null || !serialsNode.isArray()) {
            return Collections.emptyList();
        }
        List<String> serials = new ArrayList<>();
        for (JsonNode serialNode : serialsNode) {
            if (serialNode.isTextual()) {
                serials.add(serialNode.asText());
            }
        }
        return serials;
    }

    private List<String> decompressSerials(String compressed) {
        if (!StringUtils.hasText(compressed)) {
            return Collections.emptyList();
        }
        try {
            byte[] gzipped = Base64.getDecoder().decode(compressed);
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
                byte[] buffer = gzip.readAllBytes();
                String json = new String(buffer, StandardCharsets.UTF_8);
                JsonNode node = objectMapper.readTree(json);
                if (node == null || !node.isArray()) {
                    return Collections.emptyList();
                }
                List<String> serials = new ArrayList<>();
                for (JsonNode entry : node) {
                    if (entry.isTextual()) {
                        serials.add(entry.asText());
                    }
                }
                return serials;
            }
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Failed to decompress good_serials payload", ex);
            return Collections.emptyList();
        }
    }

    private ProductionPerformanceTelemetryPayload buildProductionPerformancePayload(JsonNode performanceNode) {
        String orderNo = firstNonEmptyValue(performanceNode, PRODUCTION_PERFORMANCE_ORDER_NO_FIELD);
        BigDecimal producedQty = firstDecimal(performanceNode, ORDER_PRODUCED_QTY_FIELD);
        BigDecimal ngCount = firstDecimal(performanceNode,
                PRODUCTION_PERFORMANCE_NG_COUNT_FIELD,
                ORDER_SUMMARY_DEFECTIVE_QTY_FIELD,
                ORDER_NG_QTY_FIELD,
                NG_QTY_FIELD);
        LocalDateTime executeAt = parseDateTime(firstNonEmptyValue(performanceNode, PRODUCTION_PERFORMANCE_EXECUTE_AT_FIELD));
        LocalDateTime waitingAckAt = parseDateTime(firstNonEmptyValue(performanceNode, PRODUCTION_PERFORMANCE_WAITING_ACK_AT_FIELD));

        if (!StringUtils.hasText(orderNo) || producedQty == null || executeAt == null || waitingAckAt == null) {
            return null;
        }

        return ProductionPerformanceTelemetryPayload.builder()
                .orderNo(orderNo)
                .orderProducedQty(producedQty)
                .ngCount(ngCount)
                .executeAt(executeAt)
                .waitingAckAt(waitingAckAt)
                .build();
    }

    private void persistNgTypeCounters(JsonNode payload) {
        String equipmentCode = firstNonEmptyValue(payload,
                ORDER_SUMMARY_EQUIPMENT_CODE_FIELD,
                EQUIPMENT_CODE_FIELD,
                EQUIPMENT_CODE_FIELD_SNAKE);
        String orderNo = firstNonEmptyValue(payload, PRODUCTION_PERFORMANCE_ORDER_NO_FIELD);
        if (!StringUtils.hasText(equipmentCode) || !StringUtils.hasText(orderNo)) {
            log.warn("타입별 NG 페이로드에 equipmentCode/order_no가 없어 저장하지 않습니다. payload={}", payload);
            return;
        }
        JsonNode typesNode = payload.path("types");
        if (!typesNode.isArray()) {
            log.warn("타입별 NG 페이로드에 types 배열이 없어 저장하지 않습니다. payload={}", payload);
            return;
        }
        for (JsonNode typeEntry : typesNode) {
            int type = typeEntry.path("type").asInt(-1);
            BigDecimal quantity = firstDecimal(typeEntry, ORDER_NG_QTY_FIELD, VALUE_FIELD, "qty");
            boolean validType = type >= 1 && type <= 4;
            boolean validQuantity = quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
            if (!(validType && validQuantity)) {
                continue;
            }
            String name = firstNonEmptyValue(typeEntry, ORDER_NG_NAME_FIELD, "name");
            if (!StringUtils.hasText(name)) {
                name = "NG_TYPE_" + type;
            }
            DefectiveTelemetryPayload defectivePayload = DefectiveTelemetryPayload.builder()
                    .equipmentCode(equipmentCode)
                    .defectiveCode(String.valueOf(type))
                    .defectiveName(name)
                    .defectiveQuantity(quantity)
                    .orderNo(orderNo)
                    .status(DEFECTIVE_STATUS_VALUE)
                    .defectiveType(String.valueOf(type))
                    .build();
            mesDefectiveService.saveNgTelemetry(defectivePayload, true);
        }
    }

    private boolean isNgEventRecord(String recordKey) {
        return recordKey != null && recordKey.contains(ORDER_NG_EVENT_FIELD);
    }

    private String resolveStatus(JsonNode valueNode) {
        String status = firstNonEmptyValue(valueNode, STATUS_FIELD, ORDER_NG_STATUS_FIELD);
        if (status == null && hasOrderNgFields(valueNode)) {
            return DEFECTIVE_STATUS_VALUE;
        }
        return status;
    }

    private boolean hasAnyNonNull(JsonNode node, String... fieldNames) {
        return Arrays.stream(fieldNames)
                .anyMatch(field -> node.hasNonNull(field) && node.get(field).isValueNode());
    }

    private String firstNonEmptyValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isTextual()) {
                String text = field.asText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            } else if (field.isNumber()) {
                return field.asText();
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String trimmed = text.trim();
        try {
            return OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (DateTimeParseException offsetEx) {
            try {
                return LocalDateTime.parse(trimmed);
            } catch (DateTimeParseException localEx) {
                log.warn("시간 값을 파싱할 수 없습니다. value={}", text, localEx);
                return null;
            }
        }
    }

    private String firstTextualValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && field.isTextual()) {
            String text = field.asText().trim();
            if (!text.isEmpty()) {
                return text;
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
                    // ignore malformed number text
                }
            }
        }
        return null;
    }

    private Long resolveFactoryIdFromEnergyUsage(JsonNode valueNode) {
        String rawFactoryCode = firstNonEmptyValue(valueNode, "factoryCode", "factory_code");
        String machine = firstNonEmptyValue(valueNode, "machine", "machineId");
        String factoryCode = rawFactoryCode;
        if (!StringUtils.hasText(rawFactoryCode)) {
            factoryCode = extractFactoryCode(machine);
            if (!StringUtils.hasText(factoryCode)) {
                log.warn("Factory code missing in energy telemetry payload. machine={}, payload={}", machine, valueNode);
                return null;
            }
        }
        log.debug("Telemetry factory resolution attempt. rawFactoryCode={}, extractedFactoryCode={}, machine={}",
                rawFactoryCode,
                factoryCode,
                machine);
        final String resolvedFactoryCode = factoryCode;
        return factoryRepository.findByFactoryCode(resolvedFactoryCode)
                .map(factory -> {
                    Long factoryId = factory.getId();
                    log.info("Resolved factory for telemetry. factoryCode={}, factoryId={}", resolvedFactoryCode, factoryId);
                    return factoryId;
                })
                .orElseGet(() -> {
                    log.warn("Factory not found for telemetry payload. factoryCode={}, machine={}, payload={}",
                            resolvedFactoryCode,
                            machine,
                            valueNode);
                    return null;
                });
    }

    private String extractFactoryCode(String machine) {
        if (!StringUtils.hasText(machine)) {
            return null;
        }
        int separatorIndex = machine.indexOf('.');
        if (separatorIndex <= 0) {
            return null;
        }
        return machine.substring(0, separatorIndex);
    }

    private record AggregationKey(long bucketTimestamp, Long factoryId) implements Comparable<AggregationKey> {

        @Override
        public int compareTo(AggregationKey other) {
            int timestampCompare = Long.compare(this.bucketTimestamp, other.bucketTimestamp);
            if (timestampCompare != 0) {
                return timestampCompare;
            }
            if (this.factoryId == null && other.factoryId == null) {
                return 0;
            }
            if (this.factoryId == null) {
                return -1;
            }
            if (other.factoryId == null) {
                return 1;
            }
            return Long.compare(this.factoryId, other.factoryId);
        }
    }

    private BigDecimal firstDecimal(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            BigDecimal decimal = toBigDecimal(node.get(fieldName));
            if (decimal != null) {
                return decimal;
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(JsonNode field) {
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isNumber()) {
            return field.decimalValue();
        }
        if (field.isTextual()) {
            String text = field.asText().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ignored) {
                // ignore malformed decimal text
            }
        }
        return null;
    }

    private boolean hasOrderNgFields(JsonNode node) {
        return node.has(ORDER_NG_QTY_FIELD)
                || node.has(ORDER_NG_NAME_FIELD)
                || node.has(ORDER_NG_TYPE_FIELD)
                || node.has(ORDER_NG_CODE_FIELD)
                || node.has(NG_QTY_FIELD)
                || node.has(NG_NAME_FIELD)
                || node.has(NG_TYPE_FIELD);
    }

    private JsonNode unwrapOrderNgEvent(JsonNode valueNode) {
        if (!valueNode.has(ORDER_NG_EVENT_FIELD)) {
            return valueNode;
        }
        JsonNode eventNode = valueNode.get(ORDER_NG_EVENT_FIELD);
        if (eventNode.isObject()) {
            return eventNode;
        }
        if (eventNode.isTextual()) {
            JsonNode parsed = parseMapLikeString(eventNode.asText());
            return parsed != null ? parsed : valueNode;
        }
        return valueNode;
    }

    private JsonNode extractOrderSummaryPayload(JsonNode valueNode) {
        if (valueNode == null) {
            return null;
        }
        JsonNode directNode = parseOrderSummaryNode(valueNode.get(ORDER_SUMMARY_PAYLOAD_FIELD));
        if (directNode != null) {
            return directNode;
        }
        if (ORDER_SUMMARY_PAYLOAD_FIELD.equals(valueNode.path("tag").asText()) && valueNode.has(VALUE_FIELD)) {
            return parseOrderSummaryNode(valueNode.get(VALUE_FIELD));
        }
        return null;
    }

    private JsonNode extractNgTypeCountersPayload(JsonNode valueNode) {
        if (valueNode == null) {
            return null;
        }
        JsonNode directNode = parseTelemetryPayloadNode(valueNode.get(ORDER_NG_TYPES_PAYLOAD_FIELD));
        if (directNode != null) {
            return directNode;
        }
        if (ORDER_NG_TYPES_PAYLOAD_FIELD.equals(valueNode.path("tag").asText()) && valueNode.has(VALUE_FIELD)) {
            return parseTelemetryPayloadNode(valueNode.get(VALUE_FIELD));
        }
        return null;
    }

    private JsonNode extractProductionPerformancePayload(JsonNode valueNode) {
        if (valueNode == null) {
            return null;
        }
        JsonNode directNode = parseProductionPerformanceNode(valueNode.get(PRODUCTION_PERFORMANCE_PAYLOAD_FIELD));
        if (directNode != null) {
            return directNode;
        }
        if (PRODUCTION_PERFORMANCE_PAYLOAD_FIELD.equals(valueNode.path("tag").asText()) && valueNode.has(VALUE_FIELD)) {
            return parseProductionPerformanceNode(valueNode.get(VALUE_FIELD));
        }
        return null;
    }

    private JsonNode parseOrderSummaryNode(JsonNode node) {
        return parseTelemetryPayloadNode(node);
    }

    private JsonNode parseProductionPerformanceNode(JsonNode node) {
        JsonNode parsed = parseTelemetryPayloadNode(node);
        if (parsed != null && !parsed.hasNonNull(PRODUCTION_PERFORMANCE_ORDER_NO_FIELD)) {
            return null;
        }
        return parsed;
    }

    private JsonNode parseTelemetryPayloadNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            return node;
        }
        if (node.isTextual()) {
            JsonNode parsed = parsePayload(node.asText());
            if (parsed == null) {
                parsed = parseMapLikeString(node.asText());
            }
            return parsed;
        }
        return null;
    }

    private JsonNode convertTextNodeToObject(JsonNode valueNode) {
        if (valueNode == null || !valueNode.isTextual()) {
            return valueNode;
        }
        JsonNode parsed = parsePayload(valueNode.asText());
        return parsed != null ? parsed : valueNode;
    }

    private JsonNode extractAlarmPayload(JsonNode valueNode, String recordKey) {
        JsonNode node = convertTextNodeToObject(valueNode);
        if (node == null) {
            return null;
        }
        if (node.has("tag")
                && ALARM_EVENT_KEY_SUFFIX.equals(node.get("tag").asText())
                && node.hasNonNull(VALUE_FIELD)) {
            return convertTextNodeToObject(node.get(VALUE_FIELD));
        }
        if (recordKey != null && recordKey.contains(ALARM_EVENT_KEY_SUFFIX)
                && node.hasNonNull(VALUE_FIELD)) {
            return convertTextNodeToObject(node.get(VALUE_FIELD));
        }
        return node;
    }

    private JsonNode parseMapLikeString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] tokens = trimmed.split(",");
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (String token : tokens) {
            String[] pair = token.split("=", 2);
            if (pair.length == 2) {
                String key = pair[0].trim();
                String value = pair[1].trim();
                if (!key.isEmpty()) {
                    objectNode.put(key, value);
                }
            }
        }
        return objectNode;
    }
}
