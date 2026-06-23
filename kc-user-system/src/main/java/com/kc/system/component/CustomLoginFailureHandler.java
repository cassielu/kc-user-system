package com.kc.system.component;

import com.kc.system.entity.SysLoginLog;
import com.kc.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 登录失败处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    private final SysLoginLogService loginLogService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // 获取用户名
        String username = request.getParameter("username");
        
        // 记录登录失败日志
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username != null ? username : "未知");
        loginLog.setIp(getClientIp(request));
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        loginLog.setStatus(0);
        loginLog.setMessage(getErrorMessage(exception));
        loginLog.setLoginTime(LocalDateTime.now());
        
        loginLogService.logLogin(loginLog);
        
        // 跳转到登录页并显示错误
        response.sendRedirect("/login?error");
    }

    /**
     * 获取错误信息
     */
    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return "账号已停用";
        } else if (exception instanceof UsernameNotFoundException) {
            return "用户不存在";
        } else if (exception instanceof BadCredentialsException) {
            return "密码错误";
        } else {
            return "登录失败: " + exception.getMessage();
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
