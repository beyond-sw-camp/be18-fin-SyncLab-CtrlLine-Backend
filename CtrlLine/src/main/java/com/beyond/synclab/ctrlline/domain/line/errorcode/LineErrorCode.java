package com.beyond.synclab.ctrlline.domain.line.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LineErrorCode implements ErrorCode {
    // 404
    LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "LINE_NOT_FOUND", "라인을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
