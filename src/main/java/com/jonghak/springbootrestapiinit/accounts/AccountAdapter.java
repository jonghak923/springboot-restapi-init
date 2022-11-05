package com.jonghak.springbootrestapiinit.accounts;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * spring security의 User객체를 Account객체로 변환하는 Class
 */
public class AccountAdapter extends User {

    private final Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                    .collect(Collectors.toSet());
    }

    public Account getAccount() {
        return account;
    }


}
