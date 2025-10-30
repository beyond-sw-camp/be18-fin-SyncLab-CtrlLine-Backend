package com.beyond.synclab.ctrlline.security.filter;

import com.beyond.synclab.ctrlline.domain.user.dto.UserLoginRequestDto;
import com.beyond.synclab.ctrlline.security.handler.UserAuthFailureHandler;
import com.beyond.synclab.ctrlline.security.handler.UserAuthSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserLoginFilterTest {

    private AuthenticationManager authenticationManager;
    private UserAuthSuccessHandler successHandler;
    private UserAuthFailureHandler failureHandler;
    private UserLoginFilter userLoginFilter;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        successHandler = mock(UserAuthSuccessHandler.class);
        failureHandler = mock(UserAuthFailureHandler.class);
        userLoginFilter = new UserLoginFilter(authenticationManager, successHandler, failureHandler);
    }

    @Test
    @DisplayName("JSON 로그인 요청 시 AuthenticationManager가 정상 호출된다")
    void attemptAuthentication_success() throws Exception {
        // given
        UserLoginRequestDto dto = UserLoginRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        when(request.getInputStream()).thenReturn(
                new MockServletInputStream(new ByteArrayInputStream(json.getBytes()))
        );

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        // when
        Authentication result = userLoginFilter.attemptAuthentication(request, response);

        // then
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());
        UsernamePasswordAuthenticationToken token = captor.getValue();

        assertThat(token.getPrincipal()).isEqualTo("test@example.com");
        assertThat(token.getCredentials()).isEqualTo("password123");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("JSON이 아닌 Content-Type이면 AuthenticationServiceException 발생")
    void attemptAuthentication_invalidContentType() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContentType()).thenReturn(MediaType.TEXT_PLAIN_VALUE);

        // then
        assertThrows(AuthenticationServiceException.class, () ->
                userLoginFilter.attemptAuthentication(request, response)
        );
    }

    // ✅ 내부용 MockServletInputStream 클래스
    static class MockServletInputStream extends jakarta.servlet.ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {}
    }
}