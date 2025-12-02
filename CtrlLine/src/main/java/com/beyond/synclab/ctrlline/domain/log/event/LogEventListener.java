package com.beyond.synclab.ctrlline.domain.log.event;

import com.beyond.synclab.ctrlline.domain.log.dto.LogCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {
    private final AuditorAware<Long> auditorAware;
    private final LogService logService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLogEvent(LogEvent event) {
        log.debug("LogEventListener handleLogEvent: {}", event);
        Long userId = auditorAware.getCurrentAuditor().orElse(0L);
        LogCreateRequestDto logCreateRequestDto = LogCreateRequestDto.builder()
            .userId(userId)
            .actionType(event.actionType())
            .entityName(event.entityName())
            .entityId(event.entityId())
            .build();

        logService.createLog(logCreateRequestDto);
    }
}
