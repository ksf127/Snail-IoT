package org.dromara.iot.sso.config;

import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoaderDefaultImpl;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.iot.sso.domain.vo.IotOauth2ClientVo;
import org.dromara.iot.sso.service.IIotOauth2ClientService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库驱动的 OAuth2 客户端注册加载器
 * <p>
 * 覆盖默认实现，改为从 {@code iot_oauth2_client} 表（含缓存）读取客户端注册信息，
 * 其余能力（openid / unionid / scope 列表）沿用 Sa-Token 默认实现。
 *
 * @author iot team
 */
@Component
@RequiredArgsConstructor
public class DbSaOAuth2DataLoader extends SaOAuth2DataLoaderDefaultImpl {

    /**
     * 免二次确认的标准 scope（openid / userid / userinfo 等基础授权无需用户再次确认）
     */
    private static final List<String> LOWER_SCOPES = List.of("openid", "userid", "userinfo");

    private final IIotOauth2ClientService oauth2ClientService;

    /**
     * 按客户端标识加载客户端注册信息
     *
     * @param clientId 客户端标识
     * @return 客户端注册信息，不存在或已停用时返回 null
     */
    @Override
    public SaClientModel getClientModel(String clientId) {
        if (StrUtil.isBlank(clientId)) {
            return null;
        }
        IotOauth2ClientVo client = oauth2ClientService.queryByClientId(clientId);
        if (client == null || !SystemConstants.NORMAL.equals(client.getStatus())) {
            return null;
        }
        SaClientModel model = new SaClientModel();
        model.setClientId(client.getClientId());
        model.setClientSecret(client.getClientSecret());
        model.setContractScopes(StrUtil.splitTrim(client.getContractScopes(), ','));
        model.setAllowRedirectUris(StrUtil.splitTrim(client.getAllowRedirectUris(), ','));
        model.setAllowGrantTypes(StrUtil.splitTrim(client.getAllowGrantTypes(), ','));
        model.setSubjectId(client.getSubjectId());
        model.setIsNewRefresh("1".equals(client.getNewRefresh()));
        model.setIsAutoConfirm("1".equals(client.getAutoConfirm()));
        return model;
    }

    /**
     * 免二次确认的 scope 列表
     *
     * @return scope 列表
     */
    @Override
    public List<String> getLowerScopeList() {
        return LOWER_SCOPES;
    }

}
