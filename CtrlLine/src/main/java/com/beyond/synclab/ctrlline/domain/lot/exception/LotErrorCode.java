package com.beyond.synclab.ctrlline.domain.lot.exception;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum LotErrorCode implements ErrorCode {

    LOT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "LOT_NOT_FOUND",
            "해당 LOT를 찾을 수 없습니다."
    ),

    INVALID_SEARCH_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "INVALID_SEARCH_PARAMETER",
            "검색 조건이 유효하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    // ★ enum 생성자명은 반드시 LotErrorCode여야 함
    LotErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
