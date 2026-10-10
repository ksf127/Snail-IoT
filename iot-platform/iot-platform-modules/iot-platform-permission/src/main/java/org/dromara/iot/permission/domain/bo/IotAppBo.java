package org.dromara.iot.permission.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.permission.domain.IotApp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用业务对象 iot_app
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotApp.class, reverseConvertGenerate = false)
public class IotAppBo implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 应用编码
     */
    @NotBlank(message = "应用编码不能为空")
    @Size(max = 64, message = "应用编码长度不能超过 64 个字符")
    private String appCode;

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称长度不能超过 128 个字符")
    private String appName;

    /**
     * 应用类型 WEB / APP / MINI / API
     */
    private String appType;

    /**
     * 访问地址
     */
    private String accessUrl;

    /**
     * 应用密钥
     */
    private String appSecret;

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
