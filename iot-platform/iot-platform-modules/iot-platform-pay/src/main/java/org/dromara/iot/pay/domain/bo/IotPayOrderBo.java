package org.dromara.iot.pay.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付订单业务对象 iot_pay_order
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayOrder.class, reverseConvertGenerate = false)
public class IotPayOrderBo implements Serializable {

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
    @Size(max = 64, message = "业务订单号长度不能超过 64 个字符")
    private String bizOrderNo;

    /**
     * 支付渠道
     */
    @NotBlank(message = "支付渠道不能为空")
    private String channel;

    /**
     * 商品标题
     */
    @Size(max = 255, message = "商品标题长度不能超过 255 个字符")
    private String subject;

    /**
     * 支付金额（元）
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于 0")
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
