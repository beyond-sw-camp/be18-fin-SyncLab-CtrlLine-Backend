package com.beyond.synclab.ctrlline.domain.log.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.log.dto.LogCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogListResponseDto;
import com.beyond.synclab.ctrlline.domain.log.dto.LogSearchDto;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import com.beyond.synclab.ctrlline.domain.log.entity.Logs.ActionType;
import com.beyond.synclab.ctrlline.domain.log.repository.LogRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {
    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogServiceImpl logService;

    private LocalDate baseDate;

    private Users users;

    @BeforeEach
    void setup() {
        users = Users.builder()
            .id(1L)
            .name("홍길동")
            .empNo("209901001")
            .build();
        baseDate = LocalDate.ofInstant(Instant.parse("2099-01-01T00:00:00Z"), ZoneId.systemDefault());
    }

    @Test
    @DisplayName("정상 케이스 - 모든 조건 일치 시 로그 목록 조회 성공")
    void getLogsList_success() {
        // given
        LogSearchDto dto = LogSearchDto.builder()
            .entityName("user")
            .userId(1L)
            .fromDate(baseDate)
            .toDate(baseDate.plusDays(2))
            .actionType(ActionType.CREATE)
            .build();

        Logs logs = Logs.builder()
            .entityName("user")
            .user(users)
            .userId(1L)
            .createdAt(baseDate.plusDays(1).atStartOfDay())
            .actionType(ActionType.CREATE)
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(logs));

        // when
        List<LogListResponseDto> result = logService.getLogsList(dto);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getEntityName()).isEqualTo("user");
        assertThat(result.getFirst().getActionType()).isEqualTo(ActionType.CREATE);
    }

    @Test
    @DisplayName("모든 필드 null → 전체 로그 조회")
    void getLogsList_allNull() {
        LogSearchDto dto = LogSearchDto.builder().build();

        Logs log = Logs.builder()
            .entityName("item")
            .userId(1L)
            .user(users)
            .createdAt(baseDate.atStartOfDay())
            .actionType(ActionType.UPDATE)
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(log));

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEntityName()).isEqualTo("item");
    }


    @Test
    @DisplayName("날짜만 지정 → 기간 내 로그 필터링")
    void getLogsList_onlyDate() {
        LogSearchDto dto = LogSearchDto.builder()
            .fromDate(baseDate)
            .toDate(baseDate.plusDays(1))
            .build();

        Logs log = Logs.builder()
            .entityName("item")
            .userId(1L)
            .user(users)
            .createdAt(baseDate.plusDays(1).atStartOfDay())
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(log));

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getCreatedAt()).isBetween(
            baseDate.atStartOfDay(), baseDate.plusDays(1).plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("userId만 지정 → 해당 사용자 로그만 반환")
    void getLogsList_userOnly() {
        LogSearchDto dto = LogSearchDto.builder()
            .userId(99L)
            .build();

        Logs log = Logs.builder()
            .entityName("user")
            .userId(99L)
            .user(users)
            .actionType(ActionType.DELETE)
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(log));

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("fromDate == toDate → 하루치 로그만 조회")
    void getLogsList_sameDateRange() {
        LogSearchDto dto = LogSearchDto.builder()
            .fromDate(baseDate)
            .toDate(baseDate)
            .build();

        Logs log = Logs.builder()
            .userId(users.getId())
            .user(users)
            .createdAt(baseDate.atTime(12, 0))
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(log));

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCreatedAt().toLocalDate()).isEqualTo(baseDate);
    }

    @Test
    @DisplayName("조건 불일치 → 빈 리스트 반환")
    void getLogsList_noMatch() {
        LogSearchDto dto = LogSearchDto.builder()
            .entityName("inventory")
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of());

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("조회 조건 결과 없음 -> 빈 리스트 반환")
    void getLogsList_nullSafe() {
        LogSearchDto dto = LogSearchDto.builder().build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(Collections.emptyList());

        List<LogListResponseDto> result = logService.getLogsList(dto);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createdAt 오름차순 정렬 확인")
    void getLogsList_sortedByCreatedAtAsc() {
        Logs log1 = Logs.builder()
            .userId(users.getId())
            .user(users)
            .createdAt(baseDate.atTime(9, 0))
            .build();
        Logs log2 = Logs.builder()
            .userId(users.getId())
            .user(users)
            .createdAt(baseDate.atTime(12, 0))
            .build();

        when(logRepository.findAll(ArgumentMatchers.<Specification<Logs>>any(), any(Sort.class)))
            .thenReturn(List.of(log1, log2));

        List<LogListResponseDto> result = logService.getLogsList(LogSearchDto.builder().build());

        assertThat(result.get(0).getCreatedAt()).isBefore(result.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("로그 생성 성공")
    void createLog_success() {
        // given
        LogCreateRequestDto requestDto = LogCreateRequestDto.builder()
            .entityName("user")
            .userId(1L)
            .actionType(ActionType.CREATE)
            .build();

        Logs savedLogs = Logs.builder()
            .entityName("user")
            .userId(1L)
            .actionType(ActionType.CREATE)
            .createdAt(baseDate.atTime(9, 0))
            .build();

        // save 호출 시 동일한 객체 반환하도록 mocking
        when(logRepository.save(any(Logs.class))).thenReturn(savedLogs);

        // when
        logService.createLog(requestDto);

        // then
        ArgumentCaptor<Logs> captor = ArgumentCaptor.forClass(Logs.class);
        verify(logRepository, times(1)).save(captor.capture());

        Logs captured = captor.getValue();
        assertThat(captured.getEntityName()).isEqualTo("user");
        assertThat(captured.getUserId()).isEqualTo(1L);
        assertThat(captured.getActionType()).isEqualTo(ActionType.CREATE);
    }
}