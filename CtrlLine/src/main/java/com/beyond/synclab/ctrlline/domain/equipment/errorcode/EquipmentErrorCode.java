package com.beyond.synclab.ctrlline.domain.equipment.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EquipmentErrorCode implements ErrorCode {
        // 400
        BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "필수 입력 값이 누락되었거나 요청 형식이 올바르지 않습니다."),
        // 401
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 토큰이 없거나 유효하지 않습니다."),
        // 403
        FORBIDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "관리자 권한이 아닙니다."),
        // 404
        EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"EQUIPMENT_NOT_FOUND" , "해당 설비를 찾을 수 없습니다."),
        // 409
        EQUIPMENT_CONFLICT(HttpStatus.CONFLICT, "EQUIPMENT_CONFLICT", "이미 존재하는 설비코드입니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;

        EquipmentErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }

