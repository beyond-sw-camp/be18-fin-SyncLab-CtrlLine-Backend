package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.common.property.AppProperties;
import com.beyond.synclab.ctrlline.common.property.JwtProperties;
import com.beyond.synclab.ctrlline.common.property.MesKafkaProperties;
import com.beyond.synclab.ctrlline.common.property.MiloClientProperties;
import com.beyond.synclab.ctrlline.common.property.SendGridProperties;
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
@EnableConfigurationProperties({
        JwtProperties.class,
        AppProperties.class,
        MiloClientProperties.class,
        MesKafkaProperties.class,
        SerialStorageProperties.class,
        SendGridProperties.class
})
public class AppConfig {

    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;
    private final MiloClientProperties miloClientProperties;
    private final MesKafkaProperties mesKafkaProperties;
    private final SerialStorageProperties serialStorageProperties;
    private final SendGridProperties sendGridProperties;

    @PostConstruct
    void init() {
        log.debug("jwtProperties = {}", jwtProperties);
        log.debug("appProperties = {}", appProperties);
        log.debug("miloClientProperties = {}", miloClientProperties);
        log.debug("mesKafkaProperties = {}", mesKafkaProperties);
        log.debug("serialStorageProperties = {}", serialStorageProperties);
        log.debug("sendGridProperties = {}", sendGridProperties);
    }
}
