package org.dromara.iot.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款单状态
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    /**
     * 退款中
     */
    REFUNDING("0", "退款中"),

    /**
     * 退款成功
     */
    SUCCESS("1", "退款成功"),

    /**
     * 退款失败
     */
    FAIL("2", "退款失败");

    private final String code;

    private final String desc;

}
