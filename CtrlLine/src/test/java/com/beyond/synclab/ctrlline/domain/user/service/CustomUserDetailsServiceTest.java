package com.beyond.synclab.ctrlline.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("UserName 으로 사용자 반환 성공")
    void loadUserByUsername_success() {
        // given
        String email = "test@test.com";
        Users users = Users.builder()
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(users));

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("UserName 으로 사용자 조회 실패")
    void loadUserByUsername_whenUserNotFoud_fail() {
        // given
        String email = "test@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("User가 Resigned 일때 실패")
    void loadUserByUsername_whenStatusResigned_fail() {
        // given
        String email = "test@test.com";
        Users users = Users.builder()
            .status(UserStatus.RESIGNED)
            .email(email)
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(users));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
            .isInstanceOf(UsernameNotFoundException.class);
    }

}