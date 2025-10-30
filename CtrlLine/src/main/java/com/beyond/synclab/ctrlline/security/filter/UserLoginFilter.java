package com.beyond.synclab.ctrlline.security.filter;

import com.beyond.synclab.ctrlline.domain.user.dto.UserLoginRequestDto;
import com.beyond.synclab.ctrlline.security.handler.UserAuthFailureHandler;
import com.beyond.synclab.ctrlline.security.handler.UserAuthSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class UserLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    public UserLoginFilter(
            AuthenticationManager userAuthenticationManager,
            UserAuthSuccessHandler userAuthSuccessHandler,
            UserAuthFailureHandler userAuthFailureHandler
    ) {
        super(userAuthenticationManager);
        this.authenticationManager = userAuthenticationManager;
        setAuthenticationSuccessHandler(userAuthSuccessHandler);
        setAuthenticationFailureHandler(userAuthFailureHandler);
        setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 엔드포인트
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        // JSON POST 요청만 처리하도록 필터 제한 가능
        log.debug(">> attemptAuthentication 진입");
        if (!request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("Content type not supported");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserLoginRequestDto userLoginRequestDto = objectMapper.readValue(request.getInputStream(), UserLoginRequestDto.class);
            log.debug(">> userLoginRequestDto = {}", userLoginRequestDto);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());

            AuthenticationManager manager = this.getAuthenticationManager();
            log.debug(">> authManager class = {}", manager.getClass());

            log.debug(">> authManager class = {}", this.authenticationManager.getClass());

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
