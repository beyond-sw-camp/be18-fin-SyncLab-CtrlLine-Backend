package com.beyond.synclab.ctrlline.common.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ToString
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "sendgrid")
public class SendGridProperties {

    private final String apiKey;
    private final Sender sender;

    public record Sender(String email, String name) {
    }
}
