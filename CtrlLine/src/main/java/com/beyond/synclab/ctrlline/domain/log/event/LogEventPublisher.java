package com.beyond.synclab.ctrlline.domain.log.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publish(LogEvent logEvent) {
        log.debug("LogEventPublished publish : {}", logEvent);
        publisher.publishEvent(logEvent);
    }
}