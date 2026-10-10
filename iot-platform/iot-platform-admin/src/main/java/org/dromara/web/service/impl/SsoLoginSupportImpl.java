package org.dromara.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.iot.sso.domain.vo.SsoLoginVo;
import org.dromara.iot.sso.domain.vo.SsoUserInfo;
import org.dromara.iot.sso.service.ISsoLoginSupport;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysClientService;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 单点登录 登录态支撑实现
 * <p>
 * 位于承载登录能力的 iot-platform-admin 模块，向 iot-platform-sso 统一认证中心反向提供
 * 「以指定用户签发本平台登录态」的能力，复用既有的
 * {@link SysLoginService} / {@link IAuthStrategy}，保证与账号密码登录链路完全一致。
 *
 * @author iot team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoLoginSupportImpl implements ISsoLoginSupport {

    private final SysLoginService loginService;

    private final SysUserMapper userMapper;

    private final ISysClientService clientService;

    /**
     * 以指定用户身份签发本平台登录态
     *
     * @param userId   用户ID
     * @param clientId 客户端标识（对应 sys_client.client_id）
     * @return 登录结果（含访问令牌）
     */
    @Override
    public SsoLoginVo loginByUserId(Long userId, String clientId) {
        SysUserVo user = userMapper.lambda().eq(SysUser::getUserId, userId).voOne();
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException("单点登录用户不存在：" + userId);
        }
        if (SystemConstants.DISABLE.equals(user.getStatus())) {
            throw new ServiceException("单点登录用户已被停用：" + user.getUserName());
        }
        SysClientVo client = clientService.queryByClientId(clientId);
        if (ObjectUtil.isNull(client)) {
            throw new ServiceException("单点登录客户端不存在：" + clientId);
        }

        LoginUser loginUser = loginService.buildLoginUser(user);
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginParameter model = IAuthStrategy.buildLoginParameter(client);
        LoginHelper.login(loginUser, model);

        SsoLoginVo loginVo = new SsoLoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        loginVo.setUserId(user.getUserId());
        loginVo.setUserName(user.getUserName());
        return loginVo;
    }

    /**
     * 按用户名查询用户ID
     *
     * @param userName 用户名
     * @return 用户ID，不存在时返回 null
     */
    @Override
    public Long findUserIdByUserName(String userName) {
        SysUserVo user = userMapper.lambda().eq(SysUser::getUserName, userName).voOne();
        return ObjectUtil.isNull(user) ? null : user.getUserId();
    }

    /**
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，不存在时返回 null
     */
    @Override
    public SsoUserInfo getUserInfo(Long userId) {
        SysUserVo user = userMapper.lambda().eq(SysUser::getUserId, userId).voOne();
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        SsoUserInfo userInfo = new SsoUserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUserName());
        userInfo.setNickName(user.getNickName());
        userInfo.setDeptId(user.getDeptId());
        userInfo.setDeptName(user.getDeptName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhoneNumber());
        return userInfo;
    }

}
