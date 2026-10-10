package org.dromara.iot.pay.channel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预下单结果
 *
 * @author iot team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePayResult {

    /**
     * 渠道预支付ID
     */
    private String prePayId;

    /**
     * 二维码链接（扫码支付场景）
     */
    private String codeUrl;

    /**
     * 渠道原始返回
     */
    private String rawData;

}
