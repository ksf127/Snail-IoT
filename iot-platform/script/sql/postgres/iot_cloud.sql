-- ============================================================================
-- Snail-IoT - 云平台纳管（阿里云 ECS / Flink 作业）
--
-- 使用前提：
--   1. 已导入 iot 基础脚本
--   2. 已导入 iot_platform.sql（含"Snail-IoT"一级目录 menu_id=1900000000000000001）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_cloud.sql
--
-- 说明：
--   - 云平台采用"纳管抽象层"设计：ICloudProvider SPI + CloudProviderFactory 路由；
--     内置三种实现：mock（离线模拟）、aliyun（阿里云 ECS OpenAPI）、flink（JobManager REST）；
--   - 云账号 iot_cloud_account 保存接入凭证；云资源 iot_cloud_resource 为同步后的资源清单；
--     iot_flink_job 为 Flink 作业明细视图。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 云账号表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_cloud_account;
CREATE TABLE iot_cloud_account
(
    account_id    BIGINT       NOT NULL,
    account_name  VARCHAR(128) NOT NULL,
    provider      VARCHAR(32)  NOT NULL,
    region_id     VARCHAR(64),
    access_key    VARCHAR(255),
    access_secret VARCHAR(512),
    endpoint      VARCHAR(255),
    status        CHAR(1)               DEFAULT '0',
    del_flag      CHAR(1)               DEFAULT '0',
    remark        VARCHAR(500)          DEFAULT '',
    create_dept   BIGINT,
    create_by     BIGINT,
    create_time   TIMESTAMP,
    update_by     BIGINT,
    update_time   TIMESTAMP,
    PRIMARY KEY (account_id)
);
CREATE UNIQUE INDEX uk_iot_cloud_account_name ON iot_cloud_account (account_name);
CREATE INDEX idx_iot_cloud_account_provider ON iot_cloud_account (provider);
COMMENT ON TABLE iot_cloud_account IS '云账号表（云平台接入凭证）';
COMMENT ON COLUMN iot_cloud_account.account_id    IS '云账号ID';
COMMENT ON COLUMN iot_cloud_account.account_name  IS '账号名称（唯一）';
COMMENT ON COLUMN iot_cloud_account.provider      IS '云平台（mock内置模拟/aliyun阿里云/flink）';
COMMENT ON COLUMN iot_cloud_account.region_id     IS '默认地域ID';
COMMENT ON COLUMN iot_cloud_account.access_key    IS '访问密钥ID（阿里云 AccessKeyId）';
COMMENT ON COLUMN iot_cloud_account.access_secret IS '访问密钥（阿里云 AccessKeySecret，Flink 可留空）';
COMMENT ON COLUMN iot_cloud_account.endpoint      IS '接入端点（Flink JobManager 地址）';
COMMENT ON COLUMN iot_cloud_account.status        IS '状态（0启用 1停用）';
COMMENT ON COLUMN iot_cloud_account.del_flag      IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_cloud_account.remark        IS '备注';
COMMENT ON COLUMN iot_cloud_account.create_dept   IS '创建部门';
COMMENT ON COLUMN iot_cloud_account.create_by     IS '创建者';
COMMENT ON COLUMN iot_cloud_account.create_time   IS '创建时间';
COMMENT ON COLUMN iot_cloud_account.update_by     IS '更新者';
COMMENT ON COLUMN iot_cloud_account.update_time   IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 云资源表（同步后的纳管资源清单）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_cloud_resource;
CREATE TABLE iot_cloud_resource
(
    resource_id    BIGINT       NOT NULL,
    account_id     BIGINT       NOT NULL,
    provider       VARCHAR(32),
    resource_type  VARCHAR(32),
    instance_id    VARCHAR(128),
    instance_name  VARCHAR(255),
    region_id      VARCHAR(64),
    status         VARCHAR(64),
    spec           VARCHAR(128),
    private_ip     VARCHAR(128),
    public_ip      VARCHAR(128),
    biz_meta       TEXT,
    last_sync_time TIMESTAMP,
    del_flag       CHAR(1)               DEFAULT '0',
    remark         VARCHAR(500)          DEFAULT '',
    create_dept    BIGINT,
    create_by      BIGINT,
    create_time    TIMESTAMP,
    update_by      BIGINT,
    update_time    TIMESTAMP,
    PRIMARY KEY (resource_id)
);
CREATE UNIQUE INDEX uk_iot_cloud_resource_instance ON iot_cloud_resource (account_id, instance_id);
CREATE INDEX idx_iot_cloud_resource_type ON iot_cloud_resource (resource_type);
CREATE INDEX idx_iot_cloud_resource_status ON iot_cloud_resource (status);
COMMENT ON TABLE iot_cloud_resource IS '云资源表（纳管资源清单）';
COMMENT ON COLUMN iot_cloud_resource.resource_id    IS '资源ID';
COMMENT ON COLUMN iot_cloud_resource.account_id     IS '云账号ID';
COMMENT ON COLUMN iot_cloud_resource.provider       IS '云平台（mock/aliyun/flink）';
COMMENT ON COLUMN iot_cloud_resource.resource_type  IS '资源类型（ecs/flink-job）';
COMMENT ON COLUMN iot_cloud_resource.instance_id    IS '云平台实例ID';
COMMENT ON COLUMN iot_cloud_resource.instance_name  IS '实例名称';
COMMENT ON COLUMN iot_cloud_resource.region_id      IS '地域ID';
COMMENT ON COLUMN iot_cloud_resource.status         IS '状态（原样透出云平台状态）';
COMMENT ON COLUMN iot_cloud_resource.spec           IS '规格（ECS InstanceType / Flink 并行度）';
COMMENT ON COLUMN iot_cloud_resource.private_ip     IS '内网IP';
COMMENT ON COLUMN iot_cloud_resource.public_ip      IS '公网IP';
COMMENT ON COLUMN iot_cloud_resource.biz_meta       IS '业务扩展信息（JSON 文本）';
COMMENT ON COLUMN iot_cloud_resource.last_sync_time IS '最近同步时间';
COMMENT ON COLUMN iot_cloud_resource.del_flag       IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_cloud_resource.remark         IS '备注';
COMMENT ON COLUMN iot_cloud_resource.create_dept    IS '创建部门';
COMMENT ON COLUMN iot_cloud_resource.create_by      IS '创建者';
COMMENT ON COLUMN iot_cloud_resource.create_time    IS '创建时间';
COMMENT ON COLUMN iot_cloud_resource.update_by      IS '更新者';
COMMENT ON COLUMN iot_cloud_resource.update_time    IS '更新时间';

-- ----------------------------------------------------------------------------
-- 3. Flink 作业表（JobManager REST 同步的作业明细视图）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_flink_job;
CREATE TABLE iot_flink_job
(
    job_id         BIGINT       NOT NULL,
    account_id     BIGINT       NOT NULL,
    job_name       VARCHAR(255),
    flink_job_id   VARCHAR(128),
    status         VARCHAR(64),
    parallelism    INT,
    start_time     BIGINT,
    duration       BIGINT,
    last_sync_time TIMESTAMP,
    del_flag       CHAR(1)               DEFAULT '0',
    remark         VARCHAR(500)          DEFAULT '',
    create_dept    BIGINT,
    create_by      BIGINT,
    create_time    TIMESTAMP,
    update_by      BIGINT,
    update_time    TIMESTAMP,
    PRIMARY KEY (job_id)
);
CREATE UNIQUE INDEX uk_iot_flink_job_flink_id ON iot_flink_job (account_id, flink_job_id);
CREATE INDEX idx_iot_flink_job_status ON iot_flink_job (status);
COMMENT ON TABLE iot_flink_job IS 'Flink 作业表';
COMMENT ON COLUMN iot_flink_job.job_id         IS '作业ID';
COMMENT ON COLUMN iot_flink_job.account_id     IS '云账号ID';
COMMENT ON COLUMN iot_flink_job.job_name       IS '作业名称';
COMMENT ON COLUMN iot_flink_job.flink_job_id   IS 'Flink 作业ID（JobManager 返回的 jid）';
COMMENT ON COLUMN iot_flink_job.status         IS '作业状态（RUNNING/FINISHED/FAILED/CANCELED 等）';
COMMENT ON COLUMN iot_flink_job.parallelism    IS '并行度';
COMMENT ON COLUMN iot_flink_job.start_time     IS '启动时间（毫秒时间戳）';
COMMENT ON COLUMN iot_flink_job.duration       IS '运行时长（毫秒）';
COMMENT ON COLUMN iot_flink_job.last_sync_time IS '最近同步时间';
COMMENT ON COLUMN iot_flink_job.del_flag       IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_flink_job.remark         IS '备注';
COMMENT ON COLUMN iot_flink_job.create_dept    IS '创建部门';
COMMENT ON COLUMN iot_flink_job.create_by      IS '创建者';
COMMENT ON COLUMN iot_flink_job.create_time    IS '创建时间';
COMMENT ON COLUMN iot_flink_job.update_by      IS '更新者';
COMMENT ON COLUMN iot_flink_job.update_time    IS '更新时间';

-- ----------------------------------------------------------------------------
-- 4. 菜单与权限（挂在"Snail-IoT"一级目录 1900000000000000001 下）
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000008001 AND 1900000000000008999;

-- 4.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000008001, '云平台', 1900000000000000001, 14, 'cloud', 'cloud/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:cloud:account:list', 'cloud', 103, 1, now(), 1, now(), '云平台纳管：云账号/云资源/Flink作业');

-- 4.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000008002, '账号查询', 1900000000000008001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:account:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008003, '账号新增', 1900000000000008001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:account:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008004, '账号修改', 1900000000000008001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:account:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008005, '账号删除', 1900000000000008001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:account:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008006, '连接测试', 1900000000000008001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:account:test', '#', 103, 1, now(), 1, now(), '测试云账号连通性'),
       (1900000000000008007, '资源查询', 1900000000000008001, 6, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:list', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008008, '资源详情', 1900000000000008001, 7, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008009, '资源同步', 1900000000000008001, 8, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:sync', '#', 103, 1, now(), 1, now(), '从云平台同步资源清单'),
       (1900000000000008010, '资源创建', 1900000000000008001, 9, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008011, '资源操作', 1900000000000008001, 10, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:operate', '#', 103, 1, now(), 1, now(), '启动/停止/释放'),
       (1900000000000008012, '资源删除', 1900000000000008001, 11, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:resource:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008013, '作业查询', 1900000000000008001, 12, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:flink:list', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008014, '作业详情', 1900000000000008001, 13, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:flink:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000008015, '作业同步', 1900000000000008001, 14, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:flink:sync', '#', 103, 1, now(), 1, now(), '从 Flink JobManager 同步作业'),
       (1900000000000008016, '作业操作', 1900000000000008001, 15, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:cloud:flink:operate', '#', 103, 1, now(), 1, now(), '停止/取消作业');

-- 4.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000008001 AND 1900000000000008999
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. 种子数据：一个内置模拟云账号（开箱即用，无需任何密钥）
-- ----------------------------------------------------------------------------
DELETE FROM iot_cloud_account WHERE account_id BETWEEN 1900000000000008101 AND 1900000000000008109;

INSERT INTO iot_cloud_account (account_id, account_name, provider, region_id, access_key, access_secret, endpoint,
                               status, del_flag, remark,
                               create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000008101, '内置模拟云账号', 'mock', 'cn-hangzhou', '', '', '', '0', '0',
        '开箱即用的内置模拟云平台，无需真实密钥，可直接体验资源同步与操作',
        103, 1, now(), 1, now());
