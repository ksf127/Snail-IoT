-- ============================================================================
-- Snail-IoT - PostgreSQL 业务表与菜单初始化脚本
--
-- 使用前提：
--   1. 已导入 iot-vue-plus 基础脚本：
--        script/sql/postgres/postgres_iot_vue.sql  (系统表：用户/角色/菜单/日志等)
--        script/sql/postgres/postgres_iot_job.sql   (调度任务表，可选)
--   2. 数据库：ruoyi  用户名/密码：postgres/postgres
--
-- 执行方式：
--   psql -U postgres -d ruoyi -f iot_platform.sql
--   或 docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_platform.sql
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 产品表：设备模板，承载接入协议与物模型定义
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_product;
CREATE TABLE iot_product
(
    product_id    BIGINT       NOT NULL,
    product_code  VARCHAR(64)  NOT NULL,
    product_name  VARCHAR(128) NOT NULL,
    protocol      VARCHAR(32)           DEFAULT 'MQTT',
    device_type   VARCHAR(64),
    data_format   VARCHAR(32)           DEFAULT 'JSON',
    thing_model   TEXT,
    status        CHAR(1)               DEFAULT '0',
    del_flag      CHAR(1)               DEFAULT '0',
    remark        VARCHAR(500)          DEFAULT '',
    create_dept   BIGINT,
    create_by     BIGINT,
    create_time   TIMESTAMP,
    update_by     BIGINT,
    update_time   TIMESTAMP,
    CONSTRAINT iot_product_pk PRIMARY KEY (product_id)
);
CREATE UNIQUE INDEX iot_product_code_uk ON iot_product (product_code);
COMMENT ON TABLE iot_product IS 'IoT 产品表';
COMMENT ON COLUMN iot_product.product_id IS '产品ID';
COMMENT ON COLUMN iot_product.product_code IS '产品编码';
COMMENT ON COLUMN iot_product.product_name IS '产品名称';
COMMENT ON COLUMN iot_product.protocol IS '接入协议 MQTT/HTTPS/MODBUS/OTHER';
COMMENT ON COLUMN iot_product.device_type IS '设备类型';
COMMENT ON COLUMN iot_product.data_format IS '数据格式 JSON/PASS_THROUGH';
COMMENT ON COLUMN iot_product.thing_model IS '物模型定义(JSON)';
COMMENT ON COLUMN iot_product.status IS '状态(0正常 1停用)';
COMMENT ON COLUMN iot_product.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 2. 设备表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_device;
CREATE TABLE iot_device
(
    device_id        BIGINT       NOT NULL,
    product_id       BIGINT,
    device_code      VARCHAR(64)  NOT NULL,
    device_name      VARCHAR(128) NOT NULL,
    protocol         VARCHAR(32)           DEFAULT 'MQTT',
    device_secret    VARCHAR(128),
    firmware_version VARCHAR(64),
    online_status    CHAR(1)               DEFAULT '0',
    status           CHAR(1)               DEFAULT '0',
    last_online_time TIMESTAMP,
    last_report_time TIMESTAMP,
    ip_address       VARCHAR(64),
    location         VARCHAR(255),
    tags             VARCHAR(500),
    del_flag         CHAR(1)               DEFAULT '0',
    remark           VARCHAR(500)          DEFAULT '',
    create_dept      BIGINT,
    create_by        BIGINT,
    create_time      TIMESTAMP,
    update_by        BIGINT,
    update_time      TIMESTAMP,
    CONSTRAINT iot_device_pk PRIMARY KEY (device_id)
);
CREATE UNIQUE INDEX iot_device_code_uk ON iot_device (device_code);
CREATE INDEX iot_device_product_idx ON iot_device (product_id);
CREATE INDEX iot_device_online_idx ON iot_device (online_status);
COMMENT ON TABLE iot_device IS 'IoT 设备表';
COMMENT ON COLUMN iot_device.device_id IS '设备ID';
COMMENT ON COLUMN iot_device.product_id IS '所属产品ID';
COMMENT ON COLUMN iot_device.device_code IS '设备编码(全局唯一,用于上报鉴权)';
COMMENT ON COLUMN iot_device.device_name IS '设备名称';
COMMENT ON COLUMN iot_device.protocol IS '接入协议 MQTT/HTTPS/MODBUS/OTHER';
COMMENT ON COLUMN iot_device.device_secret IS '设备密钥';
COMMENT ON COLUMN iot_device.firmware_version IS '固件版本';
COMMENT ON COLUMN iot_device.online_status IS '在线状态(0离线 1在线 2未知)';
COMMENT ON COLUMN iot_device.status IS '设备使能状态(0正常 1停用)';
COMMENT ON COLUMN iot_device.last_online_time IS '最近上线时间';
COMMENT ON COLUMN iot_device.last_report_time IS '最近数据上报时间';
COMMENT ON COLUMN iot_device.ip_address IS '设备IP';
COMMENT ON COLUMN iot_device.location IS '安装位置';
COMMENT ON COLUMN iot_device.tags IS '标签(JSON数组)';
COMMENT ON COLUMN iot_device.del_flag IS '删除标志(0存在 1删除)';

-- ----------------------------------------------------------------------------
-- 3. 演示数据
-- ----------------------------------------------------------------------------
INSERT INTO iot_product (product_id, product_code, product_name, protocol, device_type, data_format, status, del_flag, remark, create_time, update_time)
VALUES (1950000000000000001, 'P-SENSOR-TEMP', '温度传感器', 'MQTT', 'SENSOR', 'JSON', '0', '0', '采集环境温度', now(), now()),
       (1950000000000000002, 'P-GATEWAY-PLC', 'PLC 网关', 'HTTPS', 'GATEWAY', 'PASS_THROUGH', '0', '0', '工业网关 HTTPS 上报', now(), now());

INSERT INTO iot_device (device_id, product_id, device_code, device_name, protocol, device_secret, firmware_version,
                        online_status, status, last_online_time, last_report_time, ip_address, location, tags,
                        del_flag, remark, create_time, update_time)
VALUES (1950000000000000101, 1950000000000000001, 'DEV-TEMP-0001', '一号车间温度计', 'MQTT', 'secret-temp-0001', 'v1.0.0',
        '1', '0', now(), now(), '192.168.1.101', '一号车间', '["车间","温度"]', '0', '', now(), now()),
       (1950000000000000102, 1950000000000000001, 'DEV-TEMP-0002', '二号车间温度计', 'MQTT', 'secret-temp-0002', 'v1.0.0',
        '0', '0', now() - interval '2 hour', now() - interval '2 hour', '192.168.1.102', '二号车间', '["车间","温度"]', '0', '', now(), now()),
       (1950000000000000103, 1950000000000000002, 'DEV-PLC-0001', '产线 PLC 网关', 'HTTPS', 'secret-plc-0001', 'v2.3.1',
        '1', '0', now(), now(), '192.168.1.200', '产线A', '["产线","网关"]', '0', '', now(), now());

-- ----------------------------------------------------------------------------
-- 4. 菜单与权限
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000000001 AND 1900000000000000999;

-- 4.1 一级目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000000001, 'Snail-IoT', 0, 0, 'iot', NULL, '', 'N', 'Y',
        'M', '0', '0', '', 'iot', 103, 1, now(), 1, now(), 'Snail-IoT 目录');

-- 4.2 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000000002, '产品管理', 1900000000000000001, 1, 'product', 'product/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:product:list', 'product', 103, 1, now(), 1, now(), '产品列表'),
       (1900000000000000003, '设备管理', 1900000000000000001, 2, 'device', 'device/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:device:list', 'device', 103, 1, now(), 1, now(), '设备列表'),
       (1900000000000000004, '设备监控', 1900000000000000001, 3, 'deviceMonitor', 'monitor/device', '', 'N', 'Y',
        'C', '0', '0', 'iot:monitor:device', 'monitor', 103, 1, now(), 1, now(), '设备实时监控'),
       (1900000000000000005, '服务器监控', 1900000000000000001, 4, 'serverMonitor', 'monitor/server', '', 'N', 'Y',
        'C', '0', '0', 'iot:monitor:server', 'server', 103, 1, now(), 1, now(), '服务器运行监控'),
       (1900000000000000006, '档案管理', 1900000000000000001, 5, 'archive', 'archive/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:archive:list', 'archive', 103, 1, now(), 1, now(), '档案管理'),
       (1900000000000000007, '报表中心', 1900000000000000001, 6, 'report', 'report/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:report:list', 'report', 103, 1, now(), 1, now(), '报表与批量上传下载'),
       (1900000000000000008, '自动化部署', 1900000000000000001, 7, 'deploy', 'deploy/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:deploy:list', 'deploy', 103, 1, now(), 1, now(), '自动化部署');

-- 4.3 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000000101, '产品查询', 1900000000000000002, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:product:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000102, '产品新增', 1900000000000000002, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:product:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000103, '产品修改', 1900000000000000002, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:product:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000104, '产品删除', 1900000000000000002, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:product:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000105, '产品导出', 1900000000000000002, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:product:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000111, '设备查询', 1900000000000000003, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:device:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000112, '设备新增', 1900000000000000003, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:device:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000113, '设备修改', 1900000000000000003, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:device:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000114, '设备删除', 1900000000000000003, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:device:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000115, '设备导出', 1900000000000000003, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:device:export', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000121, '档案新增', 1900000000000000006, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:archive:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000131, '报表上传', 1900000000000000007, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:report:upload', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000132, '报表下载', 1900000000000000007, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:report:download', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000141, '应用发布', 1900000000000000008, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:deploy:release', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000000142, '发布查询', 1900000000000000008, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:deploy:query', '#', 103, 1, now(), 1, now(), '');

-- 4.4 授权给超管角色（superadmin 本身绕过鉴权，此处保持数据完整）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000000001 AND 1900000000000000999
ON CONFLICT DO NOTHING;
