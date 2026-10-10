package org.dromara.iot.sso.service;

import org.dromara.iot.sso.domain.CasTicket;
import org.dromara.iot.sso.domain.SsoSession;
import org.dromara.iot.sso.domain.SsoTicket;
import org.dromara.iot.sso.domain.vo.SsoLoginVo;

import java.util.List;

/**
 * 单点登录 服务层
 *
 * @author iot team
 */
public interface ISsoService {

    /**
     * 为当前登录用户签发一次性 SSO 票据（使用默认客户端）
     *
     * @return 票据信息
     */
    SsoTicket issueTicket();

    /**
     * 为当前登录用户签发一次性 SSO 票据
     *
     * @param clientId 目标客户端标识（为空时使用默认客户端）
     * @return 票据信息
     */
    SsoTicket issueTicket(String clientId);

    /**
     * 为指定用户签发一次性 SSO 票据
     * <p>
     * 与 {@link #issueTicket(String)} 的区别：不依赖当前登录态，用于 CAS 客户端回调等
     * 「已通过外部认证但当前请求无本平台登录态」的场景。
     *
     * @param userId   用户ID
     * @param userName 用户名
     * @param clientId 目标客户端标识（为空时使用默认客户端）
     * @return 票据信息
     */
    SsoTicket issueTicketForUser(Long userId, String userName, String clientId);

    /**
     * 签发 CAS 服务票据（Service Ticket）
     *
     * @param service 票据绑定的服务地址
     * @return 服务票据信息
     */
    CasTicket issueCasTicket(String service);

    /**
     * 校验并消费 CAS 服务票据（一次性，且校验 service 一致）
     *
     * @param ticket  服务票据
     * @param service 校验时携带的服务地址（为空时不校验）
     * @return 服务票据信息，无效、已过期或 service 不一致时返回 null
     */
    CasTicket consumeCasTicket(String ticket, String service);

    /**
     * 校验并消费票据（一次性，校验成功后立即失效）
     *
     * @param ticket 票据
     * @return 票据信息，无效或已过期时返回 null
     */
    SsoTicket consumeTicket(String ticket);

    /**
     * 票据换登录态：消费票据并为票据所属用户签发目标平台登录态
     *
     * @param ticket   一次性票据
     * @param clientId 目标客户端标识（为空时使用票据中记录的客户端）
     * @return 登录结果
     */
    SsoLoginVo exchange(String ticket, String clientId);

    /**
     * 查询当前用户在各平台的登录会话
     *
     * @return 会话列表
     */
    List<SsoSession> listSessions();

    /**
     * 全局登出：注销当前用户在全部平台的登录会话与令牌
     */
    void logoutAll();

}
