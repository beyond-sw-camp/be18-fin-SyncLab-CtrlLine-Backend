package com.beyond.synclab.ctrlline.domain.production.controller;

import com.beyond.synclab.ctrlline.domain.production.client.MiloClientException;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandRequest;
import com.beyond.synclab.ctrlline.domain.production.dto.ProductionOrderCommandResponse;
import com.beyond.synclab.ctrlline.domain.production.service.ProductionOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    @PostMapping("/cmd")
    public ResponseEntity<ProductionOrderCommandResponse> dispatchOrder(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam String factoryCode,
            @RequestParam String lineCode,
            @Valid @RequestBody ProductionOrderCommandRequest request
    ) {
        log.debug("dispatchOrder called authorization={}, factoryCode={}, lineCode={}", authorization, factoryCode, lineCode);
        log.debug(request.toString());
        ProductionOrderCommandResponse response = productionOrderService.dispatchOrder(factoryCode, lineCode, request);
        log.debug(response.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(MiloClientException.class)
    public ResponseEntity<String> handleMiloClientException(MiloClientException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status).body(ex.getResponseBody());
    }
}
