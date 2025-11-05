package com.beyond.synclab.ctrlline.domain.production.client.http;

import com.beyond.synclab.ctrlline.common.property.MiloClientProperties;
import com.beyond.synclab.ctrlline.domain.production.client.MiloClientException;
import com.beyond.synclab.ctrlline.domain.production.client.MiloProductionOrderClient;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderRequest;
import com.beyond.synclab.ctrlline.domain.production.client.dto.MiloProductionOrderResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class RestMiloProductionOrderClient implements MiloProductionOrderClient {

    private final RestClient restClient;

    public RestMiloProductionOrderClient(
            RestClient.Builder restClientBuilder,
            MiloClientProperties miloClientProperties
    ) {
        String baseUrl = Objects.requireNonNull(miloClientProperties.baseUrl(), "milo.client.base-url is required");
        String secretKey = Objects.requireNonNull(miloClientProperties.secretKey(), "milo.client.secret-key is required");

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();
    }

    @Override
    public MiloProductionOrderResponse dispatchOrder(String lineCode, MiloProductionOrderRequest request) {
        if (!StringUtils.hasText(lineCode)) {
            throw new IllegalArgumentException("lineCode must not be blank");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (!StringUtils.hasText(request.itemCode())) {
            throw new IllegalArgumentException("itemCode must not be blank");
        }
        if (request.qty() <= 0) {
            throw new IllegalArgumentException("qty must be greater than zero");
        }

        return restClient.post()
                .uri("/api/v1/orders/{lineCode}/cmd", lineCode)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    String body = StreamUtils.copyToString(resp.getBody(), StandardCharsets.UTF_8);
                    throw new MiloClientException(resp.getStatusCode(), body);
                })
                .body(MiloProductionOrderResponse.class);
    }
}
