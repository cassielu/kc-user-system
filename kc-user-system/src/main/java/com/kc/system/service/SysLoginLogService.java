package com.kc.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kc.system.entity.SysLoginLog;

/**
 * 登录日志 Service
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     */
    IPage<SysLoginLog> pageQuery(Page<SysLoginLog> page, String username, Integer status);

    /**
     * 记录登录日志（异步）
     */
    void logLogin(SysLoginLog log);
}
