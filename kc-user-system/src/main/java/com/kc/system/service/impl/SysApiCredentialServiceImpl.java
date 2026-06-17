package com.kc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kc.system.entity.SysApiCredential;
import com.kc.system.mapper.SysApiCredentialMapper;
import com.kc.system.service.SysApiCredentialService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysApiCredentialServiceImpl extends ServiceImpl<SysApiCredentialMapper, SysApiCredential> implements SysApiCredentialService {

    @Override
    public IPage<SysApiCredential> pageByKeyword(Page<SysApiCredential> page, String keyword) {
        // MyBatis Plus 的 selectPageByKeyword 返回的是 List，我们需要手动包装成分页结果
        // 由于注解查询无法自动分页，这里用 Wrapper 实现分页
        LambdaQueryWrapper<SysApiCredential> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysApiCredential::getName, keyword)
                    .or().like(SysApiCredential::getAccessKey, keyword)
                    .or().like(SysApiCredential::getRemark, keyword));
        }
        wrapper.orderByDesc(SysApiCredential::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<SysApiCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysApiCredential::getName, name);
        if (excludeId != null) {
            wrapper.ne(SysApiCredential::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean existsByAccessKey(String accessKey, Long excludeId) {
        LambdaQueryWrapper<SysApiCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysApiCredential::getAccessKey, accessKey);
        if (excludeId != null) {
            wrapper.ne(SysApiCredential::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public List<SysApiCredential> listActive() {
        return this.list(new LambdaQueryWrapper<SysApiCredential>()
                .eq(SysApiCredential::getStatus, 1)
                .orderByDesc(SysApiCredential::getCreateTime));
    }
}
