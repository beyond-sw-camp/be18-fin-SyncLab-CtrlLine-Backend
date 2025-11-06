package com.beyond.synclab.ctrlline.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private Users createTestUser(Long id, String empNo, String department, UserStatus status) {
        LocalDate nowDate = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();

        return Users.builder()
            .id(id)
            .empNo(empNo)
            .name("홍길동")
            .email("test@test.com")
            .password("testPassword")
            .phoneNumber("010-1234-1234")
            .hiredDate(nowDate)
            .terminationDate(nowDate)
            .extension("01123")
            .role(UserRole.USER)
            .status(status)
            .department(department)
            .position(UserPosition.ASSISTANT)
            .address("testAddress")
            .createdAt(nowDateTime)
            .updatedAt(nowDateTime)
            .build();
    }

    @Test
    @DisplayName("유저 목록 조회에 성공합니다.")
    void getUserList_Success() {
        // given

        Users users = createTestUser(1L, "209912999", "testDepartment", UserStatus.ACTIVE);

        UserSearchCommand command = new UserSearchCommand(
            "testDepartment",
            UserStatus.ACTIVE,
            UserRole.USER,
            UserPosition.ASSISTANT,
            "010-1234-1234",
            "test@test.com",
            LocalDate.now(),
            LocalDate.now()
        );

        Pageable pageable = PageRequest.of(0, 10,  Sort.by("empNo").descending());

        Page<Users> mockResponse = new PageImpl<>(List.of(users), pageable, 1);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        Page<UserResponseDto> response = userService.getUserList(command, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(response.getContent().getFirst().getUserEmail()).isEqualTo("test@test.com");
        assertThat(response.getContent().getFirst().getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("유저 목록 조회 - 필터 조건과 일치하지 않으면 빈 페이지 반환")
    void getUserList_EmptyResult() {
        // given
        UserSearchCommand command = new UserSearchCommand(
            "wrongDepartment",
            UserStatus.RESIGNED,
            UserRole.ADMIN,
            UserPosition.MANAGER,
            "010-9999-9999",
            "no@test.com",
            LocalDate.now(),
            LocalDate.now()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("empNo").descending());
        Page<Users> emptyResponse = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(emptyResponse);

        // when
        Page<UserResponseDto> response = userService.getUserList(command, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("유저 목록 조회 - 사번 내림차순 정렬 확인")
    void getUserList_SortedByEmpNoDesc() {
        // given
        UserSearchCommand command = new UserSearchCommand(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        Users user1 = createTestUser(1L, "209912999", "A팀", UserStatus.ACTIVE);
        Users user2 = createTestUser(2L, "209912998", "A팀", UserStatus.ACTIVE);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("empNo").descending());
        Page<Users> mockResponse = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        // when
        Page<UserResponseDto> response = userService.getUserList(command, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getEmpNo())
            .isGreaterThan(response.getContent().get(1).getEmpNo());
    }

    @Test
    @DisplayName("유저 목록 조회 - 유저 부서 조건으로 필터링되는지 검증")
    void getUserList_FilterByStatus() {
        // given
        Users user1 = createTestUser(1L, "209912999", "A팀", UserStatus.ACTIVE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Users> mockResponse = new PageImpl<>(List.of(user1), pageable, 1);

        UserSearchCommand command = new UserSearchCommand(
            "A팀", null, null, null, null, "match@test.com", null, null
        );

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        // when
        Page<UserResponseDto> response = userService.getUserList(command, pageable);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getUserDepartment()).isEqualTo("A팀");
    }

    @Test
    @DisplayName("유저 상세 조회 - 유저 상세 조회 성공")
    void getUser_success() {
        // given
        Long userId = 1L;
        Users user = createTestUser(userId, "202510001", "영업1팀",  UserStatus.ACTIVE);

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDto responseDto = userService.getUserById(userId);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getEmpNo()).isEqualTo("202510001");
        assertThat(responseDto.getUserDepartment()).isEqualTo("영업1팀");
    }
}