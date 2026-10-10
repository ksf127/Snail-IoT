package org.dromara.iot.sso.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * CAS 服务票据（Service Ticket，ST）
 * <p>
 * 由本平台作为 CAS Server 对外签发，绑定访问的服务地址（service）；
 * 客户端携带 ST 调用 /cas/serviceValidate 校验，一次性使用且校验 service 一致，防止票据被挪用。
 *
 * @author iot team
 */
@Data
public class CasTicket implements Serializable {

    /**
     * 服务票据（形如 ST-xxxxxxxx）
     */
    private String ticket;

    /**
     * 票据绑定的服务地址
     */
    private String service;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 票据签发毫秒时间戳
     */
    private Long issueTime;

}
