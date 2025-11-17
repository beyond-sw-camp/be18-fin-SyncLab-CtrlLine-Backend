package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
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
    public Defectives toEntity(Defectives.DefectivesBuilder builder, String documentNo) {
        return builder
                .defectiveCode(defectiveCode)
                .defectiveName(defectiveName)
                .defectiveQty(defectiveQuantity)
                .build();
    }
}
