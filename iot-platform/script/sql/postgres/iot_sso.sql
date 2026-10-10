-- ============================================================================
-- Snail-IoT - 单点登录（SSO）模块建表与菜单初始化脚本
--
-- 设计说明：
--   1. 统一认证中心：复用 sys_client（PC/APP 客户端） + Sa-Token 登录态；
--   2. OAuth2/OIDC 服务端：客户端注册表 iot_oauth2_client，Sa-Token OAuth2 插件驱动；
--   3. CAS 双角色：作为 CAS Server 对外提供 /cas/* 端点，
--      同时可作为 Client 对接外部 CAS / MaxKey / TOPIAM；
--   4. 第三方社交登录：复用 iot sys_social（JustAuth 驱动）。
--
-- 使用前提：
--   1. 已导入 iot 基础脚本（含 sys_client / sys_menu / sys_social）
--   2. 已导入 iot_platform.sql（含一级目录 menu_id = 1900000000000000001）
--
-- 执行方式：
--   psql -U postgres -d ruoyi -f iot_sso.sql
--   或 docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_sso.sql
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. OAuth2 客户端注册表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_oauth2_client;
CREATE TABLE iot_oauth2_client
(
    id                     BIGINT       NOT NULL,
    client_id              VARCHAR(64)  NOT NULL,
    client_secret          VARCHAR(128) NOT NULL,
    client_name            VARCHAR(128),
    contract_scopes        VARCHAR(500)          DEFAULT 'openid,userid,userinfo',
    allow_redirect_uris    VARCHAR(1000),
    allow_grant_types      VARCHAR(255)          DEFAULT 'authorization_code,password,refresh_token,client_credentials',
    subject_id             VARCHAR(128),
    access_token_timeout   BIGINT                DEFAULT 7200,
    refresh_token_timeout  BIGINT                DEFAULT 2592000,
    client_token_timeout   BIGINT                DEFAULT 7200,
    max_access_token_count INT                   DEFAULT 12,
    max_refresh_token_count INT                  DEFAULT 3,
    max_client_token_count INT                   DEFAULT 12,
    is_new_refresh         CHAR(1)               DEFAULT '1',
    is_auto_confirm        CHAR(1)               DEFAULT '0',
    device_type            VARCHAR(32)           DEFAULT 'pc',
    status                 CHAR(1)               DEFAULT '0',
    del_flag               CHAR(1)               DEFAULT '0',
    remark                 VARCHAR(500)          DEFAULT '',
    create_dept            BIGINT,
    create_by              BIGINT,
    create_time            TIMESTAMP,
    update_by              BIGINT,
    update_time            TIMESTAMP,
    CONSTRAINT iot_oauth2_client_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX iot_oauth2_client_id_uk ON iot_oauth2_client (client_id);
COMMENT ON TABLE iot_oauth2_client IS 'OAuth2 客户端注册表';
COMMENT ON COLUMN iot_oauth2_client.id IS '主键ID';
COMMENT ON COLUMN iot_oauth2_client.client_id IS '客户端标识';
COMMENT ON COLUMN iot_oauth2_client.client_secret IS '客户端密钥';
COMMENT ON COLUMN iot_oauth2_client.client_name IS '客户端名称';
COMMENT ON COLUMN iot_oauth2_client.contract_scopes IS '签约的 scope 列表(逗号分隔)';
COMMENT ON COLUMN iot_oauth2_client.allow_redirect_uris IS '允许的重定向地址(逗号分隔)';
COMMENT ON COLUMN iot_oauth2_client.allow_grant_types IS '允许的授权类型(逗号分隔)';
COMMENT ON COLUMN iot_oauth2_client.subject_id IS '授权对象(为空时默认取当前登录用户ID)';
COMMENT ON COLUMN iot_oauth2_client.access_token_timeout IS 'AccessToken 有效期(秒)';
COMMENT ON COLUMN iot_oauth2_client.refresh_token_timeout IS 'RefreshToken 有效期(秒)';
COMMENT ON COLUMN iot_oauth2_client.client_token_timeout IS 'ClientToken 有效期(秒)';
COMMENT ON COLUMN iot_oauth2_client.max_access_token_count IS 'AccessToken 最大保留数量';
COMMENT ON COLUMN iot_oauth2_client.max_refresh_token_count IS 'RefreshToken 最大保留数量';
COMMENT ON COLUMN iot_oauth2_client.max_client_token_count IS 'ClientToken 最大保留数量';
COMMENT ON COLUMN iot_oauth2_client.is_new_refresh IS '每次刷新是否重新生成 RefreshToken(1是 0否)';
COMMENT ON COLUMN iot_oauth2_client.is_auto_confirm IS '是否自动授权(跳过确认页)(1是 0否)';
COMMENT ON COLUMN iot_oauth2_client.device_type IS '适配设备类型(pc/android/ios/h5)';
COMMENT ON COLUMN iot_oauth2_client.status IS '状态(0正常 1停用)';
COMMENT ON COLUMN iot_oauth2_client.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 2. 演示数据：Web 端 与 移动端 各一个 OAuth2 客户端
-- ----------------------------------------------------------------------------
INSERT INTO iot_oauth2_client (id, client_id, client_secret, client_name, contract_scopes, allow_redirect_uris,
                               allow_grant_types, subject_id, device_type, status, del_flag, remark,
                               create_dept, create_by, create_time, update_by, update_time)
VALUES (1960000000000000301, 'iot-web', 'iot-web-secret', 'IoT Web 管理端',
        'openid,userid,userinfo', 'http://localhost/oauth2/callback,http://localhost/social-callback',
        'authorization_code,password,refresh_token,client_credentials', NULL, 'pc', '0', '0', 'Web 端 OAuth2 客户端',
        103, 1, now(), 1, now()),
       (1960000000000000302, 'iot-mobile', 'iot-mobile-secret', 'IoT 移动端',
        'openid,userid,userinfo', 'http://localhost:81/oauth2/callback',
        'authorization_code,password,refresh_token,client_credentials', NULL, 'android', '0', '0', '移动端 OAuth2 客户端',
        103, 1, now(), 1, now());

-- ----------------------------------------------------------------------------
-- 3. 菜单与权限（挂在 iot_platform.sql 的一级目录 1900000000000000001 下）
--    菜单 ID 使用 1900000000000002001 起的新段，避免与既有脚本冲突
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000002001 AND 1900000000000002999;

-- 3.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000002001, 'OAuth2客户端', 1900000000000000001, 11, 'oauth2-client', 'iot/oauth2Client/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:oauth2Client:list', 'lock', 103, 1, now(), 1, now(), 'OAuth2 客户端注册管理'),
       (1900000000000002002, 'SSO会话', 1900000000000000001, 12, 'sso-session', 'iot/ssoSession/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:sso:session:list', 'monitor', 103, 1, now(), 1, now(), 'SSO 单点登录会话与全局登出');

-- 3.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000002101, '客户端查询', 1900000000000002001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:oauth2Client:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002102, '客户端新增', 1900000000000002001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:oauth2Client:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002103, '客户端修改', 1900000000000002001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:oauth2Client:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002104, '客户端删除', 1900000000000002001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:oauth2Client:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002105, '客户端导出', 1900000000000002001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:oauth2Client:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002111, '会话查询', 1900000000000002002, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:sso:session:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000002112, '全局登出', 1900000000000002002, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:sso:logout', '#', 103, 1, now(), 1, now(), '强制下线指定用户全部设备');

-- 3.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000002001 AND 1900000000000002999
ON CONFLICT DO NOTHING;
