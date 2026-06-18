package com.kc.system.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 高德 API 限频器（滑动窗口算法）
 */
@Slf4j
@Component
public class RateLimiter {
    
    private final AtomicLong requestCount = new AtomicLong(0);
    private volatile long windowStart = System.currentTimeMillis();
    private final int maxRequests;
    private final long windowMs;

    public RateLimiter(@Value("${gaode.api.rate-limit:3}") int maxRequests,
                       @Value("${gaode.api.rate-window:1000}") long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * 获取请求许可（如超限则阻塞等待）
     */
    public synchronized void acquire() throws InterruptedException {
        long now = System.currentTimeMillis();
        
        // 窗口过期，重置计数器
        if (now - windowStart >= windowMs) {
            requestCount.set(0);
            windowStart = now;
        }
        
        // 达到限频阈值，等待下一窗口
        while (requestCount.get() >= maxRequests) {
            long waitTime = windowMs - (System.currentTimeMillis() - windowStart);
            if (waitTime > 0) {
                log.debug("[RateLimiter] 触发限频，等待 {}ms", waitTime + 100);
                Thread.sleep(waitTime + 100); // 额外缓冲 100ms
            }
            requestCount.set(0);
            windowStart = System.currentTimeMillis();
        }
        
        requestCount.incrementAndGet();
    }
}
