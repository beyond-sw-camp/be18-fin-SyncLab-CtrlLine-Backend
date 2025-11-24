package com.beyond.synclab.ctrlline.common.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ToString
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Redis redis;

    public record Redis(AppProperties.Redis.Prefix prefix) {
        public record Prefix(String refresh, String blacklist, String planDefectiveLastReported) {
        }
    }
}
