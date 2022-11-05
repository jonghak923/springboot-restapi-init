package com.jonghak.springbootrestapiinit.configs;

import com.jonghak.springbootrestapiinit.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountService accountService;

    @Autowired
    TokenStore tokenStore;

    @Autowired
    AppPropertices appPropertices;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(appPropertices.getClientId())
                .authorizedGrantTypes("password", "refresh_token") // 인증서버가 지원할 토큰종류
                .scopes("read", "write")
                .secret(this.passwordEncoder.encode(appPropertices.getClientSecret()))
                .accessTokenValiditySeconds(10 * 60)    // 토큰 유효한 시간 10분
//                .accessTokenValiditySeconds(60)    // 토큰 유효한 시간 1분
                .refreshTokenValiditySeconds(6 * 10 * 60);  // 재생성 토큰 유효한 시간 1시간
//                .refreshTokenValiditySeconds(60);  // 재생성 토큰 유효한 시간 1분

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(accountService)
                .tokenStore(tokenStore);
    }
}
