package org.dromara.iot.sso.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.iot.sso.domain.SsoSession;
import org.dromara.iot.sso.domain.SsoTicket;
import org.dromara.iot.sso.domain.bo.SsoExchangeBody;
import org.dromara.iot.sso.domain.vo.SsoLoginVo;
import org.dromara.iot.sso.service.ISsoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统一认证中心（SSO）
 * <p>
 * 典型流程：
 * 1. 用户在统一认证中心登录后调用 /iot/sso/ticket 获取一次性票据；
 * 2. 携带票据跳转目标平台，目标平台调用 /iot/sso/exchange 换取登录态（访问令牌）。
 *
 * @author iot team
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/iot/sso")
public class SsoController {

    private final ISsoService ssoService;

    /**
     * 签发 SSO 票据（需已登录）
     *
     * @param clientId 目标客户端标识（可选）
     * @return 一次性票据
     */
    @SaCheckLogin
    @PostMapping("/ticket")
    public R<SsoTicket> ticket(@RequestParam(required = false) String clientId) {
        return R.ok(ssoService.issueTicket(clientId));
    }

    /**
     * 票据换登录态
     *
     * @param body 票据与目标客户端
     * @return 目标平台登录结果（访问令牌）
     */
    @SaIgnore
    @PostMapping("/exchange")
    public R<SsoLoginVo> exchange(@Validated @RequestBody SsoExchangeBody body) {
        return R.ok(ssoService.exchange(body.getTicket(), body.getClientId()));
    }

    /**
     * 校验并消费 SSO 票据（不签发登录态，仅供平台探活）
     *
     * @param ticket 票据
     * @return 票据对应的用户信息
     */
    @SaIgnore
    @GetMapping("/validate")
    public R<SsoTicket> validate(@RequestParam String ticket) {
        SsoTicket ssoTicket = ssoService.consumeTicket(ticket);
        if (ssoTicket == null) {
            return R.fail("票据无效或已过期");
        }
        return R.ok(ssoTicket);
    }

    /**
     * 查询当前用户全平台登录会话
     *
     * @return 会话列表
     */
    @SaCheckLogin
    @GetMapping("/sessions")
    public R<List<SsoSession>> sessions() {
        return R.ok(ssoService.listSessions());
    }

    /**
     * 全局登出：注销当前用户全部平台会话
     *
     * @return 操作结果
     */
    @SaCheckPermission("iot:sso:logout")
    @Log(title = "SSO全局登出", businessType = BusinessType.FORCE)
    @DeleteMapping("/logout")
    public R<Void> logout() {
        ssoService.logoutAll();
        return R.ok("已退出全部平台会话");
    }

}
