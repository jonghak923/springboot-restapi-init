package com.jonghak.springbootrestapiinit.accounts;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME) // 언제까지 이 정보를 유지할 것이냐? RUNTIME : 실행
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") // SpEL(spring expression language)
public @interface CurrentUser {
}
