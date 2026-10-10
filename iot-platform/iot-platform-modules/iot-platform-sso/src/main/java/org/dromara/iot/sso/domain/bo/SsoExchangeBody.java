package org.dromara.iot.sso.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 票据换登录态请求体
 *
 * @author iot team
 */
@Data
public class SsoExchangeBody implements Serializable {

    /**
     * 一次性票据
     */
    @NotBlank(message = "票据不能为空")
    private String ticket;

    /**
     * 目标客户端标识（为空时使用票据中记录的客户端）
     */
    private String clientId;

}
