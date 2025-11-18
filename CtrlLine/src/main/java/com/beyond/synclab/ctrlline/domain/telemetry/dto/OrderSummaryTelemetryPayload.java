package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record OrderSummaryTelemetryPayload(
        String equipmentCode,
        BigDecimal producedQuantity,
        BigDecimal defectiveQuantity
) {
}
