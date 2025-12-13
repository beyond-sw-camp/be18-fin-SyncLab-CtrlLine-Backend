package com.beyond.synclab.ctrlline.domain.line.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter

public enum LineErrorCode implements ErrorCode {
    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "필수 입력 값이 누락되었거나 요청 형식이 올바르지 않습니다."),
    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 토큰이 없거나 유효하지 않습니다."),
    // 403
    FORBIDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "관리자 권한이 아닙니다."),
    // 404
    LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "LINE_NOT_FOUND", "라인을 찾을 수 없습니다."),
    // 404
    NO_EQUIPMENT_FOUND(HttpStatus.NOT_FOUND, "NO_EQUIPMENT_FOUND", "라인에 해당하는 설비를 찾을 수 없습니다."),
    // 500
    INVALID_EQUIPMENT_PPM(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_EQUIPMENT_PPM", "유효 설비 PPM계산이 불가합니다."),

    LINE_INACTIVE(HttpStatus.BAD_REQUEST, "LINE_INACTIVE", "비활성화된 라인입니다."),
    EQUIPMENT_INACTIVE(HttpStatus.BAD_REQUEST, "EQUIPMENT_INACTIVE", "해당 라인에 비활성화된 설비가 존재합니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    LineErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
