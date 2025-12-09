package com.beyond.synclab.ctrlline.domain.telemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.equipment.entity.Equipments;
import com.beyond.synclab.ctrlline.domain.equipment.repository.EquipmentRepository;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentRuntimeStatusService;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.AlarmTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.DefectiveTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.EquipmentStatusTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;
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

    @Mock
    private MesProductionPerformanceService mesProductionPerformanceService;

    @Mock
    private EquipmentRuntimeStatusService equipmentRuntimeStatusService;

    @Mock
    private FactoryEnvironmentService factoryEnvironmentService;

    private MesTelemetryListener listener;

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @BeforeEach
    void setUp() {
        listener = new MesTelemetryListener(
                new ObjectMapper(),
                factoryRepository,
                equipmentRepository,
                mesPowerConsumptionService,
                mesDefectiveService,
                mesAlarmService,
                mesProductionPerformanceService,
                equipmentRuntimeStatusService,
                factoryEnvironmentService
        );
        when(equipmentRepository.findFirstByLine_LineCodeIgnoreCaseAndEquipmentNameIgnoreCase(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(equipmentRepository.findByEquipmentCode(anyString())).thenReturn(Optional.empty());
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
        Optional<Factories> factory = Optional.of(Factories.builder()
                .id(1L)
                .factoryCode("F0001")
                .factoryName("Factory01")
                .isActive(true)
                .build());
        when(factoryRepository.findByFactoryCode("F0001")).thenReturn(factory);

        String firstPayload = """
                {"records":[
                    {"value":{"machine":"F0001.M-01","tag":"energy_usage","value":0.5,"timestamp":1762756823000}},
                    {"value":{"machine":"F0001.M-02","tag":"energy_usage","value":1.0,"timestamp":1762756823000}}
                ]}
                """;
        String triggerFlushPayload = """
                {"records":[
                    {"value":{"machine":"F0001.M-03","tag":"energy_usage","value":2.0,"timestamp":1762756825000}}
                ]}
                """;

        listener.onTelemetry(consumerRecord(firstPayload));
        listener.onTelemetry(consumerRecord(triggerFlushPayload));

        verify(mesPowerConsumptionService, times(1))
                .savePowerConsumption(BigDecimal.valueOf(1.50).setScale(2), 1L);
    }

    @Test
    void onTelemetry_updatesEquipmentRuntimeStatus() {
        String payload = """
                {"equipment_code":"EQP-500","state":"EXECUTE","alarm_level":"WARNING","alarm_active":true}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<EquipmentStatusTelemetryPayload> captor = ArgumentCaptor.forClass(EquipmentStatusTelemetryPayload.class);
        verify(equipmentRuntimeStatusService, times(1)).updateStatus(captor.capture());
        EquipmentStatusTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-500");
        assertThat(saved.state()).isEqualTo("EXECUTE");
        assertThat(saved.alarmLevel()).isEqualTo("WARNING");
        assertThat(saved.alarmActive()).isTrue();
    }

    @Test
    void onTelemetry_updatesEquipmentRuntimeStatus_whenStateIsNestedValue() {
        String payload = """
                {"records":[
                    {"value":{"tag":"state","value":{"equipment_code":"EQP-600","state":"STARTING","alarm_level":"CRITICAL","alarm_active":true}}}
                ]}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<EquipmentStatusTelemetryPayload> captor = ArgumentCaptor.forClass(EquipmentStatusTelemetryPayload.class);
        verify(equipmentRuntimeStatusService, times(1)).updateStatus(captor.capture());
        EquipmentStatusTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-600");
        assertThat(saved.state()).isEqualTo("STARTING");
        assertThat(saved.alarmLevel()).isEqualTo("CRITICAL");
        assertThat(saved.alarmActive()).isTrue();
    }

    @Test
    void onTelemetry_mapsMachineNotationToRegisteredEquipmentCode() {
        Equipments equipment = Equipments.builder()
                .equipmentCode("F2-CL2-FIP001")
                .build();
        when(equipmentRepository.findByEquipmentCode("F2-CL2-FIP001")).thenReturn(Optional.of(equipment));

        String payload = """
                {
                  "records": [
                    {
                      "value": {
                        "machine": "F0002.CL0002.FinalInspection01",
                        "tag": "state",
                        "value": {
                          "state": "EXECUTE"
                        }
                      }
                    }
                  ]
                }
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<EquipmentStatusTelemetryPayload> captor = ArgumentCaptor.forClass(EquipmentStatusTelemetryPayload.class);
        verify(equipmentRuntimeStatusService).updateStatus(captor.capture());
        assertThat(captor.getValue().equipmentCode()).isEqualTo("F2-CL2-FIP001");
    }

    @Test
    void onTelemetry_persistsEnvironmentRecord() {
        Factories factory = Factories.builder()
                .id(1L)
                .factoryCode("F0001")
                .factoryName("Factory01")
                .isActive(true)
                .build();
        when(factoryRepository.findByFactoryCode("F0001")).thenReturn(Optional.of(factory));

        String payload = """
                {"machine":"F0001.CL0001","tag":"environment","value":{"temperature":23.56,"humidity":45.12,"timestamp":1764550749000}}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<BigDecimal> temperatureCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> humidityCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(factoryEnvironmentService, times(1))
                .saveReading(eq(1L), temperatureCaptor.capture(), humidityCaptor.capture(), any());
        assertThat(temperatureCaptor.getValue()).isEqualByComparingTo("23.56");
        assertThat(humidityCaptor.getValue()).isEqualByComparingTo("45.12");
    }

    @Test
    void onTelemetry_persistsNgDefectiveRecord() {
        String payload = """
                {"records":[
                    {"value":{"status":"OK","equipmentId":7,"defectiveCode":"DF-00","defectiveName":"Scratch","quantity":1}},
                    {"value":{"status":"NG","equipmentId":10,"equipmentCode":"EQP-10","defectiveCode":"DF-01","defectiveName":"Dent","quantity":3.5,"order_no":"PLAN-100"}}
                ]}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture(), anyBoolean());
        DefectiveTelemetryPayload savedPayload = captor.getValue();
        assertThat(savedPayload.equipmentId()).isEqualTo(10L);
        assertThat(savedPayload.orderNo()).isEqualTo("PLAN-100");
        assertThat(savedPayload.defectiveQuantity()).isEqualByComparingTo("3.5");
    }

    @Test
    void onTelemetry_identifiesOrderNgPayloadWithoutStatus() {
        String payload = """
                {"order_ng_type":4,"order_ng_qty":6,"order_ng_name":"코팅 두께 불량","order_no":"PLAN-200"}
                """;

        JsonNode node = ReflectionTestUtils.invokeMethod(listener, "parsePayload", payload);
        Boolean identified = ReflectionTestUtils.invokeMethod(listener, "isNgDefectiveRecord", node);
        assertThat(identified).isTrue();
        DefectiveTelemetryPayload builtPayload = ReflectionTestUtils.invokeMethod(listener, "buildDefectivePayload", node);
        assertThat(builtPayload).isNotNull();
        assertThat(builtPayload.orderNo()).isEqualTo("PLAN-200");

        listener.onTelemetry(consumerRecord(payload));

        verify(mesDefectiveService, times(1)).saveNgTelemetry(any(), anyBoolean());
    }

    @Test
    void onTelemetry_identifiesNgPayloadWithSnakeCaseFields() {
        String payload = """
                {"equipmentId":15,"ng_type":2,"ng_qty":3,"ng_name":"셀 정렬 불량","order_no":"PLAN-300"}
                """;

        listener.onTelemetry(consumerRecord(payload));

        verify(mesDefectiveService, times(1)).saveNgTelemetry(any(), anyBoolean());
    }

    @Test
    void onTelemetry_handlesOrderNgEventMapString() {
        String payload = """
                {"order_ng_event":"{equipmentId=F0001.CL0001.ModuleAndPackUnit01, ng_type=4, ng_name=체결 토크 불량, ng_qty=7}"}
                """;

        listener.onTelemetry(consumerRecord(payload));

        verify(mesDefectiveService, times(1)).saveNgTelemetry(any(), anyBoolean());
    }

    @Test
    void onTelemetry_handlesOrderSummaryPayload() {
        String compressed = gzipBase64("[\"SR-001\",\"SR-002\"]");
        String payload = String.format("{\"order_summary_payload\":{\"equipment_code\":\"EQP-20\",\"order_no\":\"PLAN-900\",\"status\":\"WAITING_ACK\",\"produced_qty\":120,\"ng_qty\":5,\"good_serials_gzip\":\"%s\"}}",
                compressed);

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<OrderSummaryTelemetryPayload> captor = ArgumentCaptor.forClass(OrderSummaryTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveOrderSummaryTelemetry(captor.capture());
        OrderSummaryTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-20");
        assertThat(saved.producedQuantity()).isEqualByComparingTo("120");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("5");
        assertThat(saved.orderNo()).isEqualTo("PLAN-900");
        assertThat(saved.status()).isEqualTo("WAITING_ACK");
        assertThat(saved.goodSerials()).containsExactly("SR-001", "SR-002");
        assertThat(saved.goodSerialsGzip()).isEqualTo(compressed);
    }

    @Test
    void onTelemetry_handlesOrderSummaryTagPayload() {
        String compressed = gzipBase64("[\"SR-010\"]");
        String payload = String.format("{\"records\":[{\"value\":{\"machine\":\"F0001.CL0001.TrayCleaner01\",\"tag\":\"order_summary_payload\",\"value\":{\"equipmentCode\":\"EQP-30\",\"order_no\":\"PLAN-901\",\"status\":\"WAITING_ACK\",\"order_produced_qty\":50,\"order_ng_qty\":4,\"good_serials_gzip\":\"%s\"}}}]}",
                compressed);

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<OrderSummaryTelemetryPayload> captor = ArgumentCaptor.forClass(OrderSummaryTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveOrderSummaryTelemetry(captor.capture());
        OrderSummaryTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("EQP-30");
        assertThat(saved.producedQuantity()).isEqualByComparingTo("50");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("4");
        assertThat(saved.orderNo()).isEqualTo("PLAN-901");
        assertThat(saved.status()).isEqualTo("WAITING_ACK");
        assertThat(saved.goodSerials()).containsExactly("SR-010");
        assertThat(saved.goodSerialsGzip()).isEqualTo(compressed);
    }

    @Test
    void onTelemetry_usesMachineFieldWhenEquipmentCodeMissing() {
        String payload = """
                {
                  "records": [
                    {
                      "value": {
                        "machine": "F0001.CL0001.FinalInspection01",
                        "tag": "order_summary_payload",
                        "value": {
                          "equipmentCode": "",
                          "order_no": "PLAN-905",
                          "status": "EXECUTE",
                          "order_produced_qty": 31,
                          "order_ng_qty": 2
                        }
                      }
                    }
                  ]
                }
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<OrderSummaryTelemetryPayload> captor = ArgumentCaptor.forClass(OrderSummaryTelemetryPayload.class);
        verify(mesDefectiveService).saveOrderSummaryTelemetry(captor.capture());
        OrderSummaryTelemetryPayload saved = captor.getValue();
        assertThat(saved.equipmentCode()).isEqualTo("F0001.CL0001.FinalInspection01");
        assertThat(saved.producedQuantity()).isEqualByComparingTo("31");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("2");
        assertThat(saved.orderNo()).isEqualTo("PLAN-905");
    }

    @Test
    void onTelemetry_handlesNgTypePayload() {
        String payload = """
                {"order_ng_types_payload":{"equipment_code":"EQP-40","order_no":"PLAN-777","types":[{"type":1,"name":"Type1","qty":5},{"type":3,"name":"Type3","qty":0}]}}
                """;

        listener.onTelemetry(consumerRecord(payload));

        ArgumentCaptor<DefectiveTelemetryPayload> captor = ArgumentCaptor.forClass(DefectiveTelemetryPayload.class);
        verify(mesDefectiveService, times(1)).saveNgTelemetry(captor.capture(), eq(true));
        DefectiveTelemetryPayload saved = captor.getValue();
        assertThat(saved.orderNo()).isEqualTo("PLAN-777");
        assertThat(saved.defectiveCode()).isEqualTo("1");
        assertThat(saved.defectiveQuantity()).isEqualByComparingTo("5");
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

    private String gzipBase64(String json) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(json.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
