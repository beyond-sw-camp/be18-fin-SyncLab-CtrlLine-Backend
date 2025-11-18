package com.beyond.synclab.ctrlline.domain.itemline.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ItemLineErrorCode implements ErrorCode {
    ITEM_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM_LINE_NOT_FOUND", "존재하지 않는 ITEM_LINE 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
