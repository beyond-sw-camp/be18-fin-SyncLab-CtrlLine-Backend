package com.beyond.synclab.ctrlline.domain.productionplan.errorcode;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProductionPlanErrorCode implements ErrorCode {
    PRODUCTION_PLAN_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCTION_PLAN_FOR_BIDDEN", "생산계획을 등록할 권한이 없습니다."),
    // 404
    PRODUCTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCTION_PLAN_NOT_FOUND", "생산계획을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
