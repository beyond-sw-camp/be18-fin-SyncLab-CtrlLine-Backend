package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;

public interface JwtValidator {
    boolean isBlacklisted(String email);
    CustomUserDetails getUserDetails(String email);
}
