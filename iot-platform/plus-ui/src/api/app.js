import request, { download } from '@/utils/iotRequest'

/**
 * 分页查询应用列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listApp(query) {
  return request({ url: '/iot/app/list', method: 'get', params: query })
}

/**
 * 查询应用详情。
 *
 * @param {number|string} appId 应用ID
 * @returns {Promise} 应用详情
 */
export function getApp(appId) {
  return request({ url: `/iot/app/${appId}`, method: 'get' })
}

/**
 * 新增应用。
 *
 * @param {object} data 应用数据
 * @returns {Promise} 操作结果
 */
export function addApp(data) {
  return request({ url: '/iot/app', method: 'post', data })
}

/**
 * 修改应用。
 *
 * @param {object} data 应用数据
 * @returns {Promise} 操作结果
 */
export function updateApp(data) {
  return request({ url: '/iot/app', method: 'put', data })
}

/**
 * 删除应用。
 *
 * @param {Array|string} appIds 应用ID集合
 * @returns {Promise} 操作结果
 */
export function delApp(appIds) {
  return request({ url: `/iot/app/${appIds}`, method: 'delete' })
}

/**
 * 导出应用 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportApp(query) {
  return download('/iot/app/export', query)
}

/**
 * 查询应用引用的数据集ID列表。
 *
 * @param {number|string} appId 应用ID
 * @returns {Promise} 数据集ID列表
 */
export function getAppDatasets(appId) {
  return request({ url: `/iot/app/${appId}/datasets`, method: 'get' })
}

/**
 * 保存应用引用的数据集。
 *
 * @param {number|string} appId 应用ID
 * @param {Array} datasetIds 数据集ID列表
 * @returns {Promise} 操作结果
 */
export function saveAppDatasets(appId, datasetIds) {
  return request({ url: `/iot/app/${appId}/datasets`, method: 'put', data: datasetIds })
}

/**
 * 查询应用已授权的角色ID列表。
 *
 * @param {number|string} appId 应用ID
 * @returns {Promise} 角色ID列表
 */
export function getAppRoles(appId) {
  return request({ url: `/iot/app/${appId}/roles`, method: 'get' })
}

/**
 * 保存应用授权的角色。
 *
 * @param {number|string} appId 应用ID
 * @param {Array} roleIds 角色ID列表
 * @returns {Promise} 操作结果
 */
export function saveAppRoles(appId, roleIds) {
  return request({ url: `/iot/app/${appId}/roles`, method: 'put', data: roleIds })
}