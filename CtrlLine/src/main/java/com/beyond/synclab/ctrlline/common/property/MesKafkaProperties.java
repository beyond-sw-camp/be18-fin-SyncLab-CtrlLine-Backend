package com.beyond.synclab.ctrlline.common.property;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ToString
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "mes.kafka")
public class MesKafkaProperties {
    private final List<String> bootstrapServers;
    private final String topic;
    private final String clientId;
    private final String groupId;
    private final String autoOffsetReset;
    private final Duration pollInterval;
}
