package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderSummaryTelemetryPayload(
        String equipmentCode,
        BigDecimal producedQuantity,
        BigDecimal defectiveQuantity,
        String orderNo,
        String status,
        List<String> goodSerials,
        String goodSerialsGzip
) {
}
