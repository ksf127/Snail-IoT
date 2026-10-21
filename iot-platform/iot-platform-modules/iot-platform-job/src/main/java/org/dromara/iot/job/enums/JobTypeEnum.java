package org.dromara.iot.job.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum JobTypeEnum {

    /**
     * 自建调度（由 IotJobScheduler 基于本地线程池触发）
     */
    LOCAL("LOCAL", "本地调度"),

    /**
     * SnailJob 分布式调度（由 SnailJob 服务端触发，本地登记视图）
     */
    SNAILJOB("SNAILJOB", "SnailJob 调度");

    private final String code;

    private final String name;

    /**
     * 按编码解析，不存在返回 null
     */
    public static JobTypeEnum fromCode(String code) {
        for (JobTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
