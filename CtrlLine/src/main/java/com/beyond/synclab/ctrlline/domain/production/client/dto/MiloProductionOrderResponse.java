package com.beyond.synclab.ctrlline.domain.production.client.dto;

public record MiloProductionOrderResponse(
        String documentNo,
        String lineCode,
        String itemCode,
        int qty,
        String acceptedAt
) {
}
