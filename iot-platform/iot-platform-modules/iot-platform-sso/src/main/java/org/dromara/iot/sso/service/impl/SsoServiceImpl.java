package org.dromara.iot.sso.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.iot.common.constant.IotConstants;
import org.dromara.iot.sso.config.SsoProperties;
import org.dromara.iot.sso.domain.CasTicket;
import org.dromara.iot.sso.domain.SsoSession;
import org.dromara.iot.sso.domain.SsoTicket;
import org.dromara.iot.sso.domain.vo.SsoLoginVo;
import org.dromara.iot.sso.service.ISsoLoginSupport;
import org.dromara.iot.sso.service.ISsoService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 单点登录 服务层实现
 * <p>
 * 票据存放于 Redis，默认 5 分钟有效且仅可使用一次，避免重放攻击；
 * 会话记录用于展示用户在 Web / 移动端等各平台的登录情况，并支撑全局登出。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@Service
public class SsoServiceImpl implements ISsoService {

    /**
     * 票据有效期
     */
    private static final Duration TICKET_TTL = Duration.ofMinutes(5);

    /**
     * 会话记录兜底有效期（无法获取令牌有效期时使用）
     */
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    private final ISsoLoginSupport loginSupport;

    private final SsoProperties ssoProperties;

    /**
     * 为当前登录用户签发一次性 SSO 票据（使用默认客户端）
     *
     * @return 票据信息
     */
    @Override
    public SsoTicket issueTicket() {
        return issueTicket(null);
    }

    /**
     * 为当前登录用户签发一次性 SSO 票据
     *
     * @param clientId 目标客户端标识（为空时使用默认客户端）
     * @return 票据信息
     */
    @Override
    public SsoTicket issueTicket(String clientId) {
        return issueTicketForUser(LoginHelper.getUserId(), LoginHelper.getUsername(), clientId);
    }

    /**
     * 为指定用户签发一次性 SSO 票据
     *
     * @param userId   用户ID
     * @param userName 用户名
     * @param clientId 目标客户端标识（为空时使用默认客户端）
     * @return 票据信息
     */
    @Override
    public SsoTicket issueTicketForUser(Long userId, String userName, String clientId) {
        SsoTicket ticket = new SsoTicket();
        ticket.setTicket(IdUtil.fastSimpleUUID());
        ticket.setUserId(userId);
        ticket.setUserName(userName);
        ticket.setClientId(StringUtils.blankToDefault(clientId, ssoProperties.getDefaultClientId()));
        ticket.setIssueTime(System.currentTimeMillis());
        RedisUtils.setCacheObject(IotConstants.SSO_TICKET_PREFIX + ticket.getTicket(), ticket, TICKET_TTL);
        return ticket;
    }

    /**
     * 签发 CAS 服务票据（Service Ticket）
     *
     * @param service 票据绑定的服务地址
     * @return 服务票据信息
     */
    @Override
    public CasTicket issueCasTicket(String service) {
        CasTicket ticket = new CasTicket();
        ticket.setTicket("ST-" + IdUtil.fastSimpleUUID());
        ticket.setService(service);
        ticket.setUserId(LoginHelper.getUserId());
        ticket.setUserName(LoginHelper.getUsername());
        ticket.setIssueTime(System.currentTimeMillis());
        RedisUtils.setCacheObject(IotConstants.SSO_CAS_TICKET_PREFIX + ticket.getTicket(), ticket,
            Duration.ofSeconds(ssoProperties.getTicketTimeout()));
        return ticket;
    }

    /**
     * 校验并消费 CAS 服务票据（一次性，且校验 service 一致）
     *
     * @param ticket  服务票据
     * @param service 校验时携带的服务地址（为空时不校验）
     * @return 服务票据信息，无效、已过期或 service 不一致时返回 null
     */
    @Override
    public CasTicket consumeCasTicket(String ticket, String service) {
        if (StrUtil.isBlank(ticket)) {
            return null;
        }
        String key = IotConstants.SSO_CAS_TICKET_PREFIX + ticket;
        CasTicket cached = RedisUtils.getCacheObject(key);
        if (cached == null) {
            return null;
        }
        RedisUtils.deleteObject(key);
        if (StrUtil.isNotBlank(service) && !StrUtil.equals(service, cached.getService())) {
            return null;
        }
        return cached;
    }

    /**
     * 校验并消费票据（一次性，校验成功后立即失效）
     *
     * @param ticket 票据
     * @return 票据信息，无效或已过期时返回 null
     */
    @Override
    public SsoTicket consumeTicket(String ticket) {
        if (StrUtil.isBlank(ticket)) {
            return null;
        }
        String key = IotConstants.SSO_TICKET_PREFIX + ticket;
        SsoTicket cached = RedisUtils.getCacheObject(key);
        if (cached != null) {
            RedisUtils.deleteObject(key);
        }
        return cached;
    }

    /**
     * 票据换登录态：消费票据并为票据所属用户签发目标平台登录态
     *
     * @param ticket   一次性票据
     * @param clientId 目标客户端标识（为空时使用票据中记录的客户端）
     * @return 登录结果
     */
    @Override
    public SsoLoginVo exchange(String ticket, String clientId) {
        SsoTicket ssoTicket = consumeTicket(ticket);
        if (ssoTicket == null) {
            throw new ServiceException("票据无效或已过期");
        }
        String targetClient = StringUtils.blankToDefault(clientId, ssoTicket.getClientId());
        SsoLoginVo loginVo = loginSupport.loginByUserId(ssoTicket.getUserId(), targetClient);
        loginVo.setUserId(ssoTicket.getUserId());
        loginVo.setUserName(ssoTicket.getUserName());
        recordSession(ssoTicket, loginVo);
        return loginVo;
    }

    /**
     * 查询当前用户在各平台的登录会话
     *
     * @return 会话列表（按登录时间倒序）
     */
    @Override
    public List<SsoSession> listSessions() {
        Long userId = LoginHelper.getUserId();
        Collection<String> keys = RedisUtils.keys(IotConstants.SSO_SESSION_PREFIX + userId + ":*");
        List<SsoSession> sessions = new ArrayList<>();
        if (CollUtil.isEmpty(keys)) {
            return sessions;
        }
        for (String key : keys) {
            SsoSession session = RedisUtils.getCacheObject(key);
            if (session != null) {
                sessions.add(session);
            } else {
                RedisUtils.deleteObject(key);
            }
        }
        sessions.sort(Comparator.comparing(SsoSession::getLoginTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return sessions;
    }

    /**
     * 全局登出：注销当前用户在全部平台的登录会话与令牌
     */
    @Override
    public void logoutAll() {
        Long userId = LoginHelper.getUserId();
        Collection<String> keys = RedisUtils.keys(IotConstants.SSO_SESSION_PREFIX + userId + ":*");
        if (CollUtil.isNotEmpty(keys)) {
            RedisUtils.deleteObject(new ArrayList<>(keys));
        }
        StpUtil.logout(userId);
    }

    /**
     * 记录登录会话
     *
     * @param ticket  票据
     * @param loginVo 登录结果
     */
    private void recordSession(SsoTicket ticket, SsoLoginVo loginVo) {
        String clientId = StringUtils.blankToDefault(loginVo.getClientId(), ticket.getClientId());
        SsoSession session = new SsoSession();
        session.setUserId(ticket.getUserId());
        session.setUserName(ticket.getUserName());
        session.setClientId(clientId);
        session.setLoginTime(System.currentTimeMillis());
        session.setExpireIn(loginVo.getExpireIn());
        Duration ttl = (loginVo.getExpireIn() != null && loginVo.getExpireIn() > 0)
            ? Duration.ofSeconds(loginVo.getExpireIn()) : SESSION_TTL;
        RedisUtils.setCacheObject(IotConstants.SSO_SESSION_PREFIX + ticket.getUserId() + ":" + clientId, session, ttl);
    }

}
