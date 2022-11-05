package com.jonghak.springbootrestapiinit.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .anonymous()
                .and()
            .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**")// GET 요청은 인증필요 없음
                    //.anonymous() // 익명의 사용자의 접근을 허용 : 인증된 사용자 접근X (authenticated)
                    .permitAll() // 무조건 접근을 허용
                .anyRequest()
                    .authenticated() // 그 외 인증 필요
                .and()
            .exceptionHandling() // 인증이 잘못되거나 권한이 없는 경우 등 Exception 발생 시
                .accessDeniedHandler(new OAuth2AccessDeniedHandler()); // 접근 권한이 없는 경우에 사용 403 return
    }
}
