package com.jonghak.springbootrestapiinit.configs;

import com.jonghak.springbootrestapiinit.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * authenticationManager를 bean으로 노출시키기 위해
     * AuthorizationServer, ResourceServer에서 authenticationManager를 사용하기 위해
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * AuthenticationManager를 어떻게 사용할 것인지 정의
     * 내가 만든 UserDetailsServcie, PasswordEncoding을 등록
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    /**
     * Spring Security 예외처리
     * servlet에서 Filter를 적용할지 여부 결정 (Spring Security 인입 전 예외처리)
     * @param web
     * @throws Exception
     */
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().mvcMatchers("/docs/index.html");
//
//        // SpringBoot에서 제공하는 static Resource의 기본 위치를 가져와서 Spring Security가 적용되지 않도록 설정
//        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//
//
//    }

    /**
     * Spring Security 예외처리
     * Spring Security 인입 후 예외처리 적용 (Filter에서 걸려지는 것보다 많은 일을 처리함)
     * Spring Security 기본 폼 인증 설정
     * @param http
     * @throws Exception
     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
////        http.authorizeRequests()
////                .mvcMatchers("/docs/index.html").anonymous()
////                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//
//        http
//                .anonymous() // 익명사용자 허용
//                    .and()
//                .formLogin() // 기본 제공 from인증 로그인페이지 사용
//                    .and()
//                .authorizeRequests() //허용할 요청
//                .mvcMatchers(HttpMethod.GET, "/api/**").authenticated() // get /api/** 인증필요
//                .anyRequest().authenticated(); // 그 외 url 인증필요
//
//    }


}
