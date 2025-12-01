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
    private final Frontend frontend;

    public record Redis(AppProperties.Redis.Prefix prefix) {
        public record Prefix(String refresh,
                             String blacklist,
                             String planDefectiveLastReported,
                             String environmentLatest) {
        }
    }

    public record Frontend(String baseUrl) {
        public String productionPlanDetail(Long productionPlanId) {
            if (productionPlanId == null) {
                return null;
            }
            return String.format("%s/production-management/production-plans/%d", trimTrailingSlash(baseUrl), productionPlanId);
        }

        private String trimTrailingSlash(String value) {
            if (value == null) {
                return "";
            }
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }
    }
}
