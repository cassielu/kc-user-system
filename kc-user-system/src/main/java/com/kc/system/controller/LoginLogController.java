package com.kc.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kc.system.entity.SysLoginLog;
import com.kc.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/log")
@RequiredArgsConstructor
public class LoginLogController {

    private final SysLoginLogService loginLogService;

    private static final int PAGE_SIZE = 20;

    /**
     * 登录日志列表页面
     */
    @GetMapping("/login")
    @PreAuthorize("hasAuthority('menu:log:login')")
    public String loginLogPage(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) Integer status,
                               Model model) {
        Page<SysLoginLog> pageParam = new Page<>(page, PAGE_SIZE);
        IPage<SysLoginLog> result = loginLogService.pageQuery(pageParam, username, status);
        
        model.addAttribute("logs", result.getRecords());
        model.addAttribute("page", result);
        model.addAttribute("username", username);
        model.addAttribute("status", status);
        
        return "log/login";
    }
}
