package com.beyond.synclab.ctrlline.domain.productionperformance.exception;

import com.beyond.synclab.ctrlline.common.exception.AppException;

public class ProductionPerformanceNotFoundException extends AppException {

    public ProductionPerformanceNotFoundException(Long id) {
        super(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
    }

    public ProductionPerformanceNotFoundException(String message) {
        super(ProductionPerformanceErrorCode.PRODUCTION_PERFORMANCE_NOT_FOUND);
    }
}
