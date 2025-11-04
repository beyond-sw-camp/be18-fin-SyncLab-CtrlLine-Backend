package com.beyond.synclab.ctrlline.security.handler;

import com.beyond.synclab.ctrlline.common.exception.ErrorResponse;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import com.beyond.synclab.ctrlline.security.exception.AuthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        log.debug(">>> EntryPoint ex= {}, msg = {}", ex.getClass(), ex.getMessage());

        AuthErrorCode errorCode = switch (ex) {
            case AuthException authEx -> authEx.getErrorCode();
            case org.springframework.security.authentication.BadCredentialsException badCredentialsException ->
                    AuthErrorCode.INVALID_ACCESS_TOKEN;
            case org.springframework.security.authentication.InsufficientAuthenticationException insufficientAuthenticationException ->
                    AuthErrorCode.UNAUTHORIZED;
            default -> AuthErrorCode.UNAUTHORIZED;
        };

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode);
        om.writeValue(response.getWriter(), body);
    }
}
