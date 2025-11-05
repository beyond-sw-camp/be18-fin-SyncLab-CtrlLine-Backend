package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return Optional.of(0L);
            }

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return Optional.ofNullable(userDetails.getId());
        } catch (Exception e) {
            return Optional.of(0L);
        }
    }
}
