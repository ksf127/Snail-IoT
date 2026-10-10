package org.dromara.iot.pay.channel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退款结果
 *
 * @author iot team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResult {

    /**
     * 是否退款成功
     */
    private boolean success;

    /**
     * 渠道原始返回
     */
    private String rawData;

}
