// file: src/main/java/com/beyond/synclab/ctrlline/domain/productionperformance/exception/ProductionPerformanceErrorCode.java
package com.beyond.synclab.ctrlline.domain.productionperformance.exception;

import com.beyond.synclab.ctrlline.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductionPerformanceErrorCode implements ErrorCode {

    PRODUCTION_PERFORMANCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCTION_PERFORMANCE_NOT_FOUND",
            "해당 생산실적을 찾을 수 없습니다."
    ),

    INVALID_SEARCH_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "INVALID_SEARCH_PARAMETER",
            "검색 조건이 유효하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ProductionPerformanceErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
