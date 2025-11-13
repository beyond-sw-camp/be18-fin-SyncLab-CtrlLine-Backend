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
}
