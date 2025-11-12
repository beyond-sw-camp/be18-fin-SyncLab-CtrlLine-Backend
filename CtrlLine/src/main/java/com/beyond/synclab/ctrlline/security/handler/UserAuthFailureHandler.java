package com.beyond.synclab.ctrlline.security.handler;

import com.beyond.synclab.ctrlline.common.exception.ErrorResponse;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserAuthFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception
    ) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.INVALID_LOGIN;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode);
        om.writeValue(response.getWriter(), body);

        log.debug(">>> 로그인 실패: {}", errorCode.getMessage());
    }
}
