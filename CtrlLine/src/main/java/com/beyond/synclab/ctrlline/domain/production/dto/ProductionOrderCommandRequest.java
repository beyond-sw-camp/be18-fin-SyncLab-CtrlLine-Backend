package com.beyond.synclab.ctrlline.domain.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductionOrderCommandRequest(
        @NotBlank(message = "action은 필수입니다.")
        String action,

        @NotBlank(message = "orderNo는 필수입니다.")
        String orderNo,

        @Positive(message = "targetQty는 0보다 커야 합니다.")
        int targetQty,

        @NotBlank(message = "itemCode는 필수입니다.")
        String itemCode,

        Integer ppm
) {
}
