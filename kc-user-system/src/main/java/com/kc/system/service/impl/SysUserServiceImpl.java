package com.kc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kc.system.entity.SysUser;
import com.kc.system.mapper.SysUserMapper;
import com.kc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 按用户名加载用户（Spring Security 认证入口）
     * 先查 Redis，命中则直接返回，未命中才查 DB 并写入缓存，TTL = 30 分钟
     */
    @Override
    @Cacheable(value = "user:auth", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        // 通过 enabled=false 让 Spring Security 抛出 DisabledException，而非简单返回密码错误
        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,   // enabled
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                true,                    // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * 分页查询，TTL = 2 分钟
     * key 由页码 + 页大小 + 关键字组成
     */
    @Override
    @Cacheable(value = "user:page", key = "#page.current + ':' + #page.size + ':' + (#keyword ?: '')")
    public IPage<SysUser> pageByKeyword(Page<SysUser> page, String keyword) {
        return sysUserMapper.selectPageByKeyword(page, keyword);
    }

    /**
     * 按 ID 查询用户详情，TTL = 10 分钟
     */
    @Override
    @Cacheable(value = "user:id", key = "#id")
    public SysUser getById(java.io.Serializable id) {
        return super.getById(id);
    }

    /**
     * 新增用户：清除分页缓存（列表数据已变化）
     */
    @Override
    @CacheEvict(value = "user:page", allEntries = true)
    public void addUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sysUserMapper.insert(user);
    }

    /**
     * 更新用户：
     *   1. 清除该用户的 ID 缓存
     *   2. 清除该用户的认证缓存（用旧 username 作 key）
     *   3. 清除所有分页缓存
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "user:id",   key = "#user.id"),
            @CacheEvict(value = "user:page",  allEntries = true)
    })
    public void updateUser(SysUser user) {
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 手动清除旧 username 的认证缓存
        evictAuthCache(existing.getUsername());
        // 密码留空则不修改
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        sysUserMapper.updateById(user);
    }

    /**
     * 删除用户：清除 ID 缓存、认证缓存、分页缓存
     */
    @Override
    @CacheEvict(value = "user:page", allEntries = true)
    public boolean removeById(java.io.Serializable id) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing != null) {
            evictAuthCache(existing.getUsername());
            evictIdCache(id);
        }
        return super.removeById(id);
    }

    /** 手动清除认证缓存（username 为 key） */
    @CacheEvict(value = "user:auth", key = "#username")
    public void evictAuthCache(String username) { }

    /** 手动清除 ID 缓存 */
    @CacheEvict(value = "user:id", key = "#id")
    public void evictIdCache(java.io.Serializable id) { }

    @Override
    public boolean existsByUsername(String username) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        ) > 0;
    }
}
