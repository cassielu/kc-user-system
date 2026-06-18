-- =============================================
-- KC 用户管理系统 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS kc
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kc;

DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50) NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name   VARCHAR(50)  DEFAULT '' COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮筱',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 初始管理员账号：admin / 123456
-- BCrypt 加密值对应明文：123456
INSERT INTO sys_user (username, password, real_name, status)
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 1);

-- 测试用户：test / 123456
INSERT INTO sys_user (username, password, real_name, status)
VALUES ('test', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', 1);

-- =============================================
-- 接口凭据表
-- =============================================
DROP TABLE IF EXISTS sys_api_credential;
CREATE TABLE sys_api_credential (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name           VARCHAR(100) NOT NULL COMMENT '凭据名称',
    access_key     VARCHAR(100) NOT NULL COMMENT 'accessKey',
    secret         VARCHAR(200) NOT NULL COMMENT '签名密钥',
    push_url       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '下单请求地址',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    remark         VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_access_key (access_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口凭据表';

-- 初始凭据（来自用户提供的示例）
INSERT INTO sys_api_credential (name, access_key, secret, push_url, status, remark)
VALUES ('测试环境-美团', 'fc21380e7544216f', 'your-secret-key', 'http://candao-api-gateway.paas-qc-vpc.can-dao.com/api', 1, '测试环境默认凭据');

-- =============================================
-- 坐标逆地理编码记录表
-- =============================================
DROP TABLE IF EXISTS geo_reverse_record;
CREATE TABLE geo_reverse_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    batch_no       VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '批次号',
    longitude      DECIMAL(10,6) NOT NULL COMMENT '经度',
    latitude       DECIMAL(10,6) NOT NULL COMMENT '纬度',
    province       VARCHAR(50)  DEFAULT '' COMMENT '省',
    city           VARCHAR(50)  DEFAULT '' COMMENT '市',
    district       VARCHAR(50)  DEFAULT '' COMMENT '区',
    address        VARCHAR(500) DEFAULT '' COMMENT '详细地址',
    status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待解析，1-成功，2-失败',
    error_msg      VARCHAR(200) DEFAULT '' COMMENT '错误信息',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_batch_no (batch_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='坐标逆地理编码记录表';
