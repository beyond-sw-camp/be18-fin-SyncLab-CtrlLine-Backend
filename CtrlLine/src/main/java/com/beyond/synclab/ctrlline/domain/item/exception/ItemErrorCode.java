package com.beyond.synclab.ctrlline.domain.item.exception;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ItemErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", "해당 품목을 찾을 수 없습니다."),
    ITEMCODE_CONFLICT(HttpStatus.CONFLICT, "ITEMCODE_CONFLICT", "이미 존재하는 품목코드입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ItemErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
