package com.beyond.synclab.ctrlline.domain.production.client;

import org.springframework.http.HttpStatusCode;

public class MiloClientException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String responseBody;

    public MiloClientException(HttpStatusCode statusCode, String responseBody) {
        super("Milo client error, status=" + statusCode + ", responseBody=" + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
