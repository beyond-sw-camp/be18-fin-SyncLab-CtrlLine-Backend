package com.beyond.synclab.ctrlline.domain.log;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import com.beyond.synclab.ctrlline.domain.log.event.LogEvent;
import com.beyond.synclab.ctrlline.domain.log.event.LogEventListener;
import com.beyond.synclab.ctrlline.domain.log.repository.LogRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

@ExtendWith(MockitoExtension.class)
@DisplayName("Log 단위 테스트")
class LogTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private AuditorAware<Long> auditorAware;

    @InjectMocks
    private LogEventListener logEventListener;

    @Test
    @DisplayName("LogEvent 발생 시 로그 저장 동작 검증")
    void handleLogEvent_shouldSaveLogEntity() {
        // given
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(1L));
        LogEvent event = new LogEvent("CREATE", 1L, ActionType.CREATE);

        // when
        logEventListener.handleLogEvent(event);

        // then
        verify(logRepository, times(1)).save(any(Logs.class));
    }
}
