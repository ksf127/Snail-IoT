-- ============================================================================
-- Snail-IoT - 权限模块（数据线 + 服务线）建表与菜单初始化脚本
--
-- 设计说明（三线模型）：
--   权限线：部门(sys_dept) / 用户(sys_user) / 角色(sys_role)  —— 直接复用 iot，本脚本不建表
--   数据线：分组(iot_group) -> 数据集(iot_dataset)             —— 仅用于数据集归类，与部门/用户/角色无直接关系
--   服务线：应用(iot_app)                                     —— 绑定数据集(引用) + 授权角色，三线交汇点
--   关联：iot_app_dataset(应用引用数据集) / iot_app_role(应用授权角色)
--         iot_dataset_role(数据集对角色开放的数据范围)
--
-- 使用前提：
--   1. 已导入 iot 基础脚本 postgres_iot_vue.sql（含 sys_dept/sys_user/sys_role/sys_menu 等）
--   2. 已导入 iot_platform.sql（含一级目录 menu_id=1900000000000000001）
--
-- 执行方式：
--   psql -U postgres -d ruoyi -f iot_permission.sql
--   或 docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_permission.sql
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 分组表（数据线：数据的文件夹）
--    仅用于数据集归类，与部门/用户/角色/应用无任何直接关系
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_group;
CREATE TABLE iot_group
(
    group_id    BIGINT       NOT NULL,
    group_code  VARCHAR(64)  NOT NULL,
    group_name  VARCHAR(128) NOT NULL,
    parent_id   BIGINT                DEFAULT 0,
    order_num   INT                   DEFAULT 0,
    status      CHAR(1)               DEFAULT '0',
    del_flag    CHAR(1)               DEFAULT '0',
    remark      VARCHAR(500)          DEFAULT '',
    create_dept BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT iot_group_pk PRIMARY KEY (group_id)
);
CREATE UNIQUE INDEX iot_group_code_uk ON iot_group (group_code);
CREATE INDEX iot_group_parent_idx ON iot_group (parent_id);
COMMENT ON TABLE iot_group IS 'IoT 数据分组表(数据线-数据文件夹)';
COMMENT ON COLUMN iot_group.group_id IS '分组ID';
COMMENT ON COLUMN iot_group.group_code IS '分组编码';
COMMENT ON COLUMN iot_group.group_name IS '分组名称';
COMMENT ON COLUMN iot_group.parent_id IS '父分组ID(0为顶级)';
COMMENT ON COLUMN iot_group.order_num IS '显示顺序';
COMMENT ON COLUMN iot_group.status IS '状态(0正常 1停用)';
COMMENT ON COLUMN iot_group.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 2. 数据集表（数据线：数据资产）
--    挂在分组下、被应用引用、受角色数据权限约束
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_dataset;
CREATE TABLE iot_dataset
(
    dataset_id   BIGINT       NOT NULL,
    dataset_code VARCHAR(64)  NOT NULL,
    dataset_name VARCHAR(128) NOT NULL,
    group_id     BIGINT,
    dataset_type VARCHAR(32)           DEFAULT 'TABLE',
    source_table VARCHAR(128),
    description  VARCHAR(500),
    row_count    BIGINT                DEFAULT 0,
    status       CHAR(1)               DEFAULT '0',
    del_flag     CHAR(1)               DEFAULT '0',
    remark       VARCHAR(500)          DEFAULT '',
    create_dept  BIGINT,
    create_by    BIGINT,
    create_time  TIMESTAMP,
    update_by    BIGINT,
    update_time  TIMESTAMP,
    CONSTRAINT iot_dataset_pk PRIMARY KEY (dataset_id)
);
CREATE UNIQUE INDEX iot_dataset_code_uk ON iot_dataset (dataset_code);
CREATE INDEX iot_dataset_group_idx ON iot_dataset (group_id);
COMMENT ON TABLE iot_dataset IS 'IoT 数据集表(数据线-数据资产)';
COMMENT ON COLUMN iot_dataset.dataset_id IS '数据集ID';
COMMENT ON COLUMN iot_dataset.dataset_code IS '数据集编码';
COMMENT ON COLUMN iot_dataset.dataset_name IS '数据集名称';
COMMENT ON COLUMN iot_dataset.group_id IS '所属分组ID';
COMMENT ON COLUMN iot_dataset.dataset_type IS '数据集类型 TABLE/VIEW/API/STREAM';
COMMENT ON COLUMN iot_dataset.source_table IS '数据来源(物理表/视图/接口地址)';
COMMENT ON COLUMN iot_dataset.description IS '数据集描述';
COMMENT ON COLUMN iot_dataset.row_count IS '数据行数(概览)';
COMMENT ON COLUMN iot_dataset.status IS '状态(0正常 1停用)';
COMMENT ON COLUMN iot_dataset.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 3. 应用表（服务线：功能窗口，三线交汇点）
--    绑定数据集(引用非存储) + 授权角色
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_app;
CREATE TABLE iot_app
(
    app_id      BIGINT       NOT NULL,
    app_code    VARCHAR(64)  NOT NULL,
    app_name    VARCHAR(128) NOT NULL,
    app_type    VARCHAR(32)           DEFAULT 'WEB',
    access_url  VARCHAR(255),
    app_secret  VARCHAR(128),
    status      CHAR(1)               DEFAULT '0',
    del_flag    CHAR(1)               DEFAULT '0',
    remark      VARCHAR(500)          DEFAULT '',
    create_dept BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT iot_app_pk PRIMARY KEY (app_id)
);
CREATE UNIQUE INDEX iot_app_code_uk ON iot_app (app_code);
COMMENT ON TABLE iot_app IS 'IoT 应用表(服务线-功能窗口,三线交汇点)';
COMMENT ON COLUMN iot_app.app_id IS '应用ID';
COMMENT ON COLUMN iot_app.app_code IS '应用编码(SSO 接入唯一标识)';
COMMENT ON COLUMN iot_app.app_name IS '应用名称';
COMMENT ON COLUMN iot_app.app_type IS '应用类型 WEB/APP/MINI/API';
COMMENT ON COLUMN iot_app.access_url IS '访问地址';
COMMENT ON COLUMN iot_app.app_secret IS '应用密钥(SSO 票据校验)';
COMMENT ON COLUMN iot_app.status IS '状态(0正常 1停用)';
COMMENT ON COLUMN iot_app.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 4. 关联表：应用-数据集（引用关系，非存储）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_app_dataset;
CREATE TABLE iot_app_dataset
(
    app_id     BIGINT NOT NULL,
    dataset_id BIGINT NOT NULL,
    CONSTRAINT iot_app_dataset_pk PRIMARY KEY (app_id, dataset_id)
);
CREATE INDEX iot_app_dataset_dataset_idx ON iot_app_dataset (dataset_id);
COMMENT ON TABLE iot_app_dataset IS '应用-数据集引用关系表';

-- ----------------------------------------------------------------------------
-- 5. 关联表：应用-角色（授权关系）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_app_role;
CREATE TABLE iot_app_role
(
    app_id  BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT iot_app_role_pk PRIMARY KEY (app_id, role_id)
);
CREATE INDEX iot_app_role_role_idx ON iot_app_role (role_id);
COMMENT ON TABLE iot_app_role IS '应用-角色授权关系表';

-- ----------------------------------------------------------------------------
-- 6. 关联表：数据集-角色（数据权限，data_scope 复用 iot 数据范围字典）
--    data_scope: 1全部 2自定义 3本部门 4本部门及以下 5仅本人
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_dataset_role;
CREATE TABLE iot_dataset_role
(
    dataset_id BIGINT  NOT NULL,
    role_id    BIGINT  NOT NULL,
    data_scope CHAR(1) DEFAULT '1',
    CONSTRAINT iot_dataset_role_pk PRIMARY KEY (dataset_id, role_id)
);
CREATE INDEX iot_dataset_role_role_idx ON iot_dataset_role (role_id);
COMMENT ON TABLE iot_dataset_role IS '数据集-角色数据权限表';
COMMENT ON COLUMN iot_dataset_role.data_scope IS '数据范围(1全部 2自定义 3本部门 4本部门及以下 5仅本人)';

-- ----------------------------------------------------------------------------
-- 7. 演示数据
-- ----------------------------------------------------------------------------
INSERT INTO iot_group (group_id, group_code, group_name, parent_id, order_num, status, del_flag, remark, create_time, update_time)
VALUES (1960000000000000001, 'GRP-PRODUCTION', '生产数据', 0, 1, '0', '0', '产线生产相关数据', now(), now()),
       (1960000000000000002, 'GRP-ENV', '环境数据', 0, 2, '0', '0', '环境监测相关数据', now(), now());

INSERT INTO iot_dataset (dataset_id, dataset_code, dataset_name, group_id, dataset_type, source_table, description, row_count, status, del_flag, remark, create_time, update_time)
VALUES (1960000000000000101, 'DS-PRODUCT', '产品数据', 1960000000000000001, 'TABLE', 'iot_product', '产品主数据', 2, '0', '0', '', now(), now()),
       (1960000000000000102, 'DS-DEVICE', '设备数据', 1960000000000000001, 'TABLE', 'iot_device', '设备主数据', 3, '0', '0', '', now(), now()),
       (1960000000000000103, 'DS-ENV-TEMP', '温度采集数据', 1960000000000000002, 'STREAM', 'iot_device_data', '设备上报温度时序数据', 0, '0', '0', '', now(), now());

INSERT INTO iot_app (app_id, app_code, app_name, app_type, access_url, app_secret, status, del_flag, remark, create_time, update_time)
VALUES (1960000000000000201, 'APP-IOT-WEB', 'IoT 管理后台', 'WEB', 'http://localhost', 'iot-web-secret', '0', '0', '平台自带 Web 管理端', now(), now()),
       (1960000000000000202, 'APP-MOBILE', '移动端 App', 'APP', 'http://localhost:81', 'iot-app-secret', '0', '0', '移动端应用', now(), now());

INSERT INTO iot_app_dataset (app_id, dataset_id)
VALUES (1960000000000000201, 1960000000000000101),
       (1960000000000000201, 1960000000000000102),
       (1960000000000000202, 1960000000000000102)
ON CONFLICT DO NOTHING;

INSERT INTO iot_app_role (app_id, role_id)
VALUES (1960000000000000201, 1761300000000000001),
       (1960000000000000202, 1761300000000000001)
ON CONFLICT DO NOTHING;

INSERT INTO iot_dataset_role (dataset_id, role_id, data_scope)
VALUES (1960000000000000101, 1761300000000000001, '1'),
       (1960000000000000102, 1761300000000000001, '1'),
       (1960000000000000103, 1761300000000000001, '1')
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 8. 菜单与权限（挂在 iot_platform.sql 的一级目录 1900000000000000001 下）
--    菜单 ID 使用 1900000000000001001 起的新段，避免与 iot_platform.sql 的 001~999 段冲突
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000001001 AND 1900000000000001999;

-- 8.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000001001, '分组管理', 1900000000000000001, 8, 'group', 'group/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:group:list', 'tree', 103, 1, now(), 1, now(), '数据分组(数据线)'),
       (1900000000000001002, '数据集管理', 1900000000000000001, 9, 'dataset', 'dataset/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:dataset:list', 'table', 103, 1, now(), 1, now(), '数据集(数据线)'),
       (1900000000000001003, '应用管理', 1900000000000000001, 10, 'app', 'app/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:app:list', 'component', 103, 1, now(), 1, now(), '应用(服务线,三线交汇)');

-- 8.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000001101, '分组查询', 1900000000000001001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:group:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001102, '分组新增', 1900000000000001001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:group:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001103, '分组修改', 1900000000000001001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:group:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001104, '分组删除', 1900000000000001001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:group:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001105, '分组导出', 1900000000000001001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:group:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001111, '数据集查询', 1900000000000001002, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001112, '数据集新增', 1900000000000001002, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001113, '数据集修改', 1900000000000001002, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001114, '数据集删除', 1900000000000001002, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001115, '数据集导出', 1900000000000001002, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001116, '数据权限', 1900000000000001002, 6, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:dataset:auth', '#', 103, 1, now(), 1, now(), '数据集角色数据权限'),
       (1900000000000001121, '应用查询', 1900000000000001003, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001122, '应用新增', 1900000000000001003, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001123, '应用修改', 1900000000000001003, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001124, '应用删除', 1900000000000001003, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001125, '应用导出', 1900000000000001003, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000001126, '应用授权', 1900000000000001003, 6, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:app:auth', '#', 103, 1, now(), 1, now(), '应用-数据集/角色授权');

-- 8.3 授权给超管角色（superadmin 本身绕过鉴权，此处保持数据完整）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000001001 AND 1900000000000001999
ON CONFLICT DO NOTHING;
