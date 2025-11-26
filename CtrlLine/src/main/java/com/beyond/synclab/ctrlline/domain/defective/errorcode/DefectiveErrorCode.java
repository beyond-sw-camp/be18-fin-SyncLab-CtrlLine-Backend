package com.beyond.synclab.ctrlline.domain.defective.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DefectiveErrorCode implements ErrorCode {
    PLAN_DEFECTIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_DEFECTIVE_NOT_FOUND", "계획 불량을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
