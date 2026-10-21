package org.dromara.iot.cloud.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 云平台类型
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum CloudProviderEnum {

    /**
     * 内置模拟（本地自洽，无需真实凭证，用于功能验证）
     */
    MOCK("mock", "内置模拟"),

    /**
     * 阿里云（ECS OpenAPI，RPC 签名）
     */
    ALIYUN("aliyun", "阿里云"),

    /**
     * Apache Flink（JobManager REST API）
     */
    FLINK("flink", "Flink");

    private final String code;

    private final String name;

    /**
     * 按编码解析，不存在返回 null
     */
    public static CloudProviderEnum fromCode(String code) {
        for (CloudProviderEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
