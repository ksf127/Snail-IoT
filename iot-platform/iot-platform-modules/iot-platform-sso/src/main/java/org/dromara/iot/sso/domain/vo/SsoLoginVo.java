package org.dromara.iot.sso.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 单点登录结果
 * <p>
 * 由统一认证中心在「票据换登录态」时签发，包含目标平台可直接使用的访问令牌。
 *
 * @author iot team
 */
@Data
public class SsoLoginVo implements Serializable {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌有效期（秒）
     */
    private Long expireIn;

    /**
     * 客户端标识
     */
    private String clientId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

}
