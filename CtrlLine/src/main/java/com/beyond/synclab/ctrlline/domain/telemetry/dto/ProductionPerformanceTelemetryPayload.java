package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ProductionPerformanceTelemetryPayload(
        String orderNo,
        BigDecimal orderProducedQty,
        BigDecimal ngCount,
        LocalDateTime executeAt,
        LocalDateTime waitingAckAt
) {
}
