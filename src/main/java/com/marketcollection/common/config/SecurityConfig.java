package com.marketcollection.common.config;

import com.marketcollection.common.auth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                .formLogin()
                    .loginPage("/member/login")
                .and()
                    .authorizeRequests()
                    .antMatchers("/", "/main", "/member/login", "/categories/**", "/items/**", "/reviews/**", "/css/**", "/image/**", "/js/**", "/h2-console/**", "/swagger-ui.html", "/profile").permitAll()
                    .antMatchers("/review/**").hasAnyRole("USER, ADMIN")
                    .antMatchers("/api/v1/admin/**", "/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                    .logout()
                        .logoutSuccessUrl("/main")
                .and()
                    .oauth2Login()
                        .userInfoEndpoint()
                            .userService(customOAuth2UserService);
    }

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring().antMatchers("/static/image/favicon.ico");
    }
}
