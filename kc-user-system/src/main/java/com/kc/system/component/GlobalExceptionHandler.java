package com.kc.system.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 处理安全相关的异常，避免在生产环境暴露敏感信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理HTTP方法不允许的异常
     * 当有人使用TRACE等不允许的方法访问时，返回405而不是500错误
     */
    @ExceptionHandler(RequestRejectedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, Object> handleRequestRejectedException(RequestRejectedException ex, HttpServletRequest request) {
        // 记录警告日志，但不抛出异常堆栈
        log.warn("拒绝的请求: {} {}, 原因: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        
        // 返回简洁的错误信息
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 405);
        result.put("message", "HTTP方法不允许");
        return result;
    }
}
