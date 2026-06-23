package com.kc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kc.system.entity.SysOperationLog;
import com.kc.system.mapper.SysOperationLogMapper;
import com.kc.system.service.SysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志 Service 实现
 */
@Slf4j
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    @Override
    public IPage<SysOperationLog> pageQuery(Page<SysOperationLog> page, String username, String operation) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(SysOperationLog::getUsername, username);
        }
        
        if (StringUtils.hasText(operation)) {
            wrapper.like(SysOperationLog::getOperation, operation);
        }
        
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    @Async
    public void logOperation(SysOperationLog logEntity) {
        try {
            save(logEntity);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
}
