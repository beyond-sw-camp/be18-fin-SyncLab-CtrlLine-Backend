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
    public static final String NG_TYPE_FIELD = "ng_type";
    public static final String NG_NAME_FIELD = "ng_name";
    public static final String NG_QTY_FIELD = "ng_qty";
}
