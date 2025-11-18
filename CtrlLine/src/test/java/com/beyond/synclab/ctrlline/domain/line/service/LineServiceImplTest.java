package com.beyond.synclab.ctrlline.domain.line.service;

import com.beyond.synclab.ctrlline.domain.line.dto.LineResponseDto;
import com.beyond.synclab.ctrlline.domain.line.dto.LineSearchCommand;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import config.QuerydslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({LineServiceImpl.class, QuerydslTestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class LineServiceImplTest {

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LineServiceImpl lineService;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        Users user1 = userRepository.save(
                Users.builder()
                     .empNo("2024001")
                     .name("홍길동")
                     .email("hong@test.com")
                     .phoneNumber("010-1111-2222")
                     .password("1234")
                     .hiredDate(LocalDate.now())
                     .department("IT")
                     .position(Users.UserPosition.MANAGER)
                     .role(Users.UserRole.USER)
                     .status(Users.UserStatus.ACTIVE)
                     .address("Seoul")
                     .build()
        );

        Users user2 = userRepository.save(
                Users.builder()
                     .empNo("2024002")
                     .name("김철수")
                     .email("kim@test.com")
                     .phoneNumber("010-3333-4444")
                     .password("1234")
                     .hiredDate(LocalDate.now())
                     .department("HR")
                     .position(Users.UserPosition.ASSISTANT)
                     .role(Users.UserRole.USER)
                     .status(Users.UserStatus.ACTIVE)
                     .address("Busan")
                     .build()
        );

        // 라인 생성
        lineRepository.saveAll(List.of(
                Lines.builder()
                     .lineCode("PL01")
                     .lineName("전지1라인")
                     .isActive(true)
                     .user(user1)
                     .userId(user1.getId())
                     .createdAt(LocalDateTime.now())
                     .updatedAt(LocalDateTime.now())
                     .build(),
                Lines.builder()
                     .lineCode("PL02")
                     .lineName("전지2라인")
                     .isActive(false)
                     .user(user2)
                     .userId(user2.getId())
                     .createdAt(LocalDateTime.now())
                     .updatedAt(LocalDateTime.now())
                     .build(),
                Lines.builder()
                     .lineCode("PL03")
                     .lineName("조립1라인")
                     .isActive(true)
                     .user(user1)
                     .userId(user1.getId())
                     .createdAt(LocalDateTime.now())
                     .updatedAt(LocalDateTime.now())
                     .build()
        ));
    }

    @Test
    @DisplayName("lineName 으로 검색시 특정 라인만 조회된다")
    void getLineList_success_searchBylineName() {
        LineSearchCommand cmd = new LineSearchCommand(null, "전지", null, null, null);

        Page<LineResponseDto> result = lineService.getLineList(cmd, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting("lineName")
                .containsExactlyInAnyOrder("전지1라인", "전지2라인");
    }

    @Test
    @DisplayName("userName으로 검색하면 해당 사용자의 라인만 조회된다")
    void getLineList_success_searchByUserName() {
        LineSearchCommand cmd = new LineSearchCommand(null, null, "홍길동", null, null);

        Page<LineResponseDto> result = lineService.getLineList(cmd, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("isActive=true 검색시 활성화 라인만")
    void getLineList_success_searchByIsActive() {
        LineSearchCommand cmd = new LineSearchCommand(null, null, null, null, true);

        Page<LineResponseDto> result = lineService.getLineList(cmd, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("복합 조건 검색 적용 확인")
    void getLineList_success_searchByMultipleConditions() {
        LineSearchCommand cmd = new LineSearchCommand(null, "라인", "홍길동", "IT", true);

        Page<LineResponseDto> result = lineService.getLineList(cmd, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }
}
