package com.kc.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.kc.system.mapper")
@EnableCaching
@EnableAsync
public class KcUserSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(KcUserSystemApplication.class, args);
    }
}
