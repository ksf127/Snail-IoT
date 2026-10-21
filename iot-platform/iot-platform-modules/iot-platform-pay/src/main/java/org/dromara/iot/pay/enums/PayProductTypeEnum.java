package org.dromara.iot.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付商品类型
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum PayProductTypeEnum {

    /**
     * 虚拟服务（如设备接入配额、数据分析服务等）
     */
    VIRTUAL_SERVICE("virtual_service", "虚拟服务"),

    /**
     * 虚拟卡券
     */
    VIRTUAL_CARD("virtual_card", "虚拟卡券"),

    /**
     * 会员权益
     */
    MEMBERSHIP("membership", "会员权益");

    private final String code;

    private final String desc;

    public static PayProductTypeEnum fromCode(String code) {
        for (PayProductTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
