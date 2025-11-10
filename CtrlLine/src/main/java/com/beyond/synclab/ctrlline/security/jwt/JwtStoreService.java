package com.beyond.synclab.ctrlline.security.jwt;

import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;

public interface JwtStoreService {

    void saveRefreshToken(String email, String refreshToken, long refreshTtl);
    String getRefreshToken(String username);
    void deleteRefreshToken(String email);
    void blacklistAccessToken(String jti, long ttl);

    boolean isBlacklisted(String email);
    CustomUserDetails getUserDetails(String email);

}
