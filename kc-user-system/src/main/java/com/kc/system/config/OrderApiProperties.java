package com.kc.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "order-api")
public class OrderApiProperties {
    /** 下单接口完整请求地址 */
    private String url = "http://candao-api-gateway.paas-qc-vpc.can-dao.com/api";
    private int connectTimeout = 10000;
    private int readTimeout = 30000;
}
