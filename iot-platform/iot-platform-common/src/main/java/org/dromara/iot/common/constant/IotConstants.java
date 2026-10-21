package org.dromara.iot.common.constant;

/**
 * IoT 平台全局常量
 *
 * @author iot team
 */
public interface IotConstants {

    /**
     * MQTT 设备上行数据默认主题前缀
     */
    String MQTT_TOPIC_DEVICE_PREFIX = "iot/device/";

    /**
     * MQTT 设备下行指令默认主题前缀
     */
    String MQTT_TOPIC_COMMAND_PREFIX = "iot/command/";

    /**
     * Kafka 主题：设备上行数据
     */
    String KAFKA_TOPIC_DEVICE_DATA = "iot-device-data";

    /**
     * Kafka 主题：设备生命周期事件（上线/离线/告警）
     */
    String KAFKA_TOPIC_DEVICE_EVENT = "iot-device-event";

    /**
     * Redis 缓存前缀：单点登录票据
     */
    String SSO_TICKET_PREFIX = "iot:sso:ticket:";

    /**
     * Redis 缓存前缀：CAS Service Ticket（服务票据）
     */
    String SSO_CAS_TICKET_PREFIX = "iot:sso:cas:st:";

    /**
     * Redis 缓存前缀：CAS/SSO 会话（记录用户已登出的应用会话，用于全局登出）
     */
    String SSO_SESSION_PREFIX = "iot:sso:session:";

    /**
     * OAuth2 客户端注册表缓存前缀
     */
    String OAUTH2_CLIENT_CACHE_PREFIX = "iot:oauth2:client:";

    /**
     * Redis 缓存前缀：设备在线状态
     */
    String DEVICE_ONLINE_PREFIX = "iot:device:online:";

    /**
     * Redis 缓存前缀：设备上报频率限制
     */
    String COLLECT_LIMIT_PREFIX = "iot:collect:limit:";

    /**
     * 设备上报频率限制：单位时间窗口内的最大上报次数
     */
    int COLLECT_LIMIT_RATE = 100;

    /**
     * 设备上报频率限制：时间窗口（秒）
     */
    int COLLECT_LIMIT_INTERVAL = 60;

    /**
     * Redis 缓存前缀：报表批量导入任务（进度 + 结果）
     */
    String REPORT_IMPORT_TASK_PREFIX = "iot:report:import:task:";

    /**
     * 报表批量导入任务缓存有效期（秒）
     */
    long REPORT_IMPORT_TASK_TTL_SECONDS = 3600;

    /**
     * Redis 缓存前缀：报表分片上传会话
     */
    String REPORT_CHUNK_SESSION_PREFIX = "iot:report:chunk:session:";

    /**
     * 报表分片上传会话缓存有效期（秒）
     */
    long REPORT_CHUNK_SESSION_TTL_SECONDS = 7200;

    /**
     * 报表分片上传临时目录名（位于系统临时目录下）
     */
    String REPORT_CHUNK_TEMP_DIR = "iot-chunks";

    /**
     * Elasticsearch 索引前缀：设备上行数据
     */
    String ES_INDEX_DEVICE_DATA = "iot_device_data";

    /**
     * 采集数据入库批量大小
     */
    int COLLECT_BATCH_SIZE = 500;

    /**
     * 支付单号前缀
     */
    String PAY_ORDER_NO_PREFIX = "PAY";

    /**
     * 退款单号前缀
     */
    String PAY_REFUND_NO_PREFIX = "REF";

    /**
     * 商品编号前缀
     */
    String PAY_PRODUCT_NO_PREFIX = "PRD";

    /**
     * Redis 缓存前缀：支付渠道配置
     */
    String PAY_CHANNEL_CONFIG_CACHE_PREFIX = "iot:pay:channel:";

    /**
     * Redis 缓存前缀：支付回调幂等锁
     */
    String PAY_NOTIFY_LOCK_PREFIX = "iot:pay:notify:lock:";

    /**
     * 支付回调幂等锁有效期（秒）
     */
    long PAY_NOTIFY_LOCK_TTL_SECONDS = 60;

    /**
     * Redis 缓存前缀：部署任务并发锁（应用编码 + 目标环境）
     */
    String DEPLOY_TASK_LOCK_PREFIX = "iot:deploy:lock:";

    /**
     * 部署任务并发锁有效期（秒）
     */
    long DEPLOY_TASK_LOCK_TTL_SECONDS = 1800;

    /**
     * 工单编号前缀
     */
    String TICKET_NO_PREFIX = "TIC";

    /**
     * 工单规则引擎（LiteFlow）链路标识
     */
    String TICKET_RULE_CHAIN_ID = "ticketRuleChain";

}
