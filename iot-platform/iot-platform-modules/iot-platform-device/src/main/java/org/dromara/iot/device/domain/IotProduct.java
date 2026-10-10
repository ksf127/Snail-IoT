package org.dromara.iot.device.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 产品对象 iot_product
 * <p>
 * 产品是设备的模板，承载接入协议与物模型定义。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_product")
public class IotProduct extends BaseEntity {

    /**
     * 产品ID
     */
    @TableId(value = "product_id", type = IdType.ASSIGN_ID)
    private Long productId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 接入协议 MQTT / HTTPS / MODBUS / OTHER
     */
    private String protocol;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 数据格式 JSON / PASS_THROUGH
     */
    private String dataFormat;

    /**
     * 物模型定义（JSON 字符串）
     */
    private String thingModel;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

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
