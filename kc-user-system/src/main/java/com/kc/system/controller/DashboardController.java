package com.kc.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    /**
     * 欢迎页/仪表盘
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
