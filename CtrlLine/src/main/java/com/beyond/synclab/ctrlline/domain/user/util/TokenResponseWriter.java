package com.beyond.synclab.ctrlline.domain.user.util;

import com.beyond.synclab.ctrlline.common.util.CookieUtil;
import com.beyond.synclab.ctrlline.domain.user.dto.ReissueResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenResponseWriter {
    public void writeTokens(HttpServletResponse response, ReissueResponseDto dto){
        response.setHeader("Authorization", "Bearer " + dto.getAccessToken());

        Cookie cookie = CookieUtil.createHttpOnlyCookie(
            "refresh_token",
            dto.getRefreshToken(),
            dto.getMaxAge()
        );

        response.addCookie(cookie);
    }
}
