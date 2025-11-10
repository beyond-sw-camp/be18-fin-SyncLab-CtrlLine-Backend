package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug(">> loadUserByUsername : email = {}", email);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getStatus() == UserStatus.RESIGNED) {
            log.debug(">> loadUserByUsername : 퇴사한 사용자는 로그인할 수 없습니다.");
            throw new DisabledException("퇴사한 사용자는 로그인할 수 없습니다.");
        }

        return new CustomUserDetails(user);
    }
}