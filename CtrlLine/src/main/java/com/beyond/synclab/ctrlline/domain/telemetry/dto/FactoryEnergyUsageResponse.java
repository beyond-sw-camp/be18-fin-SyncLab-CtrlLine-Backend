package com.beyond.synclab.ctrlline.domain.telemetry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FactoryEnergyUsageResponse {
    private final String factoryCode;
    private final BigDecimal powerConsumption;
    private final LocalDateTime recordedAt;
}
