package org.dromara.iot.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道
 *
 * @author iot team
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    /**
     * 模拟渠道（本地自洽，无真实商户号时使用）
     */
    MOCK("mock", "模拟支付"),

    /**
     * 微信 Native 扫码支付（APIv3）
     */
    WX_NATIVE("wx_native", "微信扫码支付"),

    /**
     * 支付宝当面付（扫码）
     */
    ALI_QR("ali_qr", "支付宝扫码支付");

    private final String code;

    private final String desc;

    public static PayChannelEnum fromCode(String code) {
        for (PayChannelEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

}
