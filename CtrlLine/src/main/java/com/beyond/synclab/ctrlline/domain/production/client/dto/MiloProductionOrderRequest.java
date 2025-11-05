package com.beyond.synclab.ctrlline.domain.production.client.dto;

public record MiloProductionOrderRequest(
        String itemCode,
        int qty
) {
}
