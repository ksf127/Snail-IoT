import request, { download } from '@/utils/iotRequest'

/**
 * 查询分组列表（返回数组，非分页）。
 *
 * @param {object} query 查询条件
 * @returns {Promise} 响应 data 为分组列表
 */
export function listGroup(query) {
  return request({ url: '/iot/group/list', method: 'get', params: query })
}

/**
 * 查询分组详情。
 *
 * @param {number|string} groupId 分组ID
 * @returns {Promise} 分组详情
 */
export function getGroup(groupId) {
  return request({ url: `/iot/group/${groupId}`, method: 'get' })
}

/**
 * 新增分组。
 *
 * @param {object} data 分组数据
 * @returns {Promise} 操作结果
 */
export function addGroup(data) {
  return request({ url: '/iot/group', method: 'post', data })
}

/**
 * 修改分组。
 *
 * @param {object} data 分组数据
 * @returns {Promise} 操作结果
 */
export function updateGroup(data) {
  return request({ url: '/iot/group', method: 'put', data })
}

/**
 * 删除分组。
 *
 * @param {Array|string} groupIds 分组ID集合
 * @returns {Promise} 操作结果
 */
export function delGroup(groupIds) {
  return request({ url: `/iot/group/${groupIds}`, method: 'delete' })
}

/**
 * 导出分组 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportGroup(query) {
  return download('/iot/group/export', query)
}