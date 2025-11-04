package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthService 단위 테스트")
class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAuthService userAuthService;

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 시 AppException 발생")
    void signup_fail_duplicateEmail() {
        // given
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("test@test.com")
                .password("1234")
                .name("홍길동")
                .hiredDate(LocalDate.of(2025, 10, 1))
                .build();

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userAuthService.signup(request))
                .isInstanceOf(AppException.class)
                .hasMessage(AuthErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 성공 - empNo 자동생성 및 암호화 저장")
    void signup_success() {
        // given
        UserSignupRequestDto request = UserSignupRequestDto.builder()
                .email("hong@test.com")
                .password("1234")
                .name("홍길동")
                .hiredDate(LocalDate.of(2025, 10, 20))
                .build();

        when(userRepository.existsByEmail("hong@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("1234"))
                .thenReturn("ENCODED");
        when(userRepository.findEmpNosByPrefix("202510"))
                .thenReturn(Collections.singletonList("202510003"));

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);

        // when
        userAuthService.signup(request);

        // then
        verify(userRepository).save(captor.capture());
        Users saved = captor.getValue();

        assertThat(saved.getEmpNo()).isEqualTo("202510004");
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
    }

    @Test
    @DisplayName("empNo 생성 로직 - 동일 월 내 순번 증가 확인")
    void generateEmpNo_incrementsCorrectly() {
        // given
        LocalDate hiredDate = LocalDate.of(2025, 10, 15);
        when(userRepository.findEmpNosByPrefix("202510"))
                .thenReturn(Arrays.asList("202510003", "202510002", "202510001"));

        // when
        String empNo = ReflectionTestUtils.invokeMethod(userAuthService, "generateEmpNo", hiredDate);

        // then
        assertThat(empNo).isEqualTo("202510004");
    }
}