package com.kc.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kc.system.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface SysUserService extends IService<SysUser>, UserDetailsService {

    /**
     * 分页查询用户列表
     */
    IPage<SysUser> pageByKeyword(Page<SysUser> page, String keyword);

    /**
     * 新增用户（密码加密）
     */
    void addUser(SysUser user);

    /**
     * 更新用户（密码留空则不修改）
     */
    void updateUser(SysUser user);

    /**
     * 用户名是否已存在
     */
    boolean existsByUsername(String username);

    /**
     * 清除用户认证缓存
     */
    void evictAuthCache(String username);
}
