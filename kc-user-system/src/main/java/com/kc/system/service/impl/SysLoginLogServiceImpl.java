package com.kc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kc.system.entity.SysLoginLog;
import com.kc.system.mapper.SysLoginLogMapper;
import com.kc.system.service.SysLoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录日志 Service 实现
 */
@Slf4j
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public IPage<SysLoginLog> pageQuery(Page<SysLoginLog> page, String username, Integer status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        
        return page(page, wrapper);
    }

    @Override
    @Async
    public void logLogin(SysLoginLog logEntity) {
        try {
            save(logEntity);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }
}
