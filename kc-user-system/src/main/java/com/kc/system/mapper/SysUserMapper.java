package com.kc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kc.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按用户名查询用户
     */
    SysUser findByUsername(@Param("username") String username);

    /**
     * 分页查询（支持用户名模糊搜索）
     */
    IPage<SysUser> selectPageByKeyword(Page<SysUser> page, @Param("keyword") String keyword);
}
