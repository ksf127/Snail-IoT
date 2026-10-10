package org.dromara.iot.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 单点登录配置
 *
 * @author iot team
 */
@Data
@Component
@ConfigurationProperties(prefix = "iot.sso")
public class SsoProperties {

    /**
     * 统一认证中心对外访问地址（同时作为 OIDC 的 issuer 与 CAS Server 地址）
     */
    private String serverUrl = "http://localhost:8080";

    /**
     * 前端桥接页地址（CAS 未携带登录态时跳转，登录后回跳认证中心换取服务票据）
     */
    private String loginUrl = "http://localhost/cas-login";

    /**
     * 前端授权确认页地址（OAuth2 授权码模式需要用户确认时跳转）
     */
    private String confirmUrl = "http://localhost/oauth2-confirm";

    /**
     * 默认客户端ID（未显式指定时使用，对应 sys_client.client_id）
     */
    private String defaultClientId = "e5cd7e4891bf95d1d19206ce24a7b32e";

    /**
     * 一次性票据有效期（秒）
     */
    private long ticketTimeout = 300;

    /**
     * OIDC 相关配置
     */
    private Oidc oidc = new Oidc();

    /**
     * CAS Client（对接外部 CAS / MaxKey / TOPIAM）配置
     */
    private CasClient client = new CasClient();

    /**
     * OIDC 配置
     */
    @Data
    public static class Oidc {

        /**
         * id_token 有效期（秒）
         */
        private long idTokenTimeout = 3600;

    }

    /**
     * CAS Client 配置
     */
    @Data
    public static class CasClient {

        /**
         * 是否启用对接外部 CAS
         */
        private boolean enabled = false;

        /**
         * 外部 CAS 服务地址
         */
        private String serverUrl;

        /**
         * 本平台在外部 CAS 注册的服务地址（回调地址）
         */
        private String service = "http://localhost:8080/iot/sso/cas-callback";

        /**
         * 外部 CAS 登录地址路径
         */
        private String loginPath = "/login";

        /**
         * 外部 CAS 校验地址路径
         */
        private String validatePath = "/serviceValidate";

    }

}
