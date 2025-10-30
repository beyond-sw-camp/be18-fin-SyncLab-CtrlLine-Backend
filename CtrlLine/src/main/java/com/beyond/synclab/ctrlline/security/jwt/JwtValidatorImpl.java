package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtValidatorImpl implements JwtValidator {
    @Override
    public boolean isBlacklisted(String email) {
        return false;
    }

    @Override
    public CustomUserDetails getUserDetails(String email) {
        return null;
    }
}
