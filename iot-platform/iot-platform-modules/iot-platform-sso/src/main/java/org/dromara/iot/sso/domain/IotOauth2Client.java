package org.dromara.iot.sso.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * OAuth2 客户端注册对象 iot_oauth2_client
 * <p>
 * 统一认证中心对外签发票据所需的客户端注册表，驱动 Sa-Token OAuth2 服务端。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_oauth2_client")
public class IotOauth2Client extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户端标识
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 客户端名称
     */
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
    @TableField("is_new_refresh")
    private String newRefresh;

    /**
     * 是否自动授权（1是 0否）
     */
    @TableField("is_auto_confirm")
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
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
