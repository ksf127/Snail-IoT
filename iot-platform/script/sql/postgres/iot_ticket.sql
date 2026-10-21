-- ============================================================================
-- Snail-IoT - 工单管理（普通工单 + 规则引擎 + 流程引擎）
--
-- 使用前提：
--   1. 已导入 iot 基础脚本
--   2. 已导入 iot_platform.sql（含"Snail-IoT"一级目录 menu_id=1900000000000000001）
--   3. 流程工单需启用 Warm-Flow（application.yml 中 warm-flow.enabled=true）
--
-- 执行方式：
--   docker exec -i ruoyi-postgres psql -U postgres -d ruoyi < iot_ticket.sql
--
-- 说明：
--   - 工单支持三类：normal 普通工单（人工流转）、rule 规则工单（LiteFlow 自动处理）、
--     flow 流程工单（Warm-Flow 审批）；
--   - 规则表 iot_ticket_rule 为规则引擎数据源，命中后执行自动分配/调整优先级/自动关闭。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 工单主表
--    状态机：0待受理 -> 1处理中 -> 2已完成；1处理中 -> 3已关闭 / 4已驳回
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_ticket;
CREATE TABLE iot_ticket
(
    ticket_id           BIGINT       NOT NULL,
    ticket_no           VARCHAR(64)  NOT NULL,
    title               VARCHAR(128) NOT NULL,
    content             VARCHAR(2000),
    ticket_type         VARCHAR(32)           DEFAULT 'normal',
    category            VARCHAR(64),
    priority            CHAR(1)               DEFAULT '1',
    status              CHAR(1)               DEFAULT '0',
    source              VARCHAR(32)           DEFAULT 'web',
    applicant_id        BIGINT,
    applicant_name      VARCHAR(64),
    handler_id          BIGINT,
    handler_name        VARCHAR(64),
    rule_code           VARCHAR(64),
    auto_processed      CHAR(1)               DEFAULT '0',
    flow_code           VARCHAR(64),
    process_instance_id BIGINT,
    business_status     VARCHAR(32),
    expected_time       TIMESTAMP,
    finish_time         TIMESTAMP,
    close_time          TIMESTAMP,
    del_flag            CHAR(1)               DEFAULT '0',
    remark              VARCHAR(500)          DEFAULT '',
    create_dept         BIGINT,
    create_by           BIGINT,
    create_time         TIMESTAMP,
    update_by           BIGINT,
    update_time         TIMESTAMP,
    PRIMARY KEY (ticket_id)
);
CREATE UNIQUE INDEX uk_iot_ticket_no ON iot_ticket (ticket_no);
CREATE INDEX idx_iot_ticket_status ON iot_ticket (status);
CREATE INDEX idx_iot_ticket_type ON iot_ticket (ticket_type);
CREATE INDEX idx_iot_ticket_handler ON iot_ticket (handler_id);
COMMENT ON TABLE iot_ticket IS '工单表';
COMMENT ON COLUMN iot_ticket.ticket_id           IS '工单ID';
COMMENT ON COLUMN iot_ticket.ticket_no           IS '工单编号（唯一）';
COMMENT ON COLUMN iot_ticket.title               IS '工单标题';
COMMENT ON COLUMN iot_ticket.content             IS '工单内容';
COMMENT ON COLUMN iot_ticket.ticket_type         IS '工单类型（normal普通/rule规则/flow流程）';
COMMENT ON COLUMN iot_ticket.category            IS '工单分类';
COMMENT ON COLUMN iot_ticket.priority            IS '优先级（0低 1中 2高 3紧急）';
COMMENT ON COLUMN iot_ticket.status              IS '状态（0待受理 1处理中 2已完成 3已关闭 4已驳回）';
COMMENT ON COLUMN iot_ticket.source              IS '来源（web/app/api）';
COMMENT ON COLUMN iot_ticket.applicant_id        IS '申请人ID';
COMMENT ON COLUMN iot_ticket.applicant_name      IS '申请人名称';
COMMENT ON COLUMN iot_ticket.handler_id          IS '处理人ID';
COMMENT ON COLUMN iot_ticket.handler_name        IS '处理人名称';
COMMENT ON COLUMN iot_ticket.rule_code           IS '规则编码（规则工单命中的规则）';
COMMENT ON COLUMN iot_ticket.auto_processed      IS '是否被规则引擎自动处理（0否 1是）';
COMMENT ON COLUMN iot_ticket.flow_code           IS '流程定义编码（流程工单使用）';
COMMENT ON COLUMN iot_ticket.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN iot_ticket.business_status     IS '流程业务状态';
COMMENT ON COLUMN iot_ticket.expected_time       IS '期望完成时间';
COMMENT ON COLUMN iot_ticket.finish_time         IS '完成时间';
COMMENT ON COLUMN iot_ticket.close_time          IS '关闭时间';
COMMENT ON COLUMN iot_ticket.del_flag            IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_ticket.remark              IS '备注';
COMMENT ON COLUMN iot_ticket.create_dept         IS '创建部门';
COMMENT ON COLUMN iot_ticket.create_by           IS '创建者';
COMMENT ON COLUMN iot_ticket.create_time         IS '创建时间';
COMMENT ON COLUMN iot_ticket.update_by           IS '更新者';
COMMENT ON COLUMN iot_ticket.update_time         IS '更新时间';

-- ----------------------------------------------------------------------------
-- 2. 工单流转记录表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_ticket_record;
CREATE TABLE iot_ticket_record
(
    record_id       BIGINT       NOT NULL,
    ticket_id       BIGINT       NOT NULL,
    action          VARCHAR(32),
    node_name       VARCHAR(64),
    operator_id     BIGINT,
    operator_name   VARCHAR(64),
    message         VARCHAR(1000),
    process_task_id BIGINT,
    del_flag        CHAR(1)               DEFAULT '0',
    remark          VARCHAR(500)          DEFAULT '',
    create_dept     BIGINT,
    create_by       BIGINT,
    create_time     TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP,
    PRIMARY KEY (record_id)
);
CREATE INDEX idx_iot_ticket_record_ticket ON iot_ticket_record (ticket_id);
COMMENT ON TABLE iot_ticket_record IS '工单流转记录表';
COMMENT ON COLUMN iot_ticket_record.record_id       IS '记录ID';
COMMENT ON COLUMN iot_ticket_record.ticket_id       IS '工单ID';
COMMENT ON COLUMN iot_ticket_record.action          IS '动作（create/accept/handle/rule/flow-start/flow-complete/finish/close/reject/comment）';
COMMENT ON COLUMN iot_ticket_record.node_name       IS '节点名称';
COMMENT ON COLUMN iot_ticket_record.operator_id     IS '操作人ID';
COMMENT ON COLUMN iot_ticket_record.operator_name   IS '操作人名称';
COMMENT ON COLUMN iot_ticket_record.message         IS '处理意见 / 说明';
COMMENT ON COLUMN iot_ticket_record.process_task_id IS '流程任务ID';
COMMENT ON COLUMN iot_ticket_record.del_flag        IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_ticket_record.remark          IS '备注';
COMMENT ON COLUMN iot_ticket_record.create_dept     IS '创建部门';
COMMENT ON COLUMN iot_ticket_record.create_by       IS '创建者';
COMMENT ON COLUMN iot_ticket_record.create_time     IS '创建时间';
COMMENT ON COLUMN iot_ticket_record.update_by       IS '更新者';
COMMENT ON COLUMN iot_ticket_record.update_time     IS '更新时间';

-- ----------------------------------------------------------------------------
-- 3. 工单规则表（规则引擎 LiteFlow 数据源）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS iot_ticket_rule;
CREATE TABLE iot_ticket_rule
(
    rule_id        BIGINT       NOT NULL,
    rule_code      VARCHAR(64)  NOT NULL,
    rule_name      VARCHAR(128) NOT NULL,
    rule_type      VARCHAR(32)  NOT NULL,
    match_field    VARCHAR(32)  NOT NULL,
    match_operator VARCHAR(16)  NOT NULL,
    match_value    VARCHAR(255),
    target_value   VARCHAR(255),
    status         CHAR(1)               DEFAULT '0',
    sort_no        INT                   DEFAULT 0,
    del_flag       CHAR(1)               DEFAULT '0',
    remark         VARCHAR(500)          DEFAULT '',
    create_dept    BIGINT,
    create_by      BIGINT,
    create_time    TIMESTAMP,
    update_by      BIGINT,
    update_time    TIMESTAMP,
    PRIMARY KEY (rule_id)
);
CREATE UNIQUE INDEX uk_iot_ticket_rule_code ON iot_ticket_rule (rule_code);
COMMENT ON TABLE iot_ticket_rule IS '工单规则表（规则引擎数据源）';
COMMENT ON COLUMN iot_ticket_rule.rule_id        IS '规则ID';
COMMENT ON COLUMN iot_ticket_rule.rule_code      IS '规则编码（唯一）';
COMMENT ON COLUMN iot_ticket_rule.rule_name      IS '规则名称';
COMMENT ON COLUMN iot_ticket_rule.rule_type      IS '规则类型（assign自动分配/priority调整优先级/close自动关闭）';
COMMENT ON COLUMN iot_ticket_rule.match_field    IS '匹配字段（category/ticketType/priority/source/title）';
COMMENT ON COLUMN iot_ticket_rule.match_operator IS '匹配操作（eq/ne/contains/in）';
COMMENT ON COLUMN iot_ticket_rule.match_value    IS '匹配值（in 以英文逗号分隔）';
COMMENT ON COLUMN iot_ticket_rule.target_value   IS '目标值（assign为处理人用户ID，priority为优先级编码）';
COMMENT ON COLUMN iot_ticket_rule.status         IS '状态（0启用 1停用）';
COMMENT ON COLUMN iot_ticket_rule.sort_no        IS '排序号（越小越先匹配）';
COMMENT ON COLUMN iot_ticket_rule.del_flag       IS '删除标志（0代表存在 1代表删除）';
COMMENT ON COLUMN iot_ticket_rule.remark         IS '备注';
COMMENT ON COLUMN iot_ticket_rule.create_dept    IS '创建部门';
COMMENT ON COLUMN iot_ticket_rule.create_by      IS '创建者';
COMMENT ON COLUMN iot_ticket_rule.create_time    IS '创建时间';
COMMENT ON COLUMN iot_ticket_rule.update_by      IS '更新者';
COMMENT ON COLUMN iot_ticket_rule.update_time    IS '更新时间';

-- ----------------------------------------------------------------------------
-- 4. 菜单与权限（挂在"Snail-IoT"一级目录 1900000000000000001 下）
-- ----------------------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 1900000000000006001 AND 1900000000000006999;

-- 4.1 二级菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000006001, '工单管理', 1900000000000000001, 13, 'ticket', 'ticket/index', '', 'N', 'Y',
        'C', '0', '0', 'iot:ticket:list', 'form', 103, 1, now(), 1, now(), '工单管理：普通/规则/流程');

-- 4.2 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1900000000000006002, '工单查询', 1900000000000006001, 1, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:query', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006003, '工单新增', 1900000000000006001, 2, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006004, '工单修改', 1900000000000006001, 3, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006005, '工单删除', 1900000000000006001, 4, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:remove', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006006, '工单办理', 1900000000000006001, 5, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:handle', '#', 103, 1, now(), 1, now(), '提交/受理/流程办理'),
       (1900000000000006007, '规则查询', 1900000000000006001, 6, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:rule:list', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006008, '规则新增', 1900000000000006001, 7, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:rule:add', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006009, '规则修改', 1900000000000006001, 8, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:rule:edit', '#', 103, 1, now(), 1, now(), ''),
       (1900000000000006010, '规则删除', 1900000000000006001, 9, '', NULL, '', 'N', 'Y', 'F', '0', '0', 'iot:ticket:rule:remove', '#', 103, 1, now(), 1, now(), '');

-- 4.3 授权给超管角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1761300000000000001, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 1900000000000006001 AND 1900000000000006999
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. 种子数据：默认工单规则
--    - 仅启用不依赖具体用户ID的安全规则；自动分配示例默认停用，
--      启用前需将 target_value 替换为真实处理人用户ID
-- ----------------------------------------------------------------------------
DELETE FROM iot_ticket_rule WHERE rule_id BETWEEN 1900000000000007001 AND 1900000000000007009;

INSERT INTO iot_ticket_rule (rule_id, rule_code, rule_name, rule_type, match_field, match_operator, match_value,
                             target_value, status, sort_no, del_flag, remark,
                             create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000007001, 'TICKET_RULE_UPGRADE_URGENT', '紧急分类自动升级', 'priority', 'category', 'eq', 'urgent',
        '3', '0', 10, '0', '分类为 urgent 的工单自动升级为紧急',
        103, 1, now(), 1, now()),
       (1900000000000007002, 'TICKET_RULE_AUTO_CLOSE_SPAM', '垃圾工单自动关闭', 'close', 'title', 'contains', 'spam',
        '', '0', 20, '0', '标题包含 spam 的工单自动关闭',
        103, 1, now(), 1, now()),
       (1900000000000007003, 'TICKET_RULE_ASSIGN_NETWORK', '网络类工单自动分配', 'assign', 'category', 'eq', 'network',
        '1', '1', 30, '0', '示例规则：需将 target_value 替换为真实处理人用户ID后启用',
        103, 1, now(), 1, now());

-- ----------------------------------------------------------------------------
-- 6. 种子数据：示例工单与流转记录
--    - 普通工单（normal）：人工流转，覆盖 待受理 / 处理中 / 已完成 三种状态；
--    - 规则工单（rule）：预留 rule_code 与 auto_processed 标记，可点击「执行规则」复现
--      自动升级优先级、自动关闭；
--    - 流程工单（flow）：只填写 flow_code，process_instance_id 留空表示尚未发起，
--      需先在「流程管理」中设计对应流程定义（flow_code=ticket-approve）后点击「提交」真实发起，
--      避免伪造流程实例导致办理失败。
-- ----------------------------------------------------------------------------
DELETE FROM iot_ticket_record WHERE record_id BETWEEN 1900000000000007201 AND 1900000000000007299;
DELETE FROM iot_ticket WHERE ticket_id BETWEEN 1900000000000007101 AND 1900000000000007199;

INSERT INTO iot_ticket (ticket_id, ticket_no, title, content, ticket_type, category, priority, status, source,
                        applicant_id, applicant_name, handler_id, handler_name,
                        rule_code, auto_processed, flow_code, process_instance_id, business_status,
                        expected_time, finish_time, close_time, del_flag, remark,
                        create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000007101, 'TK20260101N001', '生产车间网关设备离线',
        '3 号车间数据采集网关自今日 08:00 起离线，现场指示灯熄灭，请协助排查供电与网络链路。',
        'normal', 'network', '2', '0', 'web',
        1761100000000000001, '云澈', NULL, NULL,
        NULL, '0', NULL, NULL, NULL,
        now() + interval '1 day', NULL, NULL, '0', '示例：普通工单-待受理',
        103, 1, now(), 1, now()),
       (1900000000000007102, 'TK20260101N002', '空压机温度传感器读数异常',
        '2 号空压机温度传感器上报 128℃，明显超出量程，疑似探头故障，需现场校验。',
        'normal', 'device', '1', '1', 'web',
        1761100000000000001, '云澈', 1761100000000000001, '云澈',
        NULL, '0', NULL, NULL, NULL,
        now() + interval '2 day', NULL, NULL, '0', '示例：普通工单-处理中',
        103, 1, now(), 1, now()),
       (1900000000000007103, 'TK20260101N003', '历史数据导出报表乱码',
        '导出近 30 天历史数据为 CSV 时中文出现乱码，Excel 打开显示异常。',
        'normal', 'other', '0', '2', 'web',
        1761100000000000001, '云澈', 1761100000000000001, '云澈',
        NULL, '0', NULL, NULL, NULL,
        now() + interval '3 day', now(), NULL, '0', '示例：普通工单-已完成',
        103, 1, now(), 1, now()),
       (1900000000000007104, 'TK20260101R001', '核心交换机端口告警',
        '核心交换机上联端口持续告警，已影响部分设备数据上报，属紧急事项。',
        'rule', 'urgent', '3', '1', 'web',
        1761100000000000001, '云澈', NULL, NULL,
        'TICKET_RULE_UPGRADE_URGENT', '1', NULL, NULL, NULL,
        now() + interval '4 hour', NULL, NULL, '0', '示例：规则工单-命中紧急升级规则，可点击「执行规则」复现',
        103, 1, now(), 1, now()),
       (1900000000000007105, 'TK20260101R002', 'spam 推广信息测试工单',
        '这是一条用于验证垃圾工单自动关闭规则的测试数据。',
        'rule', 'spam', '0', '3', 'web',
        1761100000000000001, '云澈', NULL, NULL,
        'TICKET_RULE_AUTO_CLOSE_SPAM', '1', NULL, NULL, NULL,
        now() + interval '7 day', NULL, now(), '0', '示例：规则工单-命中垃圾工单自动关闭规则（已关闭）',
        103, 1, now(), 1, now()),
       (1900000000000007106, 'TK20260101F001', '设备巡检计划审批',
        '拟对全部在线网关执行季度巡检，需按流程完成审批后下发。',
        'flow', 'maintain', '1', '0', 'web',
        1761100000000000001, '云澈', NULL, NULL,
        NULL, '0', 'ticket-approve', NULL, NULL,
        now() + interval '5 day', NULL, NULL, '0', '示例：流程工单-待发起（请在流程管理中先定义 flow_code=ticket-approve）',
        103, 1, now(), 1, now()),
       (1900000000000007107, 'TK20260101F002', '年度设备维保预算审批',
        '2026 年度设备维保预算已完成报价比选，流程审批通过并归档。',
        'flow', 'budget', '1', '2', 'web',
        1761100000000000001, '云澈', 1761100000000000001, '云澈',
        NULL, '0', 'ticket-approve', 1900000000000009001, 'FINISH',
        now() + interval '10 day', now(), NULL, '0', '示例：流程工单-流程已完成',
        103, 1, now(), 1, now());

INSERT INTO iot_ticket_record (record_id, ticket_id, action, node_name, operator_id, operator_name, message,
                               process_task_id, del_flag, remark,
                               create_dept, create_by, create_time, update_by, update_time)
VALUES (1900000000000007201, 1900000000000007101, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单，等待受理', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007202, 1900000000000007102, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007203, 1900000000000007102, 'accept', '受理工单', 1761100000000000001, '云澈',
        '已受理，安排现场校验传感器探头', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007204, 1900000000000007103, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007205, 1900000000000007103, 'accept', '受理工单', 1761100000000000001, '云澈',
        '已受理', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007206, 1900000000000007103, 'handle', '处理工单', 1761100000000000001, '云澈',
        '修复导出编码为 UTF-8 BOM，验证通过', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007207, 1900000000000007103, 'finish', '完成工单', 1761100000000000001, '云澈',
        '工单已完成', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007208, 1900000000000007104, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007209, 1900000000000007104, 'rule', '规则引擎', 0, '系统',
        '规则引擎自动处理，命中规则：TICKET_RULE_UPGRADE_URGENT', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007210, 1900000000000007105, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007211, 1900000000000007105, 'rule', '规则引擎', 0, '系统',
        '规则引擎自动处理，命中规则：TICKET_RULE_AUTO_CLOSE_SPAM', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007212, 1900000000000007105, 'close', '关闭工单', 0, '系统',
        '命中垃圾工单规则，自动关闭', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007213, 1900000000000007106, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单，等待发起审批流程', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007214, 1900000000000007107, 'create', '创建工单', 1761100000000000001, '云澈',
        '提交工单', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007215, 1900000000000007107, 'flow-start', '发起流程', 1761100000000000001, '云澈',
        '发起审批流程 ticket-approve', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007216, 1900000000000007107, 'flow-complete', '流程结束', 1761100000000000001, '云澈',
        '审批流程结束', NULL, '0', '',
        103, 1, now(), 1, now()),
       (1900000000000007217, 1900000000000007107, 'finish', '完成工单', 1761100000000000001, '云澈',
        '工单已完成', NULL, '0', '',
        103, 1, now(), 1, now());
