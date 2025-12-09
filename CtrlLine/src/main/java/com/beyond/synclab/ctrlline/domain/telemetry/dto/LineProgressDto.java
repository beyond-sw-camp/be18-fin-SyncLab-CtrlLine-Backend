package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;

public record LineProgressDto(
        String factoryCode,
        String lineCode,
        String orderNo,
        BigDecimal producedQty,
        BigDecimal targetQty,
        long updatedAt
) {}
