package com.kc.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kc.system.entity.SysOperationLog;
import com.kc.system.service.SysOperationLogService;
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
public class OperationLogController {

    private final SysOperationLogService operationLogService;

    private static final int PAGE_SIZE = 20;

    /**
     * 操作日志列表页面
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('menu:log:list')")
    public String listPage(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(required = false) String username,
                           @RequestParam(required = false) String operation,
                           Model model) {
        Page<SysOperationLog> pageParam = new Page<>(page, PAGE_SIZE);
        IPage<SysOperationLog> result = operationLogService.pageQuery(pageParam, username, operation);
        
        model.addAttribute("logs", result.getRecords());
        model.addAttribute("page", result);
        model.addAttribute("username", username);
        model.addAttribute("operation", operation);
        
        return "log/list";
    }
}
