package com.beyond.synclab.ctrlline.common.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "milo.client")
public record MiloClientProperties(
        String baseUrl,
        String secretKey
) {
}
