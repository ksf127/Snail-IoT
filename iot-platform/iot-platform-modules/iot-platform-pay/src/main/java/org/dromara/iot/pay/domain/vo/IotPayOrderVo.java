package org.dromara.iot.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单视图对象 iot_pay_order
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayOrder.class)
public class IotPayOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付单ID
     */
    private Long orderId;

    /**
     * 平台支付单号
     */
    private String orderNo;

    /**
     * 业务订单号
     */
    private String bizOrderNo;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 支付渠道
     */
    private String channel;

    /**
     * 商品标题
     */
    private String subject;

    /**
     * 支付金额（元）
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 状态（0待支付 1已支付 2已关闭 3已退款）
     */
    private String status;

    /**
     * 支付用户ID
     */
    private Long userId;

    /**
     * 渠道预支付ID
     */
    private String prePayId;

    /**
     * 二维码链接
     */
    private String codeUrl;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
