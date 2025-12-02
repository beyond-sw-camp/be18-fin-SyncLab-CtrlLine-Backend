package com.beyond.synclab.ctrlline.domain.log.service;

import com.beyond.synclab.ctrlline.domain.log.dto.LogCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogListResponseDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogSearchDto;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.repository.LogRepository;
import com.beyond.synclab.ctrlline.domain.log.spec.LogSpecification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogRepository logRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LogListResponseDto> getLogsList(LogSearchDto logSearchDto) {
        Specification<Logs> spec = Specification.allOf(
            LogSpecification.logsBetween(logSearchDto.fromDate(), logSearchDto.toDate()),
            LogSpecification.logsEntityNameContains(logSearchDto.entityName()),
            LogSpecification.logsActionTypeEquals(logSearchDto.actionType()),
            LogSpecification.logsUserIdEquals(logSearchDto.userId())
        );

        List<Logs> logs = logRepository.findAll(spec, Sort.by(Direction.DESC, "createdAt"));

        return logs.stream().map(LogListResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional
    public void createLog(LogCreateRequestDto logCreateRequestDto) {
        Logs logs = logCreateRequestDto.toEntity();

        logRepository.save(logs);
    }
}
