-- ============================================================================
-- Snail-IoT - 任务调度（自建任务管理 + 执行器 SPI + SnailJob 分布式调度对接）
--
-- 使用前提：
--   1. 已导入 iot 基础脚本
--   2. 已导入 iot_platform.sql（含"Snail-IoT"一级目录 menu_id=1900000000000000001）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_job.sql
--
-- 说明：
--   - iot_job_info 保存可调度任务：job_type=LOCAL 由内置调度器（Cron/固定频率）触发，
--     job_type=SNAILJOB 为 SnailJob 服务端任务的本地视图；
--   - 执行器通过 JobHandler SPI 扩展（新增一个 @Component 即出现在前端下拉）；
--     SnailJob 侧执行器名固定为 iotJobDispatch，任务参数填本平台任务ID（JSON 或纯数字）；
--   - iot_job_log 记录每次执行的耗时与结果，由 IotJobInvoker 统一落库。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 任务信息表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_job_info;
CREATE TABLE iot_job_info
(
    job_id           BIGINT       NOT NULL,
    job_name         VARCHAR(128) NOT NULL,
    job_group        VARCHAR(64)           DEFAULT 'DEFAULT',
    job_type         VARCHAR(32)  NOT NULL DEFAULT 'LOCAL',
    handler_name     VARCHAR(128) NOT NULL,
    cron_expr        VARCHAR(128),
    trigger_type     VARCHAR(32)  NOT NULL DEFAULT 'CRON',
    interval_seconds INT,
    job_params       TEXT,
    snail_job_id     BIGINT,
    status           CHAR(1)               DEFAULT '0',
    last_fire_time   TIMESTAMP,
    next_fire_time   TIMESTAMP,
    del_flag         CHAR(1)               DEFAULT '0',
    remark           VARCHAR(500)          DEFAULT '',
    create_dept      BIGINT,
    create_by        BIGINT,
    create_time      TIMESTAMP,
    update_by        BIGINT,
    update_time      TIMESTAMP,
    PRIMARY KEY (job_id)
);
CREATE UNIQUE INDEX uk_iot_job_info_name ON iot_job_info (job_name);
CREATE INDEX idx_iot_job_info_status ON iot_job_info (status);
CREATE INDEX idx_iot_job_info_type ON iot_job_info (job_type);
COMMENT ON TABLE iot_job_info IS '任务信息表';
COMMENT ON COLUMN iot_job_info.job_id           IS '任务ID';
COMMENT ON COLUMN iot_job_info.job_name         IS '任务名称（唯一）';
COMMENT ON COLUMN iot_job_info.job_group        IS '任务分组';
COMMENT ON COLUMN iot_job_info.job_type         IS '任务类型（LOCAL内置调度/SNAILJOB分布式调度）';
COMMENT ON COLUMN iot_job_info.handler_name     IS '执行器名称（JobHandler#name）';
COMMENT ON COLUMN iot_job_info.cron_expr        IS 'Cron 表达式（trigger_type=CRON 时生效，6 段）';
COMMENT ON COLUMN iot_job_info.trigger_type     IS '触发方式（CRON/FIXED_RATE/MANUAL）';
COMMENT ON COLUMN iot_job_info.interval_seconds IS '固定频率间隔（秒，trigger_type=FIXED_RATE 时生效）';
COMMENT ON COLUMN iot_job_info.job_params       IS '任务参数（JSON 文本，透传给执行器）';
COMMENT ON COLUMN iot_job_info.snail_job_id     IS 'SnailJob 任务ID（job_type=SNAILJOB 时登记）';
COMMENT ON COLUMN iot_job_info.status           IS '状态（0启用 1停用）';
COMMENT ON COLUMN iot_job_info.last_fire_time   IS '最近触发时间';
COMMENT ON COLUMN iot_job_info.next_fire_time   IS '下次触发时间';
COMMENT ON COLUMN iot_job_info.del_flag         IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_job_info.remark           IS '备注';
COMMENT ON COLUMN iot_job_info.create_dept      IS '创建部门';
COMMENT ON COLUMN iot_job_info.create_by        IS '创建者';
COMMENT ON COLUMN iot_job_info.create_time      IS '创建时间';
COMMENT ON COLUMN iot_job_info.update_by        IS '更新者';
COMMENT ON COLUMN iot_job_info.update_time      IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 任务执行日志表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_job_log;
CREATE TABLE iot_job_log
(
    log_id       BIGINT NOT NULL,
    job_id       BIGINT,
    job_name     VARCHAR(128),
    trigger_type VARCHAR(32),
    handler_name VARCHAR(128),
    status       VARCHAR(32),
    start_time   TIMESTAMP,
    end_time     TIMESTAMP,
    duration     BIGINT,
    result       TEXT,
    message      TEXT,
    create_dept  BIGINT,
    create_by    BIGINT,
    create_time  TIMESTAMP,
    update_by    BIGINT,
    update_time  TIMESTAMP,
    PRIMARY KEY (log_id)
);
CREATE INDEX idx_iot_job_log_job_id ON iot_job_log (job_id);
CREATE INDEX idx_iot_job_log_status ON iot_job_log (status);
CREATE INDEX idx_iot_job_log_start_time ON iot_job_log (start_time);
COMMENT ON TABLE iot_job_log IS '任务执行日志表';
COMMENT ON COLUMN iot_job_log.log_id       IS '日志ID';
COMMENT ON COLUMN iot_job_log.job_id       IS '任务ID';
COMMENT ON COLUMN iot_job_log.job_name     IS '任务名称快照';
COMMENT ON COLUMN iot_job_log.trigger_type IS '触发方式（CRON/FIXED_RATE/MANUAL/SNAILJOB）';
COMMENT ON COLUMN iot_job_log.handler_name IS '执行器名称';
COMMENT ON COLUMN iot_job_log.status       IS '执行状态（SUCCESS/FAIL）';
COMMENT ON COLUMN iot_job_log.start_time   IS '开始时间';
COMMENT ON COLUMN iot_job_log.end_time     IS '结束时间';
COMMENT ON COLUMN iot_job_log.duration     IS '耗时（毫秒）';
COMMENT ON COLUMN iot_job_log.result       IS '执行结果';
COMMENT ON COLUMN iot_job_log.message      IS '异常信息';
COMMENT ON COLUMN iot_job_log.create_dept  IS '创建部门';
COMMENT ON COLUMN iot_job_log.create_by    IS '创建者';
COMMENT ON COLUMN iot_job_log.create_time  IS '创建时间';
COMMENT ON COLUMN iot_job_log.update_by    IS '更新者';
COMMENT ON COLUMN iot_job_log.update_time  IS '更新时间';

-- ----------------------------------------------------------------------------
-- 3. 菜单与权限（挂在"Snail-IoT"一级目录 1900000000000000001 下）
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000007001 AND 1900000000000007999;

-- 3.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000007001, '任务调度', 1900000000000000001, 15, 'job', 'job/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:job:info:list', 'time', 103, 1, now(), 1, now(), '任务调度：自建任务 + SnailJob 分布式调度');

-- 3.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000007002, '任务查询', 1900000000000007001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000007003, '任务新增', 1900000000000007001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000007004, '任务修改', 1900000000000007001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000007005, '任务删除', 1900000000000007001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000007006, '任务启停', 1900000000000007001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:changeStatus', '#', 103, 1, now(), 1, now(), '启用/停用任务'),
       (1900000000000007007, '手动触发', 1900000000000007001, 6, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:info:trigger', '#', 103, 1, now(), 1, now(), '立即执行一次任务'),
       (1900000000000007008, '日志查询', 1900000000000007001, 7, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:log:list', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000007009, '日志删除', 1900000000000007001, 8, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:job:log:remove', '#', 103, 1, now(), 1, now(), '');

-- 3.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000007001 AND 1900000000000007999
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. 种子数据：内置示例任务（开箱即用）
-- ----------------------------------------------------------------------------
DELETE FROM iot_job_info WHERE job_id BETWEEN 1900000000000007101 AND 1900000000000007199;

INSERT INTO iot_job_info (job_id, job_name, job_group, job_type, handler_name, cron_expr, trigger_type, interval_seconds,
                          job_params, status, del_flag, remark,
                          create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000007101, '系统心跳检测', 'DEFAULT', 'LOCAL', 'heartbeatJob', '0 0/5 * * * ?', 'CRON', NULL,
        '{}', '0', '0', '内置示例：每 5 分钟输出一次系统心跳时间',
        103, 1, now(), 1, now()),
       (1900000000000007102, '历史任务日志清理', 'DEFAULT', 'LOCAL', 'cleanJobLog', '0 0 3 * * ?', 'CRON', NULL,
        '{"days":7}', '1', '0', '内置示例：每天 03:00 清理 7 天前的执行日志（默认停用）',
        103, 1, now(), 1, now()),
       (1900000000000007103, '系统负载上报', 'DEFAULT', 'LOCAL', 'systemLoadReport', '0 0/10 * * * ?', 'CRON', NULL,
        '{}', '0', '0', '内置示例：每 10 分钟采集一次 JVM 内存与运行时长',
        103, 1, now(), 1, now()),
       (1900000000000007104, 'Redis 服务巡检', 'DEFAULT', 'LOCAL', 'redisHealthCheck', NULL, 'FIXED_RATE', 300,
        '{"key":"iot:job:redis-health"}', '0', '0', '内置示例：每 300 秒校验一次 Redis 探针键读写',
        103, 1, now(), 1, now()),
       (1900000000000007105, '任务日志统计', 'DEFAULT', 'LOCAL', 'jobLogStat', '0 0 8 * * ?', 'CRON', NULL,
        '{"days":1}', '0', '0', '内置示例：每天 08:00 统计近 1 天任务执行成功/失败条数',
        103, 1, now(), 1, now());
