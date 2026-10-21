package org.dromara.iot.cloud.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.cloud.domain.IotCloudAccount;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 云账号业务对象 iot_cloud_account
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotCloudAccount.class, reverseConvertGenerate = false)
public class IotCloudAccountBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 云账号ID
     */
    private Long accountId;

    /**
     * 账号名称
     */
    @NotBlank(message = "账号名称不能为空")
    @Size(max = 64, message = "账号名称长度不能超过 64 个字符")
    private String accountName;

    /**
     * 云平台（mock/aliyun/flink）
     */
    @NotBlank(message = "云平台不能为空")
    private String provider;

    /**
     * 默认地域ID
     */
    @Size(max = 64, message = "地域ID长度不能超过 64 个字符")
    private String regionId;

    /**
     * 访问密钥ID
     */
    @Size(max = 128, message = "访问密钥ID长度不能超过 128 个字符")
    private String accessKey;

    /**
     * 访问密钥
     */
    @Size(max = 255, message = "访问密钥长度不能超过 255 个字符")
    private String accessSecret;

    /**
     * 接入端点
     */
    @Size(max = 255, message = "接入端点长度不能超过 255 个字符")
    private String endpoint;

    /**
     * 状态（0启用 1停用）
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
