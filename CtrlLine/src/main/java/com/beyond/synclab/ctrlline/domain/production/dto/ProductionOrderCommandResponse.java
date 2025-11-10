package com.beyond.synclab.ctrlline.domain.production.dto;

import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;

public record ProductionOrderCommandResponse(
        String documentNo,
        String lineCode,
        String itemCode,
        int qty,
        String acceptedAt
) {

    public static ProductionOrderCommandResponse from(MiloProductionOrderResponse response) {
        return new ProductionOrderCommandResponse(
                response.documentNo(),
                response.lineCode(),
                response.itemCode(),
                response.qty(),
                response.acceptedAt()
        );
    }
}
