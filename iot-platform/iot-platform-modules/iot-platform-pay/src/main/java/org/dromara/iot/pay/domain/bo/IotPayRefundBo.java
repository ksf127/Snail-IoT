package org.dromara.iot.pay.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayRefund;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付退款单业务对象 iot_pay_refund
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayRefund.class, reverseConvertGenerate = false)
public class IotPayRefundBo implements Serializable {

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
    @NotBlank(message = "支付单号不能为空")
    private String orderNo;

    /**
     * 退款金额（元）
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @Size(max = 255, message = "退款原因长度不能超过 255 个字符")
    private String reason;

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
