package com.kc.system.config;

import com.kc.system.config.OrderApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置（用于调用外部下单接口）
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final OrderApiProperties orderApiProperties;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(orderApiProperties.getConnectTimeout());
        factory.setReadTimeout(orderApiProperties.getReadTimeout());
        return new RestTemplate(factory);
    }
}
