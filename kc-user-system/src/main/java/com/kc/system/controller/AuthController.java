package com.kc.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * 登录页面（GET /login）
     * POST /login 由 Spring Security 的 formLogin 自动处理
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 首页重定向到用户列表
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/user/list";
    }
}
