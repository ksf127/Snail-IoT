package org.dromara.iot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付退款单对象 iot_pay_refund
 * <p>
 * 状态：0退款中 1退款成功 2退款失败。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_pay_refund")
public class IotPayRefund extends BaseEntity {

    /**
     * 退款单ID
     */
    @TableId(value = "refund_id", type = IdType.ASSIGN_ID)
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
     * 退款回调原文
     */
    private String notifyData;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
