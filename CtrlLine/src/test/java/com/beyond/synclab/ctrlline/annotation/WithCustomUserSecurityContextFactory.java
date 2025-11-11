package com.beyond.synclab.ctrlline.annotation;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import java.util.Arrays;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


// WithSecurityContextFactory
//   - Spring Security가 제공하는 인터페이스이다.
//   - @WithSecurityContext에 지정된 factory가 구현해야 하는 타입이다.
public class WithCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String username = annotation.username();
        String[] roles = annotation.roles();

        Users testUser = Users.builder()
            .email(username)
            .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(testUser);

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(customUserDetails, null,
                Arrays.stream(roles)
                    .map(SimpleGrantedAuthority::new)
                    .toList()
            );

        context.setAuthentication(auth);
        return context;
    }
}
