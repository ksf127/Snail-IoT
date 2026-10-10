-- ============================================================================
-- Snail-IoT - 聚合支付（阶段四）
--
-- 使用前提：
--   1. 已导入 iot 基础脚本
--   2. 已导入 iot_platform.sql（含"Snail-IoT"一级目录 menu_id=1900000000000000001）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_pay.sql
--
-- 说明：
--   - 支付渠道配置采用"表驱动"（与 sys_oss_config 同范式），支持多商户/多渠道热切换；
--   - 默认种子一条 mock 渠道，用于本地无真实商户号时自洽验证。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 支付订单表
--    状态机：0待支付 -> 1已支付 -> 3已退款；0待支付 -> 2已关闭（超时/取消）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_pay_order;
CREATE TABLE iot_pay_order
(
    order_id     BIGINT        NOT NULL,
    order_no     VARCHAR(64)   NOT NULL,
    biz_order_no VARCHAR(64),
    channel      VARCHAR(32)   NOT NULL,
    subject      VARCHAR(255),
    amount       NUMERIC(12, 2) NOT NULL,
    currency     VARCHAR(8)             DEFAULT 'CNY',
    status       CHAR(1)                DEFAULT '0',
    user_id      BIGINT,
    pre_pay_id   VARCHAR(255),
    code_url     VARCHAR(512),
    pay_time     TIMESTAMP,
    expire_time  TIMESTAMP,
    notify_data  TEXT,
    del_flag     CHAR(1)                DEFAULT '0',
    remark       VARCHAR(500)           DEFAULT '',
    create_dept  BIGINT,
    create_by    BIGINT,
    create_time  TIMESTAMP,
    update_by    BIGINT,
    update_time  TIMESTAMP,
    PRIMARY KEY (order_id)
);
CREATE UNIQUE INDEX uk_iot_pay_order_no ON iot_pay_order (order_no);
COMMENT ON TABLE iot_pay_order IS '支付订单表';
COMMENT ON COLUMN iot_pay_order.order_id     IS '支付单ID';
COMMENT ON COLUMN iot_pay_order.order_no     IS '平台支付单号';
COMMENT ON COLUMN iot_pay_order.biz_order_no IS '业务订单号';
COMMENT ON COLUMN iot_pay_order.channel      IS '支付渠道（mock/wx_native/ali_qr）';
COMMENT ON COLUMN iot_pay_order.subject      IS '商品标题';
COMMENT ON COLUMN iot_pay_order.amount        IS '支付金额（元）';
COMMENT ON COLUMN iot_pay_order.currency      IS '币种';
COMMENT ON COLUMN iot_pay_order.status       IS '状态（0待支付 1已支付 2已关闭 3已退款）';
COMMENT ON COLUMN iot_pay_order.user_id      IS '支付用户ID';
COMMENT ON COLUMN iot_pay_order.pre_pay_id   IS '渠道预支付ID';
COMMENT ON COLUMN iot_pay_order.code_url     IS '二维码链接';
COMMENT ON COLUMN iot_pay_order.pay_time     IS '支付完成时间';
COMMENT ON COLUMN iot_pay_order.expire_time  IS '订单过期时间';
COMMENT ON COLUMN iot_pay_order.notify_data  IS '支付回调原文';
COMMENT ON COLUMN iot_pay_order.del_flag     IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_pay_order.remark       IS '备注';
COMMENT ON COLUMN iot_pay_order.create_dept  IS '创建部门';
COMMENT ON COLUMN iot_pay_order.create_by    IS '创建者';
COMMENT ON COLUMN iot_pay_order.create_time  IS '创建时间';
COMMENT ON COLUMN iot_pay_order.update_by    IS '更新者';
COMMENT ON COLUMN iot_pay_order.update_time  IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 退款单表
--    状态：0退款中 1退款成功 2退款失败
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_pay_refund;
CREATE TABLE iot_pay_refund
(
    refund_id     BIGINT        NOT NULL,
    refund_no     VARCHAR(64)   NOT NULL,
    order_no      VARCHAR(64)   NOT NULL,
    refund_amount NUMERIC(12, 2) NOT NULL,
    reason        VARCHAR(255),
    status        CHAR(1)                DEFAULT '0',
    refund_time   TIMESTAMP,
    notify_data   TEXT,
    del_flag      CHAR(1)                DEFAULT '0',
    remark        VARCHAR(500)           DEFAULT '',
    create_dept   BIGINT,
    create_by     BIGINT,
    create_time   TIMESTAMP,
    update_by     BIGINT,
    update_time   TIMESTAMP,
    PRIMARY KEY (refund_id)
);
CREATE UNIQUE INDEX uk_iot_pay_refund_no ON iot_pay_refund (refund_no);
COMMENT ON TABLE iot_pay_refund IS '支付退款单表';
COMMENT ON COLUMN iot_pay_refund.refund_id     IS '退款单ID';
COMMENT ON COLUMN iot_pay_refund.refund_no     IS '平台退款单号';
COMMENT ON COLUMN iot_pay_refund.order_no      IS '关联支付单号';
COMMENT ON COLUMN iot_pay_refund.refund_amount IS '退款金额（元）';
COMMENT ON COLUMN iot_pay_refund.reason        IS '退款原因';
COMMENT ON COLUMN iot_pay_refund.status        IS '状态（0退款中 1退款成功 2退款失败）';
COMMENT ON COLUMN iot_pay_refund.refund_time   IS '退款完成时间';
COMMENT ON COLUMN iot_pay_refund.notify_data   IS '退款回调原文';
COMMENT ON COLUMN iot_pay_refund.del_flag      IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_pay_refund.remark        IS '备注';
COMMENT ON COLUMN iot_pay_refund.create_dept   IS '创建部门';
COMMENT ON COLUMN iot_pay_refund.create_by     IS '创建者';
COMMENT ON COLUMN iot_pay_refund.create_time   IS '创建时间';
COMMENT ON COLUMN iot_pay_refund.update_by     IS '更新者';
COMMENT ON COLUMN iot_pay_refund.update_time   IS '更新时间';

-- ----------------------------------------------------------------------------
-- 3. 渠道配置表（DB 驱动，多商户/多渠道）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_pay_channel_config;
CREATE TABLE iot_pay_channel_config
(
    config_id   BIGINT       NOT NULL,
    config_key  VARCHAR(64)  NOT NULL,
    config_name VARCHAR(128),
    channel     VARCHAR(32)  NOT NULL,
    merchant_id VARCHAR(128),
    app_id      VARCHAR(128),
    private_key TEXT,
    public_key  TEXT,
    api_key     TEXT,
    cert_path   VARCHAR(512),
    notify_url  VARCHAR(512),
    status      CHAR(1)               DEFAULT '0',
    del_flag    CHAR(1)               DEFAULT '0',
    remark      VARCHAR(500)          DEFAULT '',
    create_dept BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP,
    update_by   BIGINT,
    update_time TIMESTAMP,
    PRIMARY KEY (config_id)
);
CREATE UNIQUE INDEX uk_iot_pay_channel_config_key ON iot_pay_channel_config (config_key);
COMMENT ON TABLE iot_pay_channel_config IS '支付渠道配置表';
COMMENT ON COLUMN iot_pay_channel_config.config_id   IS '配置ID';
COMMENT ON COLUMN iot_pay_channel_config.config_key  IS '配置键（唯一）';
COMMENT ON COLUMN iot_pay_channel_config.config_name IS '配置名称';
COMMENT ON COLUMN iot_pay_channel_config.channel     IS '渠道类型（mock/wx_native/ali_qr）';
COMMENT ON COLUMN iot_pay_channel_config.merchant_id IS '商户号';
COMMENT ON COLUMN iot_pay_channel_config.app_id      IS '应用ID';
COMMENT ON COLUMN iot_pay_channel_config.private_key IS '商户私钥';
COMMENT ON COLUMN iot_pay_channel_config.public_key  IS '平台公钥';
COMMENT ON COLUMN iot_pay_channel_config.api_key     IS 'API 密钥';
COMMENT ON COLUMN iot_pay_channel_config.cert_path   IS '证书路径';
COMMENT ON COLUMN iot_pay_channel_config.notify_url  IS '异步回调地址';
COMMENT ON COLUMN iot_pay_channel_config.status      IS '状态（0正常 1停用）';
COMMENT ON COLUMN iot_pay_channel_config.del_flag    IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_pay_channel_config.remark      IS '备注';
COMMENT ON COLUMN iot_pay_channel_config.create_dept IS '创建部门';
COMMENT ON COLUMN iot_pay_channel_config.create_by   IS '创建者';
COMMENT ON COLUMN iot_pay_channel_config.create_time IS '创建时间';
COMMENT ON COLUMN iot_pay_channel_config.update_by   IS '更新者';
COMMENT ON COLUMN iot_pay_channel_config.update_time IS '更新时间';

-- ----------------------------------------------------------------------------
-- 4. 菜单与权限（挂在"Snail-IoT"一级目录 1900000000000000001 下）
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000003001 AND 1900000000000003999;

-- 4.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000003001, '支付管理', 1900000000000000001, 8, 'pay', 'pay/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:pay:list', 'money', 103, 1, now(), 1, now(), '聚合支付与退款');

-- 4.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000003002, '支付下单', 1900000000000003001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:create', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003003, '支付查询', 1900000000000003001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003004, '支付退款', 1900000000000003001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:refund', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003005, '渠道配置', 1900000000000003001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:config', '#', 103, 1, now(), 1, now(), '');

-- 4.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000003001 AND 1900000000000003999
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. 种子数据：模拟支付渠道（本地自洽验证用）
-- ----------------------------------------------------------------------------
DELETE FROM iot_pay_channel_config WHERE config_id = 1900000000000004001;
INSERT INTO iot_pay_channel_config (config_id, config_key, config_name, channel, status, del_flag, remark,
                                    create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000004001, 'mock', '模拟支付', 'mock', '0', '0', '本地自洽验证渠道，无需真实商户号',
        103, 1, now(), 1, now());
