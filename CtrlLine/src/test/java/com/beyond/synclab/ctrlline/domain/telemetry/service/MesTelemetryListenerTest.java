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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;

@ExtendWith(MockitoExtension.class)
class MesTelemetryListenerTest {

    @Mock
    private MesPowerConsumptionService mesPowerConsumptionService;

    @Mock
    private MesDefectiveService mesDefectiveService;

    private MesTelemetryListener listener;

    @BeforeEach
    void setUp() {
        listener = new MesTelemetryListener(new ObjectMapper(), mesPowerConsumptionService, mesDefectiveService);
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

    @Test
    void onTelemetry_savesNgDefectiveRecord() {
        String payload = """
                {"records":[
                    {"value":{"status":"OK","equipmentId":7,"defectiveCode":"DF-00","defectiveName":"Scratch","quantity":1}},
                    {"value":{"status":"NG","equipmentId":10,"equipmentCode":"EQP-10","defectiveCode":"DF-01","defectiveName":"Dent","quantity":3.5}}
                ]}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture());
        DefectiveTelemetryPayload savedPayload = captor.getValue();
        assertThat(savedPayload.equipmentId()).isEqualTo(10L);
        assertThat(savedPayload.equipmentCode()).isEqualTo("EQP-10");
        assertThat(savedPayload.defectiveCode()).isEqualTo("DF-01");
        assertThat(savedPayload.defectiveName()).isEqualTo("Dent");
        assertThat(savedPayload.defectiveQuantity()).isEqualByComparingTo("3.5");
        assertThat(savedPayload.status()).isEqualTo("NG");
    }

    private ConsumerRecord<String, String> consumerRecord(String payload) {
        return new ConsumerRecord<>("mes-machine-telemetry", 0, 0L, "key", payload);
    }
}
