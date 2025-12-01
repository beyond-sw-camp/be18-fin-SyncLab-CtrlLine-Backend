package com.beyond.synclab.ctrlline.domain.telemetry.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TelemetryErrorCode implements ErrorCode {
    ENERGY_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "ENERGY_DATA_NOT_FOUND", "해당 공장의 전력 소모량 데이터가 없습니다."),
    ENVIRONMENT_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "ENVIRONMENT_DATA_NOT_FOUND", "해당 공장의 온습도 데이터가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TelemetryErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
