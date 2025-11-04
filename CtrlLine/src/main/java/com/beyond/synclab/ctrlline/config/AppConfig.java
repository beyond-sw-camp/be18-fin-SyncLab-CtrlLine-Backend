package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.common.property.AppProperties;
import com.beyond.synclab.ctrlline.common.property.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class, AppProperties.class})
public class AppConfig {

    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;

    @PostConstruct
    void init() {
        log.debug("jwtProperties = {}", jwtProperties);
        log.debug("appProperties = {}", appProperties);
    }
}
