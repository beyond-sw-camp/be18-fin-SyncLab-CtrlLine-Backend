package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;

public interface JwtStoreService {
    void deleteRefreshToken(String email);
    void saveRefreshToken(String email, String refreshToken, long refreshTtl);
    boolean isBlacklisted(String email);
    CustomUserDetails getUserDetails(String email);

    void blacklistAccessToken(String jti, long ttl);
}
