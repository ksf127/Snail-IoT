package org.dromara.iot.sso.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2OidcConfig;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.hutool.core.util.URLUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

/**
 * 统一认证中心 OAuth2 / OIDC 服务端配置
 * <p>
 * 将数据库驱动的客户端注册表与 Sa-Token OAuth2 服务端对接，并注入三类视图函数：
 * 未登录视图（跳转前端登录页）、授权确认视图（跳转前端确认页）、OAuth2 密码模式登录处理。
 *
 * @author iot team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class IotOAuth2Configuration {

    /**
     * 授权码有效期（秒）
     */
    private static final long CODE_TIMEOUT = 300L;

    /**
     * AccessToken 默认有效期（秒，2 小时）
     */
    private static final long ACCESS_TOKEN_TIMEOUT = 7200L;

    /**
     * RefreshToken 默认有效期（秒，30 天）
     */
    private static final long REFRESH_TOKEN_TIMEOUT = 2592000L;

    /**
     * ClientToken 默认有效期（秒，2 小时）
     */
    private static final long CLIENT_TOKEN_TIMEOUT = 7200L;

    /**
     * 单个客户端最大 token 保留数量
     */
    private static final int MAX_TOKEN_COUNT = 10;

    /**
     * 免二次确认的 scope（与加载器保持一致）
     */
    private static final String LOWER_SCOPE = "openid,userid,userinfo";

    private final SsoProperties ssoProperties;

    private final DbSaOAuth2DataLoader dataLoader;

    /**
     * 初始化 Sa-Token OAuth2 服务端
     */
    @PostConstruct
    public void init() {
        SaOAuth2ServerConfig config = new SaOAuth2ServerConfig()
            .setEnableAuthorizationCode(true)
            .setEnableImplicit(true)
            .setEnablePassword(true)
            .setEnableClientCredentials(true)
            .setCodeTimeout(CODE_TIMEOUT)
            .setAccessTokenTimeout(ACCESS_TOKEN_TIMEOUT)
            .setRefreshTokenTimeout(REFRESH_TOKEN_TIMEOUT)
            .setClientTokenTimeout(CLIENT_TOKEN_TIMEOUT)
            .setMaxAccessTokenCount(MAX_TOKEN_COUNT)
            .setMaxRefreshTokenCount(MAX_TOKEN_COUNT)
            .setMaxClientTokenCount(MAX_TOKEN_COUNT)
            .setIsNewRefresh(true)
            .setMode4ReturnAccessToken(false)
            .setHideStatusField(false)
            .setHigherScope("")
            .setLowerScope(LOWER_SCOPE)
            .setOidc(new SaOAuth2OidcConfig()
                .setIss(ssoProperties.getServerUrl())
                .setIdTokenTimeout(ssoProperties.getOidc().getIdTokenTimeout()))
            .setClients(new LinkedHashMap<>());
        SaOAuth2Manager.setServerConfig(config);
        SaOAuth2Manager.setDataLoader(dataLoader);
        registerViewFunctions();
        log.info("统一认证中心 OAuth2/OIDC 服务端初始化完成，issuer={}", ssoProperties.getServerUrl());
    }

    /**
     * 注册三类视图函数
     */
    private void registerViewFunctions() {
        // 未登录：跳转前端登录页，登录后再回到原授权请求
        SaOAuth2Strategy.instance.notLoginView = () -> {
            redirectToLoginPage();
            return null;
        };
        // 需要用户确认授权：跳转前端确认页
        SaOAuth2Strategy.instance.confirmView = (clientId, scopes) -> {
            String confirmUrl = ssoProperties.getConfirmUrl()
                + "?client_id=" + URLUtil.encode(clientId)
                + "&scope=" + URLUtil.encode(String.join(",", scopes))
                + "&redirect_uri=" + URLUtil.encode(SaHolder.getRequest().getUrl());
            SaHolder.getResponse().redirect(confirmUrl);
            return null;
        };
        // OAuth2 密码模式登录：本平台登录态由前端登录页完成，此处优雅降级为跳转登录页
        SaOAuth2Strategy.instance.doLoginHandle = (name, pwd) -> {
            redirectToLoginPage();
            return null;
        };
    }

    /**
     * 跳转前端登录页并携带回跳地址
     */
    private void redirectToLoginPage() {
        String redirectUri = URLUtil.encode(SaHolder.getRequest().getUrl());
        SaHolder.getResponse().redirect(ssoProperties.getLoginUrl() + "?redirect_uri=" + redirectUri);
    }

}
