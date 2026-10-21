package org.dromara.iot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 支付渠道配置对象 iot_pay_channel_config
 * <p>
 * 表驱动多渠道配置（与 sys_oss_config 同范式），支持多商户/多渠道热切换。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_pay_channel_config")
public class IotPayChannelConfig extends BaseEntity {

    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
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
     * 渠道类型（mock/wx_native/ali_qr）
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
     * 网关地址（为空使用渠道默认地址；支付宝沙箱填 https://openapi-sandbox.dl.alipaydev.com/gateway.do）
     */
    private String gatewayUrl;

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
