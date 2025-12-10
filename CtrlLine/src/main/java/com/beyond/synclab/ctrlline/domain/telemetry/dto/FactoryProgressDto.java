package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import java.util.List;

public record FactoryProgressDto(
        String factoryCode,
        BigDecimal totalProducedQty,
        BigDecimal totalTargetQty,
        BigDecimal progressRate,
        long updatedAt,
        List<LineProgressDto> lines
) {
    public static FactoryProgressDto of(
            String factoryCode,
            BigDecimal totalProducedQty,
            BigDecimal totalTargetQty,
            BigDecimal progressRate,
            long updatedAt,
            List<LineProgressDto> lines
    ) {
        return new FactoryProgressDto(
                factoryCode,
                totalProducedQty,
                totalTargetQty,
                progressRate,
                updatedAt,
                List.copyOf(lines)
        );
    }
}
