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
    product_id   BIGINT,
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
COMMENT ON COLUMN iot_pay_order.product_id   IS '关联商品ID（为空表示自由金额下单）';
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
    gateway_url VARCHAR(255),
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
COMMENT ON COLUMN iot_pay_channel_config.gateway_url IS '网关地址（为空使用渠道默认地址，如支付宝沙箱）';
COMMENT ON COLUMN iot_pay_channel_config.status      IS '状态（0正常 1停用）';
COMMENT ON COLUMN iot_pay_channel_config.del_flag    IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_pay_channel_config.remark      IS '备注';
COMMENT ON COLUMN iot_pay_channel_config.create_dept IS '创建部门';
COMMENT ON COLUMN iot_pay_channel_config.create_by   IS '创建者';
COMMENT ON COLUMN iot_pay_channel_config.create_time IS '创建时间';
COMMENT ON COLUMN iot_pay_channel_config.update_by   IS '更新者';
COMMENT ON COLUMN iot_pay_channel_config.update_time IS '更新时间';

-- ----------------------------------------------------------------------------
-- 4. 支付商品表（虚拟商品货架：虚拟服务/卡券/会员权益）
--    下单可选择商品，由服务端取价并锁定标题，规避客户端篡改金额
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_pay_product;
CREATE TABLE iot_pay_product
(
    product_id   BIGINT         NOT NULL,
    product_no   VARCHAR(64)    NOT NULL,
    product_name VARCHAR(128)   NOT NULL,
    product_type VARCHAR(32)    NOT NULL,
    price        NUMERIC(12, 2) NOT NULL,
    currency     VARCHAR(8)              DEFAULT 'CNY',
    status       CHAR(1)                 DEFAULT '0',
    description  VARCHAR(500),
    del_flag     CHAR(1)                 DEFAULT '0',
    remark       VARCHAR(500)            DEFAULT '',
    create_dept  BIGINT,
    create_by    BIGINT,
    create_time  TIMESTAMP,
    update_by    BIGINT,
    update_time  TIMESTAMP,
    PRIMARY KEY (product_id)
);
CREATE UNIQUE INDEX uk_iot_pay_product_no ON iot_pay_product (product_no);
COMMENT ON TABLE iot_pay_product IS '支付商品表（虚拟商品货架）';
COMMENT ON COLUMN iot_pay_product.product_id   IS '商品ID';
COMMENT ON COLUMN iot_pay_product.product_no   IS '商品编号（唯一）';
COMMENT ON COLUMN iot_pay_product.product_name IS '商品名称';
COMMENT ON COLUMN iot_pay_product.product_type IS '商品类型（virtual_service/virtual_card/membership）';
COMMENT ON COLUMN iot_pay_product.price        IS '售价（元）';
COMMENT ON COLUMN iot_pay_product.currency     IS '币种';
COMMENT ON COLUMN iot_pay_product.status       IS '状态（0上架 1下架）';
COMMENT ON COLUMN iot_pay_product.description  IS '商品描述';
COMMENT ON COLUMN iot_pay_product.del_flag     IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_pay_product.remark       IS '备注';
COMMENT ON COLUMN iot_pay_product.create_dept  IS '创建部门';
COMMENT ON COLUMN iot_pay_product.create_by    IS '创建者';
COMMENT ON COLUMN iot_pay_product.create_time  IS '创建时间';
COMMENT ON COLUMN iot_pay_product.update_by    IS '更新者';
COMMENT ON COLUMN iot_pay_product.update_time  IS '更新时间';

-- ----------------------------------------------------------------------------
-- 5. 菜单与权限（挂在"Snail-IoT"一级目录 1900000000000000001 下）
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000003001 AND 1900000000000003999;

-- 5.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000003001, '支付管理', 1900000000000000001, 8, 'pay', 'pay/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:pay:list', 'money', 103, 1, now(), 1, now(), '聚合支付与退款');

-- 5.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000003002, '支付下单', 1900000000000003001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:create', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003003, '支付查询', 1900000000000003001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003004, '支付退款', 1900000000000003001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:refund', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003005, '渠道配置', 1900000000000003001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:config', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000003006, '商品管理', 1900000000000003001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:pay:product', '#', 103, 1, now(), 1, now(), '');

-- 5.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000003001 AND 1900000000000003999
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 6. 种子数据：模拟支付渠道（本地自洽验证用）
-- ----------------------------------------------------------------------------
DELETE FROM iot_pay_channel_config WHERE config_id = 1900000000000004001;
INSERT INTO iot_pay_channel_config (config_id, config_key, config_name, channel, status, del_flag, remark,
                                    create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000004001, 'mock', '模拟支付', 'mock', '0', '0', '本地自洽验证渠道，无需真实商户号',
        103, 1, now(), 1, now());

-- ----------------------------------------------------------------------------
-- 7. 种子数据：真实商户渠道凭证模板（微信 Native / 支付宝当面付 / 支付宝沙箱）
--    占位符需替换为真实凭证后方可启用：
--      - private_key: 商户/应用私钥 PEM 全文（含 -----BEGIN ... PRIVATE KEY-----），
--                     多行内容用美元引用 $pem$ ... $pem$ 包裹
--      - 微信: api_key=APIv3 密钥; cert_path=商户证书序列号 serial_no; public_key 留空
--      - 支付宝: public_key=支付宝公钥（沙箱为"支付宝公钥"，非应用公钥）; api_key/cert_path 留空
--      - gateway_url: 为空使用生产网关 https://openapi.alipay.com/gateway.do；
--                     支付宝沙箱填 https://openapi-sandbox.dl.alipaydev.com/gateway.do（微信无沙箱，留空）
--      - notify_url 必须是公网 HTTPS，路径为 /iot/pay/notify/{channel}
--    默认 status='1'（停用），填入真实凭证后改为 '0' 启用
--    注意：同一渠道仅"状态=0"的第一条生效，故支付宝生产(4003)与沙箱(4004)请只启用其一。
-- ----------------------------------------------------------------------------
DELETE FROM iot_pay_channel_config WHERE config_id IN (1900000000000004002, 1900000000000004003, 1900000000000004004);

-- 7.1 微信 Native（演示占位符，不可用于真实支付）
INSERT INTO iot_pay_channel_config (config_id, config_key, config_name, channel,
                                    merchant_id, app_id, private_key, public_key, api_key, cert_path,
                                    notify_url, gateway_url, status, del_flag, remark,
                                    create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000004002, 'wx_native_default', '微信扫码支付', 'wx_native',
        '1900000001',                                   -- 商户号 mchId（演示占位符）
        'wx8888888888888888',                           -- 公众号/小程序 appId（演示占位符）
        $pem$-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQ...（apiclient_key.pem 全文，演示占位符）
-----END PRIVATE KEY-----$pem$,
        '',                                             -- 微信无需
        'DEMO00000000000000000000000000',               -- APIv3 密钥（32 位，演示占位符）
        '5F8A1B2C3D4E5F6A7B8C9D0E1F2A3B4C5D6E7F8A',    -- 商户证书序列号 serial_no（演示占位符）
        'https://your-domain.com/iot/pay/notify/wx_native',
        NULL,                                           -- 微信无沙箱，留空用默认地址
        '1', '0', '演示占位符，不可用于真实支付；替换真实微信商户凭证后改为启用',
        103, 1, now(), 1, now());

-- 7.2 支付宝当面付（生产网关，演示占位符）
INSERT INTO iot_pay_channel_config (config_id, config_key, config_name, channel,
                                    merchant_id, app_id, private_key, public_key, api_key, cert_path,
                                    notify_url, gateway_url, status, del_flag, remark,
                                    create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000004003, 'ali_qr_default', '支付宝扫码支付', 'ali_qr',
        '',                                             -- 支付宝无需
        '2021000000000000',                             -- 应用 AppId（演示占位符）
        $pem$-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...（应用私钥 PEM 全文，演示占位符）
-----END RSA PRIVATE KEY-----$pem$,
        $pem$-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...（支付宝公钥 PEM 全文，演示占位符）
-----END PUBLIC KEY-----$pem$,
        '',                                             -- 支付宝无需
        '',                                             -- 支付宝无需
        'https://your-domain.com/iot/pay/notify/ali_qr',
        NULL,                                           -- 留空走生产网关
        '1', '0', '演示占位符，不可用于真实支付；走生产网关 https://openapi.alipay.com/gateway.do',
        103, 1, now(), 1, now());

-- 7.3 支付宝沙箱（预设沙箱网关，替换沙箱应用凭证后启用即可联调）
--     沙箱网关：https://openapi-sandbox.dl.alipaydev.com/gateway.do
--     沙箱凭证获取：支付宝开放平台 -> 沙箱环境 -> 应用信息（AppId/应用私钥/支付宝公钥）
INSERT INTO iot_pay_channel_config (config_id, config_key, config_name, channel,
                                    merchant_id, app_id, private_key, public_key, api_key, cert_path,
                                    notify_url, gateway_url, status, del_flag, remark,
                                    create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000004004, 'ali_qr_sandbox', '支付宝扫码支付（沙箱）', 'ali_qr',
        '',                                             -- 支付宝无需
        '2021000000000000',                             -- 沙箱应用 AppId（需自行填入）
        $pem$-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...（沙箱应用私钥 PEM 全文，需自行填入）
-----END RSA PRIVATE KEY-----$pem$,
        $pem$-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...（沙箱支付宝公钥 PEM 全文，需自行填入）
-----END PUBLIC KEY-----$pem$,
        '',                                             -- 支付宝无需
        '',                                             -- 支付宝无需
        'https://your-domain.com/iot/pay/notify/ali_qr',
        'https://openapi-sandbox.dl.alipaydev.com/gateway.do',  -- 支付宝沙箱网关
        '1', '0', '支付宝沙箱：填入沙箱 AppId/应用私钥/支付宝公钥后改为启用（与生产渠道互斥，只启用其一）',
        103, 1, now(), 1, now());

-- ----------------------------------------------------------------------------
-- 8. 种子数据：虚拟商品货架（用于下单流程测试）
--    说明：product_no 唯一；status 0上架 1下架
-- ----------------------------------------------------------------------------
DELETE FROM iot_pay_product WHERE product_id BETWEEN 1900000000000005001 AND 1900000000000005999;

INSERT INTO iot_pay_product (product_id, product_no, product_name, product_type, price, currency, status,
                             description, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (1900000000000005001, 'PRD-VIRTUAL-SERVICE', '虚拟服务', 'virtual_service', 9.90, 'CNY', '0',
     '示例虚拟商品：IoT 平台虚拟服务，用于下单流程自洽验证', '0', '默认虚拟商品种子',
     103, 1, now(), 1, now()),

    (1900000000000005002, 'PRD-DEVICE-QUOTA-100', '设备接入配额（100 台/年）', 'virtual_service', 99.00, 'CNY', '0',
     '测试商品：100 台设备接入配额，有效期一年', '0', '测试数据',
     103, 1, now(), 1, now()),

    (1900000000000005003, 'PRD-DATA-ANALYSIS-MONTH', '数据分析服务包（月）', 'virtual_service', 199.00, 'CNY', '0',
     '测试商品：数据分析服务包，按月计费', '0', '测试数据',
     103, 1, now(), 1, now()),

    (1900000000000005004, 'PRD-VIP-CARD-100', '100 元代金卡', 'virtual_card', 100.00, 'CNY', '0',
     '测试商品：100 元面值虚拟卡券', '0', '测试数据',
     103, 1, now(), 1, now()),

    (1900000000000005005, 'PRD-MEMBER-YEAR', '高级会员年卡', 'membership', 999.00, 'CNY', '1',
     '测试商品：高级会员权益，当前为下架状态', '0', '测试数据（下架示例）',
     103, 1, now(), 1, now());
