package org.dromara.iot.cloud.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 纳管资源类型
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum CloudResourceTypeEnum {

    /**
     * 阿里云 ECS 云服务器
     */
    ECS("ecs", "ECS 云服务器"),

    /**
     * Flink 作业
     */
    FLINK_JOB("flink-job", "Flink 作业");

    private final String code;

    private final String name;

    /**
     * 按编码解析，不存在返回 null
     */
    public static CloudResourceTypeEnum fromCode(String code) {
        for (CloudResourceTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
