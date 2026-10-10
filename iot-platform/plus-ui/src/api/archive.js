import request from '@/utils/iotRequest'

/**
 * 分页查询档案列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listArchive(query) {
  return request({ url: '/iot/archive/list', method: 'get', params: query })
}

/**
 * 归档对象存储文件。
 *
 * @param {object} params { archiveType, bizId, ossId }
 * @returns {Promise} 归档记录ID
 */
export function archiveFile(params) {
  return request({ url: '/iot/archive/archive', method: 'post', params })
}
