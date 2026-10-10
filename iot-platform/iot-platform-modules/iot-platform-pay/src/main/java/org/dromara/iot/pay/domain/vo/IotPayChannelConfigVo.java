package org.dromara.iot.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.pay.domain.IotPayChannelConfig;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付渠道配置视图对象 iot_pay_channel_config
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotPayChannelConfig.class)
public class IotPayChannelConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置键（唯一）
     */
    private String configKey;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 渠道类型
     */
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
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
