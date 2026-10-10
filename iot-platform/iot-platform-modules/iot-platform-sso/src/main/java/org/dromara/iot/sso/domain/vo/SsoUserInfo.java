package org.dromara.iot.sso.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * OIDC UserInfo 返回的用户信息
 *
 * @author iot team
 */
@Data
public class SsoUserInfo implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

}
