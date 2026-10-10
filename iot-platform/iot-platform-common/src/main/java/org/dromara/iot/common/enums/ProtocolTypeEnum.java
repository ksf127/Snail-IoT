package org.dromara.iot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备接入协议
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum ProtocolTypeEnum {

    /**
     * MQTT 协议（含 MQTT over TLS）
     */
    MQTT("MQTT", "MQTT"),

    /**
     * HTTPS 协议（设备侧主动 HTTP 上报）
     */
    HTTPS("HTTPS", "HTTPS"),

    /**
     * Modbus 透传（网关接入）
     */
    MODBUS("MODBUS", "Modbus"),

    /**
     * 其他自定义协议
     */
    OTHER("OTHER", "其他");

    private final String code;

    private final String desc;

}
