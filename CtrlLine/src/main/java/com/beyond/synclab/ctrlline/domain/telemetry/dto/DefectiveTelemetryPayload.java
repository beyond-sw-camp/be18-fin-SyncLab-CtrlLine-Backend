package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record DefectiveTelemetryPayload(
        Long equipmentId,
        String equipmentCode,
        String defectiveCode,
        String defectiveName,
        BigDecimal defectiveQuantity,
        String status
) {
}
