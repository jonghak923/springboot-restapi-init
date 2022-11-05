package com.jonghak.springbootrestapiinit.configs;

import com.jonghak.springbootrestapiinit.accounts.Account;
import com.jonghak.springbootrestapiinit.accounts.AccountRepository;
import com.jonghak.springbootrestapiinit.accounts.AccountRole;
import com.jonghak.springbootrestapiinit.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppPropertices appPropertices;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                // java 11 미만 version일 경우 Set.of를 사용하지 못함
                Set<AccountRole> accountSet = new HashSet<>();
                accountSet.add(AccountRole.ADMIN);
                accountSet.add(AccountRole.USER);

                Account admin = Account.builder()
                        .email(appPropertices.getAdminUsername())
                        .password(appPropertices.getAdminPassword())
                        .roles(Set.of(AccountRole.ADMIN))
                        .build();
                accountService.saveAccount(admin);

                Account user = Account.builder()
                        .email(appPropertices.getUserUsername())
                        .password(appPropertices.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(user);
            }
        };
    }
}
