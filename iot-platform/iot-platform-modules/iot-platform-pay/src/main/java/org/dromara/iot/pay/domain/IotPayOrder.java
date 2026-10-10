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
 * 支付订单对象 iot_pay_order
 * <p>
 * 状态机：0待支付 -> 1已支付 -> 3已退款；0待支付 -> 2已关闭（超时/取消）。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_pay_order")
public class IotPayOrder extends BaseEntity {

    /**
     * 支付单ID
     */
    @TableId(value = "order_id", type = IdType.ASSIGN_ID)
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
     * 支付渠道（mock/wx_native/ali_qr）
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
     * 支付回调原文
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
