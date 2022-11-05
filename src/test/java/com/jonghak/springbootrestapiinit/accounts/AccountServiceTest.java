package com.jonghak.springbootrestapiinit.accounts;

import com.jonghak.springbootrestapiinit.common.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest extends BaseTest {

    //junit5에서 rule 없어짐
    //@Rule
    //public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findeByUsername() {
        //Given
        String password = "jonghak2";
        String username = "jonghak2@email.com";
        Set<AccountRole> accountSet = new HashSet<>();
        accountSet.add(AccountRole.ADMIN);
        accountSet.add(AccountRole.USER);
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(accountSet)
                .build();

        this.accountService.saveAccount(account);

        // When
        UserDetailsService userDetailsService = accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
//        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(this.passwordEncoder.matches(password, userDetails.getPassword())).isTrue();

    }

    @Test
    public void findByUsernameFail() {
        String username = "random@email.com";

        /* TEST CASE1 : try catch
        try{
            accountService.loadUserByUsername(username);
            fail("supposed to be failed");
        }catch(UsernameNotFoundException e){
            assertThat(e.getMessage()).containsSequence(username);
        }*/

        // TEST CASE2 : junit4
        /*
        // ExpectedException은 코드 작성하기 전에 먼저 에상을 해줘야함
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        accountService.loadUserByUsername(username);
        */

        // TEST CASE3 : junit5
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername(username));
        assertThat(exception.getMessage()).contains(username);

    }

}