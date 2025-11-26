package com.beyond.synclab.ctrlline.domain.productionperformance.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class ProductionPerformanceNotFoundException extends AppException {
    public ProductionPerformanceNotFoundException() {
        super(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
    }
}
