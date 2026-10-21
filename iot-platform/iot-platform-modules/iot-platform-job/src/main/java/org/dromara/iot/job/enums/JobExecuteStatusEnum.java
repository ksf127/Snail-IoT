package org.dromara.iot.job.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务执行状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum JobExecuteStatusEnum {

    /**
     * 执行成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 执行失败
     */
    FAIL("FAIL", "失败");

    private final String code;

    private final String name;

    /**
     * 按编码解析，不存在返回 null
     */
    public static JobExecuteStatusEnum fromCode(String code) {
        for (JobExecuteStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
