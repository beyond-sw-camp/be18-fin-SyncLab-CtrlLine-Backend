package com.beyond.synclab.ctrlline.domain.production.client.dto;

public record MiloProductionOrderRequest(
        String action,
        String orderNo,
        int targetQty,
        String itemCode,
        Integer ppm
) {
}
