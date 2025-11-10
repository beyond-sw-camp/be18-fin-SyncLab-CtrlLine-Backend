package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.common.property.MesKafkaProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final MesKafkaProperties mesKafkaProperties;

    @Bean
    public Map<String, Object> telemetryConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", mesKafkaProperties.getBootstrapServers()));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, mesKafkaProperties.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, mesKafkaProperties.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, mesKafkaProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> telemetryConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(telemetryConsumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> telemetryKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(telemetryConsumerFactory());
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckOnError(false);
        return factory;
    }
}
