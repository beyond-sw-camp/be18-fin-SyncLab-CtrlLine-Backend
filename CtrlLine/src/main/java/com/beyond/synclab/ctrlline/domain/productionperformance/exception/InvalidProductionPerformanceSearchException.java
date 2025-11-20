// file: src/main/java/com/beyond/synclab/ctrlline/domain/productionperformance/exception/InvalidProductionPerformanceSearchException.java
package com.beyond.synclab.ctrlline.domain.productionperformance.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class InvalidProductionPerformanceSearchException extends AppException {

    public InvalidProductionPerformanceSearchException() {
        super(ProductionPerformanceErrorCode.INVALID_SEARCH_PARAMETER);
    }

    public InvalidProductionPerformanceSearchException(String message) {
        super(ProductionPerformanceErrorCode.INVALID_SEARCH_PARAMETER);
    }
}
