package com.kc.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kc.system.entity.SysOperationLog;

/**
 * 操作日志 Service
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     */
    IPage<SysOperationLog> pageQuery(Page<SysOperationLog> page, String username, String operation);

    /**
     * 记录操作日志（异步）
     */
    void logOperation(SysOperationLog log);
}
