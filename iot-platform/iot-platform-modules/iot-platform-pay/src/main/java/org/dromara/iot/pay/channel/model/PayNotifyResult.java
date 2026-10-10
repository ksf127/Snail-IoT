package org.dromara.iot.pay.channel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回调解析结果
 *
 * @author iot team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyResult {

    /**
     * 是否支付成功
     */
    private boolean paid;

    /**
     * 平台支付单号
     */
    private String orderNo;

    /**
     * 渠道交易号
     */
    private String tradeNo;

    /**
     * 回调原文
     */
    private String rawData;

}
