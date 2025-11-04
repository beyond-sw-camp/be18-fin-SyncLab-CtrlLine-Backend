package com.beyond.synclab.ctrlline.common.property;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
@ToString
@RequiredArgsConstructor
public class JwtProperties {
    private final AccessToken accessToken;
    private final RefreshToken refreshToken;

    public record AccessToken(long expired, String secretKey) {
    }

    public record RefreshToken(long expired, String secretKey) {
    }
}
