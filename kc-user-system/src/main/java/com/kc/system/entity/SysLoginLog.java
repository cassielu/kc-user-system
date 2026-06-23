package com.kc.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 登录IP地址 */
    private String ip;

    /** 浏览器标识 */
    private String userAgent;

    /** 状态：1成功 0失败 */
    private Integer status;

    /** 提示信息 */
    private String message;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
