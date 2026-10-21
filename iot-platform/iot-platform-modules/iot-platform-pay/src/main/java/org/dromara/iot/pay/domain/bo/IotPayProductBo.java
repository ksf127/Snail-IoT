package org.dromara.iot.pay.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayProduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付商品业务对象 iot_pay_product
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayProduct.class, reverseConvertGenerate = false)
public class IotPayProductBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编号（唯一，留空自动生成）
     */
    @Size(max = 64, message = "商品编号长度不能超过 64 个字符")
    private String productNo;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 128, message = "商品名称长度不能超过 128 个字符")
    private String productName;

    /**
     * 商品类型
     */
    @NotBlank(message = "商品类型不能为空")
    private String productType;

    /**
     * 售价（元）
     */
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    /**
     * 币种
     */
    private String currency;

    /**
     * 状态（0上架 1下架）
     */
    private String status;

    /**
     * 商品描述
     */
    @Size(max = 500, message = "商品描述长度不能超过 500 个字符")
    private String description;

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
