package com.kc.system;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时工具类：生成 BCrypt 加密后的密码，获取后可删除
 * 直接右键 Run 即可，无需启动 Spring 上下文
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"123456", "admin123", "test123"};
        for (String pwd : passwords) {
            System.out.println("明文: " + pwd + "  →  BCrypt: " + encoder.encode(pwd));
        }
    }
}
