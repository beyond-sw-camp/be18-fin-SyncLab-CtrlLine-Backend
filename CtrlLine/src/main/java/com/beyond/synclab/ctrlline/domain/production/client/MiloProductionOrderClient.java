package com.beyond.synclab.ctrlline.domain.production.client;

import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;

public interface MiloProductionOrderClient {

    MiloProductionOrderResponse dispatchOrder(String lineCode, MiloProductionOrderRequest request);
}
