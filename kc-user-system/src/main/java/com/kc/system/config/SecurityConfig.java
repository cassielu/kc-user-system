package com.kc.system.config;

import com.kc.system.component.CustomLoginFailureHandler;
import com.kc.system.component.CustomLoginSuccessHandler;
import com.kc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    /**
     * 配置HTTP防火墙，处理不支持的HTTP方法
     * 生产环境中可能会收到TRACE等方法的扫描请求，返回405而不是抛异常
     */
    @Bean
    public HttpFirewall allowStrictHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 使用默认配置即可，TRACE等方法默认就被拒绝
        // 这里主要是确保防火墙正常工作
        return firewall;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(sysUserService).passwordEncoder(passwordEncoder);
    }

    /**
     * 配置HTTP防火墙
     * 生产环境中可能会收到TRACE等方法的扫描请求
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.httpFirewall(allowStrictHttpFirewall());
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
                .successHandler(customLoginSuccessHandler)
                .failureHandler(customLoginFailureHandler)
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
