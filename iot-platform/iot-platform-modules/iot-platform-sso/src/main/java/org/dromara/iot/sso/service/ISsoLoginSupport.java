package org.dromara.iot.sso.service;

import org.dromara.iot.sso.domain.vo.SsoLoginVo;
import org.dromara.iot.sso.domain.vo.SsoUserInfo;

/**
 * 单点登录 登录态支撑 SPI
 * <p>
 * 统一认证中心（iot-platform-sso）位于业务底层，无法依赖承载登录能力的 iot-platform-admin 模块，
 * 因此通过本接口反向解耦：由 iot-platform-admin 提供实现，委托既有的
 * {@code SysLoginService} / {@code IAuthStrategy} 完成真实的登录态签发。
 *
 * @author iot team
 */
public interface ISsoLoginSupport {

    /**
     * 以指定用户身份签发本平台登录态
     *
     * @param userId   用户ID
     * @param clientId 客户端标识（对应 sys_client.client_id）
     * @return 登录结果（含访问令牌）
     */
    SsoLoginVo loginByUserId(Long userId, String clientId);

    /**
     * 按用户名查询用户ID（用于 CAS 客户端回调时将外部账号映射为本地用户）
     *
     * @param userName 用户名
     * @return 用户ID，不存在时返回 null
     */
    Long findUserIdByUserName(String userName);

    /**
     * 查询用户信息（用于 OIDC UserInfo 端点 / OAuth2 资源服务）
     *
     * @param userId 用户ID
     * @return 用户信息，不存在时返回 null
     */
    SsoUserInfo getUserInfo(Long userId);

}
