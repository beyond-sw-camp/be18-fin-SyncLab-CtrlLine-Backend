package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
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

@ExtendWith(MockitoExtension.class)
class MesTelemetryListenerTest {

    @Mock
    private MesPowerConsumptionService mesPowerConsumptionService;

    @Mock
    private MesDefectiveService mesDefectiveService;

    @Mock
    private MesAlarmService mesAlarmService;

    private MesTelemetryListener listener;

    @BeforeEach
    void setUp() {
        listener = new MesTelemetryListener(new ObjectMapper(), mesPowerConsumptionService, mesDefectiveService, mesAlarmService);
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

    @Test
    void onTelemetry_savesOrderNgPayloadWithoutStatus() {
        String payload = """
                {"order_ng_type":4,"order_ng_qty":6,"order_ng_name":"코팅 두께 불량"}
                """;

        JsonNode node = ReflectionTestUtils.invokeMethod(listener, "parsePayload", payload);
        Boolean identified = ReflectionTestUtils.invokeMethod(listener, "isNgDefectiveRecord", node);
        assertThat(identified).isTrue();
        DefectiveTelemetryPayload builtPayload = ReflectionTestUtils.invokeMethod(listener, "buildDefectivePayload", node);
        assertThat(builtPayload).isNotNull();

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture());
        DefectiveTelemetryPayload savedPayload = captor.getValue();
        assertThat(savedPayload.defectiveCode()).isEqualTo("4");
        assertThat(savedPayload.defectiveName()).isEqualTo("코팅 두께 불량");
        assertThat(savedPayload.defectiveQuantity()).isEqualByComparingTo("6");
        assertThat(savedPayload.status()).isEqualTo("NG");
    }

    @Test
    void onTelemetry_savesNgPayloadWithSnakeCaseFields() {
        String payload = """
                {"equipmentId":15,"ng_type":2,"ng_qty":3,"ng_name":"셀 정렬 불량"}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture());
        DefectiveTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentId()).isEqualTo(15L);
        assertThat(saved.defectiveCode()).isEqualTo("2");
        assertThat(saved.defectiveName()).isEqualTo("셀 정렬 불량");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("3");
        assertThat(saved.status()).isEqualTo("NG");
    }

    @Test
    void onTelemetry_handlesOrderNgEventMapString() {
        String payload = """
                {"order_ng_event":"{equipmentId=F0001.CL0001.ModuleAndPackUnit01, ng_type=4, ng_name=체결 토크 불량, ng_qty=7}"}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture());
        DefectiveTelemetryPayload saved = captor.getValue();
        assertThat(saved.defectiveCode()).isEqualTo("4");
        assertThat(saved.defectiveName()).isEqualTo("체결 토크 불량");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("7");
    }

    @Test
    void onTelemetry_handlesOrderSummaryPayload() {
        String payload = """
                {"order_summary_payload":{"equipment_code":"EQP-20","produced_qty":120,"ng_qty":5}}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<OrderSummaryTelemetryPayload> captor = ArgumentCaptor.forClass(OrderSummaryTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveOrderSummaryTelemetry(captor.capture());
        OrderSummaryTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-20");
        assertThat(saved.producedQuantity()).isEqualByComparingTo("120");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("5");
    }

    @Test
    void onTelemetry_handlesOrderSummaryTagPayload() {
        String payload = """
                {"records":[{"value":{"machine":"F0001.CL0001.TrayCleaner01","tag":"order_summary_payload","value":{"equipmentCode":"EQP-30","order_produced_qty":50,"order_ng_qty":4}}}]}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<OrderSummaryTelemetryPayload> captor = ArgumentCaptor.forClass(OrderSummaryTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveOrderSummaryTelemetry(captor.capture());
        OrderSummaryTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-30");
        assertThat(saved.producedQuantity()).isEqualByComparingTo("50");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("4");
    }

    @Test
    void onTelemetry_savesAlarmPayload() {
        String payload = """
                {"equipmentCode":"F1-CL1-EU001","alarm_code":"TC01","alarm_type":2,"alarm_name":"슬러리 공급 부족","alarm_level":"WARNING","occurred_at":"2025-11-17T11:33:44.456881+09:00","cleared_at":"","user":null}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<AlarmTelemetryPayload> captor = ArgumentCaptor.forClass(AlarmTelemetryPayload.class);
        verify(mesAlarmService, times(1)).saveAlarmTelemetry(captor.capture());
        AlarmTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("F1-CL1-EU001");
        assertThat(saved.alarmCode()).isEqualTo("TC01");
        assertThat(saved.alarmType()).isEqualTo("2");
        assertThat(saved.alarmName()).isEqualTo("슬러리 공급 부족");
        assertThat(saved.alarmLevel()).isEqualTo("WARNING");
        assertThat(saved.occurredAt()).isNotNull();
        assertThat(saved.clearedAt()).isNull();
    }

    @Test
    void onTelemetry_savesAlarmPayloadWhenNestedUnderValue() {
        String payload = """
                {"records":[
                    {"value":{
                        "machine":"F0001.CL0001.AssemblyUnit01",
                        "tag":"alarm_event_payload",
                        "value":"{\\"equipmentCode\\":\\"F1-CL1-EU003\\",\\"alarm_code\\":\\"HOT01\\",\\"alarm_type\\":1,\\"alarm_name\\":\\"온도 상승\\",\\"alarm_level\\":\\"WARNING\\",\\"occurred_at\\":\\"2025-11-17T12:00:00+09:00\\"}"
                    }}
                ]}
                """;

        listener.onTelemetry(new ConsumerRecord<>("mes-machine-telemetry", 0, 0L,
                "F0001.CL0001.AssemblyUnit01.alarm_event_payload", payload));

        ArgumentCaptor<AlarmTelemetryPayload> captor = ArgumentCaptor.forClass(AlarmTelemetryPayload.class);
        verify(mesAlarmService, times(1)).saveAlarmTelemetry(captor.capture());
        AlarmTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("F1-CL1-EU003");
        assertThat(saved.alarmName()).isEqualTo("온도 상승");
        assertThat(saved.alarmCode()).isEqualTo("HOT01");
    }

    private ConsumerRecord<String, String> consumerRecord(String payload) {
        return new ConsumerRecord<>("mes-machine-telemetry", 0, 0L, "key", payload);
    }
}
