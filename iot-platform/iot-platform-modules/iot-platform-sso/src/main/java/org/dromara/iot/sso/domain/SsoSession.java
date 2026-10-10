package org.dromara.iot.sso.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 单点登录会话
 * <p>
 * 记录用户在各平台（Web / 移动端等）建立的登录会话，用于「全平台会话」展示与全局登出。
 *
 * @author iot team
 */
@Data
public class SsoSession implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 客户端标识
     */
    private String clientId;

    /**
     * 登录时间（毫秒时间戳）
     */
    private Long loginTime;

    /**
     * 令牌有效期（秒）
     */
    private Long expireIn;

}
