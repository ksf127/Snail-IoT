package org.dromara.iot.job.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务触发方式
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum JobTriggerTypeEnum {

    /**
     * Cron 表达式触发
     */
    CRON("CRON", "Cron 表达式"),

    /**
     * 固定频率触发（秒）
     */
    FIXED_RATE("FIXED_RATE", "固定频率"),

    /**
     * 仅手动触发
     */
    MANUAL("MANUAL", "手动触发"),

    /**
     * SnailJob 服务端触发（仅作为执行日志的触发来源标识）
     */
    SNAILJOB("SNAILJOB", "SnailJob 触发");

    private final String code;

    private final String name;

    /**
     * 按编码解析，不存在返回 null
     */
    public static JobTriggerTypeEnum fromCode(String code) {
        for (JobTriggerTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
