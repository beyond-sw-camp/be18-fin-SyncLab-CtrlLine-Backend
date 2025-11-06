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
    private final String secretKey;

    public RestMiloProductionOrderClient(
            RestClient.Builder restClientBuilder,
            MiloClientProperties miloClientProperties
    ) {
        String baseUrl = Objects.requireNonNull(miloClientProperties.baseUrl(), "milo.client.base-url is required");
        this.secretKey = Objects.requireNonNull(miloClientProperties.secretKey(), "milo.client.secret-key is required");

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public MiloProductionOrderResponse dispatchOrder(String factoryCode, String lineCode, MiloProductionOrderRequest request) {
        if (!StringUtils.hasText(factoryCode)) {
            throw new IllegalArgumentException("factoryCode must not be blank");
        }
        if (!StringUtils.hasText(lineCode)) {
            throw new IllegalArgumentException("lineCode must not be blank");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (!StringUtils.hasText(request.action())) {
            throw new IllegalArgumentException("action must not be blank");
        }
        if (!StringUtils.hasText(request.orderNo())) {
            throw new IllegalArgumentException("orderNo must not be blank");
        }
        if (request.targetQty() <= 0) {
            throw new IllegalArgumentException("targetQty must be greater than zero");
        }
        if (!StringUtils.hasText(request.itemCode())) {
            throw new IllegalArgumentException("itemCode must not be blank");
        }

        String signaturePayload = String.join("|",
                factoryCode,
                lineCode,
                request.action(),
                request.orderNo(),
                String.valueOf(request.targetQty()),
                request.itemCode(),
                request.ppm() == null ? "" : String.valueOf(request.ppm())
        );

        String signature = createSignature(signaturePayload);

        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/orders/cmd")
                        .queryParam("factoryCode", factoryCode)
                        .queryParam("lineCode", lineCode)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + signature)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    String body = StreamUtils.copyToString(resp.getBody(), StandardCharsets.UTF_8);
                    throw new MiloClientException(resp.getStatusCode(), body);
                })
                .body(MiloProductionOrderResponse.class);
    }

    private String createSignature(String payload) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(secretKeyBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create HMAC signature", ex);
        }
    }

    private byte[] secretKeyBytes() {
        return secretKey.getBytes(StandardCharsets.UTF_8);
    }
}
