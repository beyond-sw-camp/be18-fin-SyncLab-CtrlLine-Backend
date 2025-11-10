package com.beyond.synclab.ctrlline.domain.factory.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FactoryErrorCode implements ErrorCode {
    FACTORY_NOT_FOUND(HttpStatus.NOT_FOUND,"FACTORY_NOT_FOUND" , "해당 공장을 찾을 수 없습니다."), // 404
    FACTORY_CONFLICT(HttpStatus.CONFLICT, "FACTORY_CONFLICT", "이미 존재하는 공장코드입니다."); // 409

    private final HttpStatus status;
    private final String code;
    private final String message;

    FactoryErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
