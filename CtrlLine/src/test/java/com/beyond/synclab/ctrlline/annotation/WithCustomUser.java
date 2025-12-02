package com.beyond.synclab.ctrlline.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

// @WithSecurityContext
//   - 인증 정보를 생성하는 팩토리 클래스를 지정한다.
//   - 테스트 메서드 위에 @WithCustomUser를 붙이면, 지정된 팩토리가 호출되어 SecurityContext가 만들어진다.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomUserSecurityContextFactory.class)
public @interface WithCustomUser {
    String username() default "hong123@test.com";
    String[] roles() default {"ROLE_USER"};
}
