package org.dromara.iot.sso.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 单点登录票据
 * <p>
 * 由统一认证中心签发，其它平台携带该票据换取登录态，实现全平台免密跳转。
 *
 * @author iot team
 */
@Data
public class SsoTicket implements Serializable {

    /**
     * 一次性票据
     */
    private String ticket;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 目标客户端标识（对应 sys_client.client_id）
     */
    private String clientId;

    /**
     * 票据签发毫秒时间戳
     */
    private Long issueTime;

}
