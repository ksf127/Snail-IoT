import request, { download } from '@/utils/iotRequest'

/**
 * 分页查询设备列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listDevice(query) {
  return request({ url: '/iot/device/list', method: 'get', params: query })
}

/**
 * 查询设备详情。
 *
 * @param {number|string} deviceId 设备ID
 * @returns {Promise} 设备详情
 */
export function getDevice(deviceId) {
  return request({ url: `/iot/device/${deviceId}`, method: 'get' })
}

/**
 * 新增设备。
 *
 * @param {object} data 设备数据
 * @returns {Promise} 操作结果
 */
export function addDevice(data) {
  return request({ url: '/iot/device', method: 'post', data })
}

/**
 * 修改设备。
 *
 * @param {object} data 设备数据
 * @returns {Promise} 操作结果
 */
export function updateDevice(data) {
  return request({ url: '/iot/device', method: 'put', data })
}

/**
 * 删除设备。
 *
 * @param {Array|string} deviceIds 设备ID集合
 * @returns {Promise} 操作结果
 */
export function delDevice(deviceIds) {
  return request({ url: `/iot/device/${deviceIds}`, method: 'delete' })
}

/**
 * 设备在线状态统计。
 *
 * @returns {Promise} 响应 data 为 { total, online, offline, unknown }
 */
export function deviceStatistics() {
  return request({ url: '/iot/device/statistics', method: 'get' })
}

/**
 * 导出设备 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportDevice(query) {
  return download('/iot/device/export', query)
}
