package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.security.filter.UserLoginFilter;
import com.beyond.synclab.ctrlline.security.handler.RestAccessDeniedHandler;
import com.beyond.synclab.ctrlline.security.handler.RestAuthenticationEntryPoint;
import com.beyond.synclab.ctrlline.security.handler.UserAuthFailureHandler;
import com.beyond.synclab.ctrlline.security.handler.UserAuthSuccessHandler;
import com.beyond.synclab.ctrlline.security.jwt.JwtFilter;
import com.beyond.synclab.ctrlline.security.jwt.JwtStoreService;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserAuthSuccessHandler userAuthSuccessHandler;
    private final UserAuthFailureHandler userAuthFailureHandler;
    private final JwtStoreService jwtStoreService;
    private final JwtUtil jwtUtil;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    private static final String[] AUTH_WHITE_LIST = {
            // Swagger
            "/swagger-ui/**", "/v3/api-docs/**",

            // Auth API
            "/api/v1/auth/login",
            "/api/v1/auth/token/**",
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain userSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        log.debug("Configuring SecurityFilterChain");

        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource));

        http
                // 🔹 인증/인가 실패 핸들러
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(restAccessDeniedHandler)   // 403
                )
                // 🔹 JWT 필터
                .addFilterBefore(
                        new JwtFilter(jwtUtil, jwtStoreService, restAuthenticationEntryPoint),
                        UsernamePasswordAuthenticationFilter.class
                );

        // 🔹 요청 인가 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITE_LIST).permitAll()
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .anyRequest().authenticated()
                );

        UserLoginFilter userLoginFilter = new UserLoginFilter(authenticationManager, userAuthSuccessHandler, userAuthFailureHandler);

        http.addFilterAt(userLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
