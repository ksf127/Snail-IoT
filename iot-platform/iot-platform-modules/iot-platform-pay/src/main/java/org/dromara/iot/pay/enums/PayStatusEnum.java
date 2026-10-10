package org.dromara.iot.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付单状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {

    /**
     * 待支付
     */
    WAIT("0", "待支付"),

    /**
     * 已支付
     */
    PAID("1", "已支付"),

    /**
     * 已关闭（超时或取消）
     */
    CLOSED("2", "已关闭"),

    /**
     * 已退款
     */
    REFUNDED("3", "已退款");

    private final String code;

    private final String desc;

}
