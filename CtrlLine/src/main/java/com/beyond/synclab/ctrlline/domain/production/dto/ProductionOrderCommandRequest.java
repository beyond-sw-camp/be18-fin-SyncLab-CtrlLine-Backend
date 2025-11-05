package com.beyond.synclab.ctrlline.domain.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductionOrderCommandRequest(
        @NotBlank(message = "itemCode는 필수입니다.")
        String itemCode,

        @Positive(message = "qty는 0보다 커야 합니다.")
        int qty
) {
}
