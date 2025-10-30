package com.beyond.synclab.ctrlline.security.jwt;

public interface JwtStoreService {
    void deleteRefreshToken(String email);
    void saveRefreshToken(String email, String refreshToken, long refreshTtl);
}
