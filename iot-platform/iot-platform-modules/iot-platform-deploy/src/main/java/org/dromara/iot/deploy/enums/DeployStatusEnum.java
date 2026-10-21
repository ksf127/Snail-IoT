package org.dromara.iot.deploy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 部署任务状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum DeployStatusEnum {

    /**
     * 待执行
     */
    PENDING("0", "待执行"),

    /**
     * 执行中
     */
    RUNNING("1", "执行中"),

    /**
     * 成功
     */
    SUCCESS("2", "成功"),

    /**
     * 失败
     */
    FAIL("3", "失败");

    private final String code;

    private final String desc;

}
