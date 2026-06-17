package com.kc.system.config;

import com.kc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(sysUserService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF（页面表单需要携带 _csrf，使用 Thymeleaf 会自动注入）
            .csrf().and()
            .authorizeRequests()
                // 放行登录页和静态资源
                .antMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // 其余请求需要认证
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/user/list", true)
                .failureHandler((request, response, exception) -> {
                    // 区分账号禁用和密码错误，跳转不同的 URL 参数
                    if (exception instanceof DisabledException) {
                        response.sendRedirect("/login?disabled");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
            .sessionManagement()
                .maximumSessions(1);
    }
}
