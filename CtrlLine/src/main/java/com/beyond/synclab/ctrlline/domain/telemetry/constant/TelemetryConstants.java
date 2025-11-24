package com.beyond.synclab.ctrlline.domain.telemetry.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TelemetryConstants {

    public static final String ENERGY_USAGE_TAG = "energy_usage";
    public static final String VALUE_FIELD = "value";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String STATUS_FIELD = "status";
    public static final String DEFECTIVE_STATUS_VALUE = "NG";
    public static final String EQUIPMENT_ID_FIELD = "equipmentId";
    public static final String EQUIPMENT_ID_FIELD_SNAKE = "equipment_id";
    public static final String EQUIPMENT_CODE_FIELD = "equipmentCode";
    public static final String EQUIPMENT_CODE_FIELD_SNAKE = "equipment_code";
    public static final String DEFECTIVE_CODE_FIELD = "defectiveCode";
    public static final String DEFECTIVE_CODE_FIELD_ALT = "defectCode";
    public static final String DEFECTIVE_CODE_FIELD_SNAKE = "defective_code";
    public static final String DEFECTIVE_NAME_FIELD = "defectiveName";
    public static final String DEFECTIVE_NAME_FIELD_ALT = "defectName";
    public static final String DEFECTIVE_NAME_FIELD_SNAKE = "defective_name";
    public static final String DEFECTIVE_QTY_FIELD = "quantity";
    public static final String DEFECTIVE_QTY_FIELD_ALT = "defectiveQty";
    public static final String DEFECTIVE_QTY_FIELD_SNAKE = "defective_qty";
    public static final String ORDER_NG_TYPE_FIELD = "order_ng_type";
    public static final String ORDER_NG_CODE_FIELD = "order_ng_code";
    public static final String ORDER_NG_NAME_FIELD = "order_ng_name";
    public static final String ORDER_NG_QTY_FIELD = "order_ng_qty";
    public static final String ORDER_NG_STATUS_FIELD = "order_ng_status";
    public static final String ORDER_NG_EVENT_FIELD = "order_ng_event";
    public static final String ORDER_PRODUCED_QTY_FIELD = "order_produced_qty";
    public static final String ORDER_SUMMARY_PAYLOAD_FIELD = "order_summary_payload";
    public static final String ORDER_SUMMARY_EQUIPMENT_CODE_FIELD = "equipment_code";
    public static final String ORDER_SUMMARY_PRODUCED_QTY_FIELD = "produced_qty";
    public static final String ORDER_SUMMARY_DEFECTIVE_QTY_FIELD = "ng_qty";
    public static final String ORDER_NG_TYPES_PAYLOAD_FIELD = "order_ng_types_payload";
    public static final String PRODUCTION_PERFORMANCE_PAYLOAD_FIELD = "production_performance_payload";
    public static final String PRODUCTION_PERFORMANCE_ORDER_NO_FIELD = "order_no";
    public static final String PRODUCTION_PERFORMANCE_NG_COUNT_FIELD = "ng_count";
    public static final String PRODUCTION_PERFORMANCE_EXECUTE_AT_FIELD = "execute_at";
    public static final String PRODUCTION_PERFORMANCE_WAITING_ACK_AT_FIELD = "waiting_ack_at";
    public static final String NG_TYPE_FIELD = "ng_type";
    public static final String NG_NAME_FIELD = "ng_name";
    public static final String NG_QTY_FIELD = "ng_qty";

    public static final String ALARM_TYPE_FIELD = "alarm_type";
    public static final String ALARM_TYPE_FIELD_CAMEL = "alarmType";
    public static final String ALARM_NAME_FIELD = "alarm_name";
    public static final String ALARM_NAME_FIELD_CAMEL = "alarmName";
    public static final String ALARM_CODE_FIELD = "alarm_code";
    public static final String ALARM_CODE_FIELD_CAMEL = "alarmCode";
    public static final String ALARM_LEVEL_FIELD = "alarm_level";
    public static final String ALARM_LEVEL_FIELD_CAMEL = "alarmLevel";
    public static final String ALARM_OCCURRED_AT_FIELD = "occurred_at";
    public static final String ALARM_OCCURRED_AT_FIELD_CAMEL = "occurredAt";
    public static final String ALARM_CLEARED_AT_FIELD = "cleared_at";
    public static final String ALARM_CLEARED_AT_FIELD_CAMEL = "clearedAt";
    public static final String ALARM_USER_FIELD = "user";
    public static final String ALARM_CAUSE_FIELD = "alarm_cause";
    public static final String ALARM_CAUSE_FIELD_CAMEL = "alarmCause";
}
