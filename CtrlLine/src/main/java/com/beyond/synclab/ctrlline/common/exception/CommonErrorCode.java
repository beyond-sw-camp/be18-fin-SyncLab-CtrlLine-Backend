package com.beyond.synclab.ctrlline.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 에러"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력 값"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청"),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "예상치 못한 에러"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER_NOT_FOUND" , "해당 사용자를 찾을 수 없습니다."),

    FACTORY_CONFLICT(HttpStatus.CONFLICT, "FACTORY_CONFLICT", "이미 존재하는 공장코드입니다."),
    FACTORY_NOT_FOUND(HttpStatus.NOT_FOUND,"FACTORY_NOT_FOUND" , "해당 공장을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

