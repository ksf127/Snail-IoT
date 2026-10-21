package org.dromara.iot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 支付商品对象 iot_pay_product
 * <p>
 * 面向虚拟商品（虚拟服务/卡券/会员权益）的可售货架，下单时可按商品取价并锁定标题。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_pay_product")
public class IotPayProduct extends BaseEntity {

    /**
     * 商品ID
     */
    @TableId(value = "product_id", type = IdType.ASSIGN_ID)
    private Long productId;

    /**
     * 商品编号（唯一）
     */
    private String productNo;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品类型（virtual_service/virtual_card/membership）
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
