package com.beyond.synclab.ctrlline.common.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ToString
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private final List<String> allowedOrigins;
    private final List<String> allowedOriginPatterns;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
}
