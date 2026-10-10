package org.dromara.iot.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayRefund;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付退款单视图对象 iot_pay_refund
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayRefund.class)
public class IotPayRefundVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 退款单ID
     */
    private Long refundId;

    /**
     * 平台退款单号
     */
    private String refundNo;

    /**
     * 关联支付单号
     */
    private String orderNo;

    /**
     * 退款金额（元）
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 状态（0退款中 1退款成功 2退款失败）
     */
    private String status;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
