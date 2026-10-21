package org.dromara.iot.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayProduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付商品视图对象 iot_pay_product
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayProduct.class)
public class IotPayProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编号
     */
    private String productNo;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品类型
     */
    private String productType;

    /**
     * 售价（元）
     */
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
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
