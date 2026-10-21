import request from '@/utils/iotRequest';

/**
 * 查询当前用户全平台登录会话。
 *
 * @returns {Promise} 响应 data 为会话数组
 */
export function listSsoSessions() {
  return request({ url: '/iot/sso/sessions', method: 'get' });
}

/**
 * 全局登出：注销当前用户全部平台会话。
 *
 * @returns {Promise} 操作结果
 */
export function ssoLogout() {
  return request({ url: '/iot/sso/logout', method: 'delete' });
}
