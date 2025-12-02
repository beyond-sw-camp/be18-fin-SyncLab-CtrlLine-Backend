package com.beyond.synclab.ctrlline.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateMeRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.SearchUserParameterDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("유저서비스 테스트")
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

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

        SearchUserParameterDto command = SearchUserParameterDto.builder()
            .userDepartment("testDepartment")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPosition(UserPosition.ASSISTANT)
            .userPhoneNumber("010-1234-1234")
            .userEmail("test@test.com")
            .hiredDate(LocalDate.now())
            .terminationDate(LocalDate.now())
            .userEmpNo("209901999")
            .userName("홍길동")
            .build();

        Pageable pageable = PageRequest.of(0, 10,  Sort.by("empNo").descending());

        Page<Users> mockResponse = new PageImpl<>(List.of(users), pageable, 1);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        Page<UserListResponseDto> response = userService.getUserList(command, pageable);

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
        SearchUserParameterDto command = SearchUserParameterDto.builder()
            .userDepartment("wrongDepartment")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPosition(UserPosition.ASSISTANT)
            .userPhoneNumber("010-1234-1234")
            .userEmail("test@test.com")
            .hiredDate(LocalDate.now())
            .terminationDate(LocalDate.now())
            .userEmpNo("209901999")
            .userName("홍길동")
            .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("empNo").descending());
        Page<Users> emptyResponse = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(emptyResponse);

        // when
        Page<UserListResponseDto> response = userService.getUserList(command, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("유저 목록 조회 - 사번 내림차순 정렬 확인")
    void getUserList_SortedByEmpNoDesc() {
        // given
        SearchUserParameterDto command = SearchUserParameterDto.builder().build();

        Users user1 = createTestUser(1L, "209912999", "A팀", UserStatus.ACTIVE);
        Users user2 = createTestUser(2L, "209912998", "A팀", UserStatus.ACTIVE);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("empNo").descending());
        Page<Users> mockResponse = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        // when
        Page<UserListResponseDto> response = userService.getUserList(command, pageable);

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

        SearchUserParameterDto command = SearchUserParameterDto.builder()
            .userDepartment("A부서")
            .userEmail("match@test.com")
            .hiredDate(LocalDate.now())
            .terminationDate(LocalDate.now())
            .build();

        when(userRepository.findAll(ArgumentMatchers.<Specification<Users>>any(), eq(pageable)))
            .thenReturn(mockResponse);

        // when
        Page<UserListResponseDto> response = userService.getUserList(command, pageable);

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

        UserResponseDto responseDto = userService.getUserById(userId, user);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getEmpNo()).isEqualTo("202510001");
        assertThat(responseDto.getUserDepartment()).isEqualTo("영업1팀");
    }

    @Test
    @DisplayName("유저 수정 성공")
    void patchUser_success() {
        //given
        Long userId = 1L;
        Users user = createTestUser(userId, "209912001", "testDepartment", UserStatus.ACTIVE);

        UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
            .name("홍길동")
            .email("hong1234@test.com")
            .phoneNumber("010-1234-1234")
            .department("updateDepartment")
            .position(UserPosition.ASSISTANT)
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .address("한화로123")
            .terminationDate(LocalDate.now())
            .password("1234")
            .passwordConfirm("1234")
            .extension("01123")
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(userUpdateRequestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userUpdateRequestDto.getPassword())).thenReturn("encoded1234");

        // when
        UserResponseDto responseDto = userService.updateUserById(userUpdateRequestDto, userId);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUserEmail()).isEqualTo("hong1234@test.com");
        assertThat(responseDto.getEmpNo()).isEqualTo("209912001");
        assertThat(responseDto.getUserDepartment()).isEqualTo("updateDepartment");

        verify(userRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("유저 수정 실패 - 없는 유저 조회")
    void patchUser_notFound_fail() {
        //given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserById(mock(UserUpdateRequestDto.class), userId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공")
    void updateUser_success() {
        // given
        UserUpdateMeRequestDto userUpdateMeRequestDto = UserUpdateMeRequestDto.builder()
                .userAddress("서울")
                .userPassword("1234")
                .userPasswordConfirm("1234")
                .userPhoneNumber("010-1234-1234")
                .userEmail("test@test.com")
                .userName("홍길동")
                .build();

        Users users = Users.builder()
                .id(1L)
                .email("old@old.com")
                .password("oldPassword")
                .phoneNumber("010-0000-0000")
                .address("예전 거주지")
                .name("홍올드")
                .build();
        when(passwordEncoder.encode("1234")).thenReturn("newPassword");

        // when
        UserResponseDto responseDto = userService.updateMyInfo(userUpdateMeRequestDto, users);


        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUserPhoneNumber()).isEqualTo("010-1234-1234");
        assertThat(responseDto.getUserEmail()).isEqualTo("test@test.com");
        assertThat(responseDto.getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("유저 개인 정보 수정 실패 - 비밀번호 불일치")
    void updateUser_passwordMismatch_fail() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userPassword("1234")
            .userPasswordConfirm("5678")
            .build();

        Users user = Users.builder().id(1L).password("old").build();

        // when & then
        assertThatThrownBy(() -> userService.updateMyInfo(dto, user))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(UserErrorCode.PASSWORD_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공 - 비밀번호가 null인 경우")
    void updateUser_noPasswordUpdate_success() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userAddress("부산")
            .userEmail("new@test.com")
            .build();

        Users user = Users.builder()
            .id(1L)
            .email("old@test.com")
            .address("서울")
            .password("oldPassword")
            .build();

        // when
        UserResponseDto result = userService.updateMyInfo(dto, user);

        // then
        assertThat(result.getUserEmail()).isEqualTo("new@test.com");
        assertThat(user.getPassword()).isEqualTo("oldPassword"); // 기존 유지
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공 - 비밀번호 공백 입력 시 비번 변경 제외")
    void updateUser_blankPassword_success() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userPassword("")
            .userPasswordConfirm("")
            .userName("새이름")
            .build();

        Users user = Users.builder()
            .id(1L)
            .name("기존이름")
            .password("oldPassword")
            .build();

        // when
        UserResponseDto result = userService.updateMyInfo(dto, user);

        // then
        assertThat(result.getUserName()).isEqualTo("새이름");
        assertThat(user.getPassword()).isEqualTo("oldPassword"); // 변경 X
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공 - 비밀번호 없음, confirm만 들어온 경우")
    void updateUser_onlyConfirmProvided_success() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userPassword(null)
            .userPasswordConfirm("1234")
            .userPhoneNumber("010-2222-3333")
            .build();

        Users user = Users.builder()
            .id(1L)
            .password("oldPassword")
            .phoneNumber("010-0000-0000")
            .build();

        // when
        UserResponseDto result = userService.updateMyInfo(dto, user);

        // then
        assertThat(result.getUserPhoneNumber()).isEqualTo("010-2222-3333");
        assertThat(user.getPassword()).isEqualTo("oldPassword"); // 변경되지 않아야 함
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공 - 모든 필드가 null 또는 blank인 경우 변경 없음")
    void updateUser_noFieldsToUpdate_success() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userAddress(null)
            .userEmail("")
            .userName(null)
            .userPhoneNumber("   ")
            .build();

        Users user = Users.builder()
            .id(1L)
            .email("old@test.com")
            .name("홍길동")
            .address("서울")
            .phoneNumber("010-0000-0000")
            .password("oldPassword")
            .build();

        // when
        UserResponseDto result = userService.updateMyInfo(dto, user);

        // then
        assertThat(result.getUserEmail()).isEqualTo("old@test.com");
        assertThat(result.getUserName()).isEqualTo("홍길동");
        assertThat(user.getPassword()).isEqualTo("oldPassword");
    }

    @Test
    @DisplayName("유저 개인 정보 수정 성공 - 비밀번호 정상 변경")
    void updateUser_passwordUpdate_success() {
        // given
        UserUpdateMeRequestDto dto = UserUpdateMeRequestDto.builder()
            .userPassword("newPassword")
            .userPasswordConfirm("newPassword")
            .build();

        Users user = Users.builder()
            .id(1L)
            .password("oldPassword")
            .build();

        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPw");

        // when
        UserResponseDto result = userService.updateMyInfo(dto, user);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(user.getPassword()).isEqualTo("encodedPw");
        verify(passwordEncoder, times(1)).encode("newPassword");
    }

    @Test
    @DisplayName("유저 수정 실패 - 비밀번호 불일치로 예외 발생")
    void updateUserById_passwordMismatch_fail() {
        // given
        Long userId = 1L;
        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
            .password("1234")
            .passwordConfirm("5678")
            .build();

        // when & then
        assertThatThrownBy(() -> userService.updateUserById(dto, userId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(UserErrorCode.PASSWORD_MISMATCH.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저 수정 실패 -이메일 중복으로 예외 발생")
    void updateUserById_duplicateEmail_fail() {
        // given
        Long userId = 1L;
        Users user = createTestUser(userId, "209912001", "testDept", UserStatus.ACTIVE);
        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
            .email("dup@test.com")
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateUserById(dto, userId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(AuthErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("내정보 조회 - 성공")
    void getMyInfo_success() {
        // given
        Long userId = 1L;
        Users user = createTestUser(userId, "202510001", "영업1팀", UserStatus.ACTIVE);

        // repository가 반환할 객체 준비
        Users foundedUser = createTestUser(userId, "202510001", "영업1팀", UserStatus.ACTIVE);

        // when
        when(userRepository.getReferenceById(userId)).thenReturn(foundedUser);

        UserResponseDto responseDto = userService.getMyInfo(user);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getEmpNo()).isEqualTo("202510001");
        assertThat(responseDto.getUserDepartment()).isEqualTo("영업1팀");
        assertThat(responseDto.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }


}