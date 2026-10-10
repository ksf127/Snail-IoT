package org.dromara.iot.sso.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.sso.domain.IotOauth2Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 客户端注册业务对象 iot_oauth2_client
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotOauth2Client.class, reverseConvertGenerate = false)
public class IotOauth2ClientBo implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端标识
     */
    @NotBlank(message = "客户端标识不能为空")
    @Size(max = 64, message = "客户端标识长度不能超过 64 个字符")
    private String clientId;

    /**
     * 客户端密钥
     */
    @NotBlank(message = "客户端密钥不能为空")
    @Size(max = 128, message = "客户端密钥长度不能超过 128 个字符")
    private String clientSecret;

    /**
     * 客户端名称
     */
    @Size(max = 128, message = "客户端名称长度不能超过 128 个字符")
    private String clientName;

    /**
     * 签约的 scope 列表（逗号分隔）
     */
    private String contractScopes;

    /**
     * 允许的重定向地址（逗号分隔）
     */
    private String allowRedirectUris;

    /**
     * 允许的授权类型（逗号分隔）
     */
    private String allowGrantTypes;

    /**
     * 授权对象（为空时默认取当前登录用户ID）
     */
    private String subjectId;

    /**
     * AccessToken 有效期（秒）
     */
    private Long accessTokenTimeout;

    /**
     * RefreshToken 有效期（秒）
     */
    private Long refreshTokenTimeout;

    /**
     * ClientToken 有效期（秒）
     */
    private Long clientTokenTimeout;

    /**
     * AccessToken 最大保留数量
     */
    private Integer maxAccessTokenCount;

    /**
     * RefreshToken 最大保留数量
     */
    private Integer maxRefreshTokenCount;

    /**
     * ClientToken 最大保留数量
     */
    private Integer maxClientTokenCount;

    /**
     * 每次刷新是否重新生成 RefreshToken（1是 0否）
     */
    private String newRefresh;

    /**
     * 是否自动授权（1是 0否）
     */
    private String autoConfirm;

    /**
     * 适配设备类型（pc/android/ios/h5）
     */
    private String deviceType;

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
