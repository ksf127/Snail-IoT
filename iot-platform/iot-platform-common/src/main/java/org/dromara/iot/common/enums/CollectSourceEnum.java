package org.dromara.iot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据采集来源
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum CollectSourceEnum {

    /**
     * MQTT 网关/设备直连
     */
    MQTT("MQTT", "MQTT采集"),

    /**
     * HTTPS 设备主动上报
     */
    HTTPS("HTTPS", "HTTPS采集"),

    /**
     * 手动/接口补偿录入
     */
    MANUAL("MANUAL", "手动录入");

    private final String code;

    private final String desc;

}
