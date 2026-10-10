package org.dromara.iot.collect.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 设备上报数据消息体
 * <p>
 * MQTT / HTTPS 两种协议统一转换为该结构后再投递至 Kafka。
 *
 * @author iot team
 */
@Data
public class DeviceDataMessage implements Serializable {

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 采集来源 MQTT / HTTPS / MANUAL
     */
    private String source;

    /**
     * 设备上报的原始报文（JSON 字符串）
     */
    private String payload;

    /**
     * 解析后的物模型属性值
     */
    private Map<String, Object> properties;

    /**
     * 上报时间（毫秒时间戳）
     */
    private Long timestamp;

    /**
     * 链路追踪 ID，便于全链路排查
     */
    private String traceId;

}
