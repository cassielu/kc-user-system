package com.kc.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gaode.api")
public class GaodeApiProperties {
    
    /** 高德 API Key */
    private String key;
    
    /** 逆地理编码接口地址 */
    private String reverseGeocodeUrl = "https://restapi.amap.com/v3/geocode/regeo";
    
    /** 每秒最大请求数（免费 key 并发限制） */
    private int rateLimit = 3;
    
    /** 限频窗口（毫秒） */
    private long rateWindow = 1000;
}
