package org.dromara.iot.pay.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayChannelConfig;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付渠道配置业务对象 iot_pay_channel_config
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayChannelConfig.class, reverseConvertGenerate = false)
public class IotPayChannelConfigBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置键（唯一）
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 64, message = "配置键长度不能超过 64 个字符")
    private String configKey;

    /**
     * 配置名称
     */
    @Size(max = 128, message = "配置名称长度不能超过 128 个字符")
    private String configName;

    /**
     * 渠道类型
     */
    @NotBlank(message = "渠道类型不能为空")
    private String channel;

    /**
     * 商户号
     */
    private String merchantId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String privateKey;

    /**
     * 平台公钥
     */
    private String publicKey;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 证书路径
     */
    private String certPath;

    /**
     * 异步回调地址
     */
    private String notifyUrl;

    /**
     * 状态（0正常 1停用）
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
