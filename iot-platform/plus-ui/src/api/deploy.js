import request from '@/utils/iotRequest'

/**
 * 分页查询部署任务。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listDeploy(query) {
  return request({ url: '/iot/deploy/list', method: 'get', params: query })
}

/**
 * 触发部署发布。
 *
 * @param {object} params { appCode, version, targetEnv }
 * @returns {Promise} 部署任务ID
 */
export function releaseDeploy(params) {
  return request({ url: '/iot/deploy/release', method: 'post', params })
}

/**
 * 查询部署日志。
 *
 * @param {number|string} taskId 部署任务ID
 * @returns {Promise} 部署日志
 */
export function deployLog(taskId) {
  return request({ url: `/iot/deploy/${taskId}/log`, method: 'get' })
}
