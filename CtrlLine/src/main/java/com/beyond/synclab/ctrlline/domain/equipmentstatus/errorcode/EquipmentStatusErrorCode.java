package com.beyond.synclab.ctrlline.domain.equipmentstatus.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter

public enum EquipmentStatusErrorCode implements ErrorCode {

    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "필수 입력 값이 누락되었거나 요청 형식이 올바르지 않습니다."),
    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 토큰이 없거나 유효하지 않습니다."),
    // 403
    FORBIDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "관리자 권한이 아닙니다."),
    // 404
    EQUIPMENT_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND,"EQUIPMENT_STATUS_NOT_FOUND" , "해당 설비 상태를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    EquipmentStatusErrorCode(HttpStatus status, String code, String message) {
                this.status = status;
                this.code = code;
                this.message = message;
            }

}
