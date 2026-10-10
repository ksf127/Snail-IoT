package org.dromara.iot.device.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.device.domain.IotProduct;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品业务对象 iot_product
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotProduct.class, reverseConvertGenerate = false)
public class IotProductBo implements Serializable {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空")
    @Size(max = 64, message = "产品编码长度不能超过 64 个字符")
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    @Size(max = 128, message = "产品名称长度不能超过 128 个字符")
    private String productName;

    /**
     * 接入协议 MQTT / HTTPS / MODBUS / OTHER
     */
    @NotBlank(message = "接入协议不能为空")
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
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
