package org.dromara.iot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备在线状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum DeviceOnlineStatusEnum {

    /**
     * 离线
     */
    OFFLINE("0", "离线"),

    /**
     * 在线
     */
    ONLINE("1", "在线"),

    /**
     * 未知（尚未上报过数据）
     */
    UNKNOWN("2", "未知");

    private final String code;

    private final String desc;

}
