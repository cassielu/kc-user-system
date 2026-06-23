package com.kc.system.component;

import com.kc.system.entity.SysOperationLog;
import com.kc.system.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志记录工具
 */
@Component
@RequiredArgsConstructor
public class OperationLogHelper {

    private final SysOperationLogService operationLogService;

    /**
     * 记录操作日志
     */
    public void log(String operation, String method, String params, boolean success, String errorMsg, Long costTime) {
        try {
            SysOperationLog log = new SysOperationLog();
            
            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                log.setUsername(user.getUsername());
            }
            
            // 获取请求信息
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                log.setIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
            
            log.setOperation(operation);
            log.setMethod(method);
            log.setParams(truncate(params, 2000));
            log.setStatus(success ? 1 : 0);
            log.setErrorMsg(truncate(errorMsg, 2000));
            log.setCostTime(costTime);
            
            operationLogService.logOperation(log);
        } catch (Exception e) {
            // 日志记录失败不应影响主流程
        }
    }

    /**
     * 记录成功操作
     */
    public void logSuccess(String operation, String method, String params, Long costTime) {
        log(operation, method, params, true, null, costTime);
    }

    /**
     * 记录失败操作
     */
    public void logError(String operation, String method, String params, String errorMsg, Long costTime) {
        log(operation, method, params, false, errorMsg, costTime);
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
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
