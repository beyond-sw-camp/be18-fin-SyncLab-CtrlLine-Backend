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
    LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "LINE_NOT_FOUND", "라인을 찾을 수 없습니다."),
    NO_EQUIPMENT_FOUND(HttpStatus.NOT_FOUND, "NO_EQUIPMENT_FOUND", "라인에 해당하는 설비를 찾을 수 없습니다."),

    // 500
    INVALID_EQUIPMENT_PPM(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_EQUIPMENT_PPM", "유효 설비 PPM계산이 불가합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
