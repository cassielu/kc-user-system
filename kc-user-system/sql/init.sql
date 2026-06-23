-- =============================================
-- KC 用户管理系统 数据库初始化脚本
-- 生产环境只需执行此文件即可
-- 包含：用户表、凭据表、地理编码表、权限管理、操作日志、登录日志
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

-- =============================================
-- 权限管理系统相关表
-- =============================================

-- 1. 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID,0表示顶级',
    menu_name   VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_code   VARCHAR(100) NOT NULL COMMENT '菜单编码(权限标识)',
    menu_type   TINYINT      NOT NULL COMMENT '类型:1-目录,2-菜单,3-按钮',
    url         VARCHAR(200) DEFAULT '' COMMENT '访问路径',
    icon        VARCHAR(50)  DEFAULT '' COMMENT '图标',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1-启用,0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- 2. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_name   VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(100) NOT NULL COMMENT '角色编码',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1-启用,0-禁用',
    remark      VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 3. 角色-菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 4. 用户-角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 初始化权限数据
-- =============================================

-- 菜单数据(对应现有layout.html中的菜单)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, url, icon, sort_order, status) VALUES
(1, 0, '系统管理', 'menu:system', 1, '', 'bi-gear-fill', 1, 1),
(2, 1, '用户管理', 'menu:user:list', 2, '/user/list', 'bi-people-fill', 1, 1),
(3, 1, '接口凭据', 'menu:credential:list', 2, '/credential/list', 'bi-key-fill', 2, 1),
(4, 0, '业务工具', 'menu:business', 1, '', 'bi-tools', 2, 1),
(5, 4, '模拟下单', 'menu:order:simulate', 2, '/order/simulate', 'bi-cart-plus-fill', 1, 1),
(6, 4, '坐标解析', 'menu:geo:reverse', 2, '/geo/reverse', 'bi-geo-alt-fill', 2, 1);

-- 角色数据
INSERT INTO sys_role (id, role_name, role_code, status, remark) VALUES
(1, '系统管理员', 'ROLE_ADMIN', 1, '拥有所有权限'),
(2, '普通用户', 'ROLE_USER', 1, '只能访问业务工具');

-- 管理员拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE status = 1;

-- 普通用户只有业务工具权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 4), (2, 5), (2, 6);

-- 为admin用户(id=1)分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 为test用户(id=2)分配普通用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);

-- =============================================
-- 角色管理菜单
-- =============================================

-- 添加角色管理菜单(在系统管理下)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, url, icon, sort_order, status) VALUES
(7, 1, '角色管理', 'menu:role:list', 2, '/role/list', 'bi-shield-fill-check', 3, 1);

-- 添加角色管理子权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, url, icon, sort_order, status) VALUES
(8, 7, '分配权限', 'menu:role:permission', 3, '', '', 1, 1),
(9, 7, '新增角色', 'menu:role:add', 3, '', '', 2, 1),
(10, 7, '编辑角色', 'menu:role:edit', 3, '', '', 3, 1),
(11, 7, '删除角色', 'menu:role:delete', 3, '', '', 4, 1);

-- 为管理员角色分配角色管理权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 7), (1, 8), (1, 9), (1, 10), (1, 11);

-- =============================================
-- 操作日志表
-- =============================================

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        DEFAULT NULL COMMENT '操作用户ID',
    username    VARCHAR(100)  DEFAULT NULL COMMENT '操作用户名',
    operation   VARCHAR(200)  NOT NULL COMMENT '操作描述',
    method      VARCHAR(200)  DEFAULT NULL COMMENT '请求方法',
    params      TEXT          COMMENT '请求参数',
    ip          VARCHAR(50)   DEFAULT NULL COMMENT 'IP地址',
    user_agent  VARCHAR(500)  DEFAULT NULL COMMENT '浏览器标识',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1成功 0失败',
    error_msg   TEXT          COMMENT '错误信息',
    cost_time   BIGINT        DEFAULT NULL COMMENT '耗时(毫秒)',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 添加操作日志菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, url, icon, sort_order, status) VALUES
(12, 1, '操作日志', 'menu:log:list', 2, '/log/list', 'bi-journal-text', 4, 1);

-- 为管理员分配操作日志权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 12);

-- =============================================
-- 登录日志表
-- =============================================

CREATE TABLE IF NOT EXISTS sys_login_log (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    username    VARCHAR(100)  NOT NULL COMMENT '登录用户名',
    ip          VARCHAR(50)   DEFAULT NULL COMMENT '登录IP地址',
    user_agent  VARCHAR(500)  DEFAULT NULL COMMENT '浏览器标识',
    status      TINYINT       NOT NULL COMMENT '状态：1成功 0失败',
    message     VARCHAR(500)  DEFAULT NULL COMMENT '提示信息',
    login_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 添加登录日志菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, url, icon, sort_order, status) VALUES
(13, 1, '登录日志', 'menu:log:login', 2, '/log/login', 'bi-box-arrow-in-right', 5, 1);

-- 为管理员分配登录日志权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 13);
