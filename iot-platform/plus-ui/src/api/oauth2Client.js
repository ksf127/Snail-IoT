import request, { download } from '@/utils/iotRequest';

/**
 * 查询 OAuth2 客户端列表。
 *
 * @param {object} query 查询条件（clientId / clientName / status）
 * @returns {Promise} 响应 data 为客户端数组
 */
export function listOauth2Client(query) {
  return request({ url: '/iot/oauth2-client/list', method: 'get', params: query });
}

/**
 * 查询 OAuth2 客户端详情。
 *
 * @param {number|string} id 主键ID
 * @returns {Promise} 客户端详情
 */
export function getOauth2Client(id) {
  return request({ url: `/iot/oauth2-client/${id}`, method: 'get' });
}

/**
 * 新增 OAuth2 客户端。
 *
 * @param {object} data 客户端数据
 * @returns {Promise} 操作结果
 */
export function addOauth2Client(data) {
  return request({ url: '/iot/oauth2-client', method: 'post', data });
}

/**
 * 修改 OAuth2 客户端。
 *
 * @param {object} data 客户端数据
 * @returns {Promise} 操作结果
 */
export function updateOauth2Client(data) {
  return request({ url: '/iot/oauth2-client', method: 'put', data });
}

/**
 * 删除 OAuth2 客户端。
 *
 * @param {Array|string} ids 主键ID集合
 * @returns {Promise} 操作结果
 */
export function delOauth2Client(ids) {
  return request({ url: `/iot/oauth2-client/${ids}`, method: 'delete' });
}

/**
 * 导出 OAuth2 客户端 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportOauth2Client(query) {
  return download('/iot/oauth2-client/export', query);
}
