package org.dromara.iot.sso.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.iot.sso.config.SsoProperties;
import org.dromara.iot.sso.domain.vo.SsoUserInfo;
import org.dromara.iot.sso.service.ISsoLoginSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 / OIDC 服务端
 * <p>
 * 统一认证中心对外暴露的标准协议端点：
 * 授权码 / 隐式 / 密码 / 客户端凭证四种模式，外加 access_token 撤销、OIDC 元数据与 UserInfo。
 * 端点以显式映射转发至 {@link SaOAuth2ServerProcessor}，避免 catch-all 吞掉自定义端点。
 *
 * @author iot team
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
public class OAuth2Controller {

    /**
     * Bearer 令牌前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    private final SsoProperties ssoProperties;

    private final ISsoLoginSupport loginSupport;

    /**
     * 授权端点：authorization_code / implicit
     *
     * @return 授权结果（重定向或令牌）
     */
    @SaIgnore
    @GetMapping("/oauth2/authorize")
    public Object authorize() {
        return SaOAuth2ServerProcessor.instance.authorize();
    }

    /**
     * 令牌端点：authorization_code / password / refresh_token / client_credentials
     *
     * @return 令牌信息
     */
    @SaIgnore
    @PostMapping("/oauth2/token")
    public Object token() {
        return SaOAuth2ServerProcessor.instance.token();
    }

    /**
     * 刷新令牌端点
     *
     * @return 新的令牌信息
     */
    @SaIgnore
    @PostMapping("/oauth2/refresh")
    public Object refresh() {
        return SaOAuth2ServerProcessor.instance.refresh();
    }

    /**
     * 撤销令牌端点
     *
     * @return 操作结果
     */
    @SaIgnore
    @PostMapping("/oauth2/revoke")
    public Object revoke() {
        return SaOAuth2ServerProcessor.instance.revoke();
    }

    /**
     * 客户端凭证令牌端点
     *
     * @return 客户端令牌信息
     */
    @SaIgnore
    @PostMapping("/oauth2/client_token")
    public Object clientToken() {
        return SaOAuth2ServerProcessor.instance.clientToken();
    }

    /**
     * OAuth2 密码模式登录端点（本平台登录由前端登录页完成，此处跳转登录页）
     *
     * @return 授权结果
     */
    @SaIgnore
    @PostMapping("/oauth2/doLogin")
    public Object doLogin() {
        return SaOAuth2ServerProcessor.instance.doLogin();
    }

    /**
     * OAuth2 授权确认端点
     *
     * @return 授权结果
     */
    @SaIgnore
    @PostMapping("/oauth2/doConfirm")
    public Object doConfirm() {
        return SaOAuth2ServerProcessor.instance.doConfirm();
    }

    /**
     * OIDC UserInfo 端点（需携带 access_token）
     *
     * @return 用户信息
     */
    @SaIgnore
    @GetMapping("/oauth2/userinfo")
    public R<SsoUserInfo> userinfo() {
        String accessToken = resolveAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            return R.fail("缺少 access_token");
        }
        AccessTokenModel tokenModel = SaOAuth2Util.getAccessToken(accessToken);
        if (tokenModel == null || tokenModel.getLoginId() == null) {
            return R.fail("access_token 无效或已过期");
        }
        SsoUserInfo userInfo = loginSupport.getUserInfo(Long.valueOf(String.valueOf(tokenModel.getLoginId())));
        if (userInfo == null) {
            return R.fail("用户不存在");
        }
        return R.ok(userInfo);
    }

    /**
     * OIDC 服务端元数据
     *
     * @return 元数据
     */
    @SaIgnore
    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> openidConfiguration() {
        String issuer = ssoProperties.getServerUrl();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("issuer", issuer);
        metadata.put("authorization_endpoint", issuer + "/oauth2/authorize");
        metadata.put("token_endpoint", issuer + "/oauth2/token");
        metadata.put("userinfo_endpoint", issuer + "/oauth2/userinfo");
        metadata.put("revocation_endpoint", issuer + "/oauth2/revoke");
        metadata.put("response_types_supported", List.of("code", "token"));
        metadata.put("grant_types_supported", List.of(
            "authorization_code", "implicit", "password", "client_credentials", "refresh_token"));
        metadata.put("scopes_supported", List.of("openid", "userid", "userinfo"));
        metadata.put("subject_types_supported", List.of("public"));
        metadata.put("id_token_signing_alg_values_supported", List.of("HS256"));
        metadata.put("token_endpoint_auth_methods_supported", List.of("client_secret_post", "client_secret_basic"));
        return metadata;
    }

    /**
     * 解析请求中的 access_token（优先 Authorization: Bearer，其次 access_token 参数）
     *
     * @return access_token，不存在时返回 null
     */
    private String resolveAccessToken() {
        String header = SaHolder.getRequest().getHeader("Authorization");
        if (StrUtil.isNotBlank(header) && StrUtil.startWithIgnoreCase(header, BEARER_PREFIX)) {
            return StrUtil.trim(header.substring(BEARER_PREFIX.length()));
        }
        return SaHolder.getRequest().getParam("access_token");
    }

}
