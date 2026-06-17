package com.kc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kc.system.entity.SysApiCredential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysApiCredentialMapper extends BaseMapper<SysApiCredential> {

    /** 按关键字分页查询（name / access_key / remark） */
    @Select("<script>"
            + "SELECT * FROM sys_api_credential "
            + "<where>"
            + "  <if test='keyword != null and keyword != \"\"'>"
            + "    AND (name LIKE CONCAT('%', #{keyword}, '%')"
            + "      OR access_key LIKE CONCAT('%', #{keyword}, '%')"
            + "      OR remark LIKE CONCAT('%', #{keyword}, '%'))"
            + "  </if>"
            + "</where>"
            + "ORDER BY create_time DESC"
            + "</script>")
    List<SysApiCredential> selectPageByKeyword(@Param("keyword") String keyword);
}
