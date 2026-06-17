package com.kc.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kc.system.entity.SysApiCredential;

/**
 * 接口凭据 Service
 */
public interface SysApiCredentialService extends IService<SysApiCredential> {

    /** 分页查询 */
    IPage<SysApiCredential> pageByKeyword(Page<SysApiCredential> page, String keyword);

    /** 是否存在同名凭据 */
    boolean existsByName(String name, Long excludeId);

    /** 是否存在同 accessKey */
    boolean existsByAccessKey(String accessKey, Long excludeId);

    /** 查询所有启用的凭据（供模拟下单下拉使用） */
    java.util.List<SysApiCredential> listActive();
}
