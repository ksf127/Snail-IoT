import request from '@/utils/iotRequest';

/* ------------------------------------------------------------------
 * 任务管理（自建调度 LOCAL + SnailJob 分布式调度 SNAILJOB）
 * ------------------------------------------------------------------ */

/**
 * 分页查询任务列表。
 *
 * @param {object} query 查询条件（jobName/jobGroup/jobType/triggerType/status）+ 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listJobInfos(query) {
  return request({ url: '/iot/job/info/list', method: 'get', params: query });
}

/**
 * 查询任务详情。
 *
 * @param {string|number} jobId 任务ID
 * @returns {Promise} 任务详情
 */
export function getJobInfo(jobId) {
  return request({ url: `/iot/job/info/${jobId}`, method: 'get' });
}

/**
 * 新增任务。
 *
 * @param {object} data 任务表单
 * @returns {Promise}
 */
export function addJobInfo(data) {
  return request({ url: '/iot/job/info', method: 'post', data });
}

/**
 * 修改任务。
 *
 * @param {object} data 任务表单
 * @returns {Promise}
 */
export function updateJobInfo(data) {
  return request({ url: '/iot/job/info', method: 'put', data });
}

/**
 * 删除任务（级联删除其执行日志）。
 *
 * @param {string|number|Array} jobIds 任务ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delJobInfo(jobIds) {
  return request({ url: `/iot/job/info/${jobIds}`, method: 'delete' });
}

/**
 * 启用/停用任务。
 *
 * @param {string|number} jobId 任务ID
 * @param {string} status 状态（0启用 1停用）
 * @returns {Promise}
 */
export function changeJobStatus(jobId, status) {
  return request({ url: `/iot/job/info/changeStatus/${jobId}/${status}`, method: 'put' });
}

/**
 * 手动触发一次任务。
 *
 * @param {string|number} jobId 任务ID
 * @returns {Promise} 响应 data 为本次执行的日志
 */
export function triggerJob(jobId) {
  return request({ url: `/iot/job/info/trigger/${jobId}`, method: 'post' });
}

/**
 * 查询可用执行器列表。
 *
 * @returns {Promise} 响应 data 为 { 执行器名称: 描述 }
 */
export function listJobHandlers() {
  return request({ url: '/iot/job/info/handlers', method: 'get' });
}

/* ------------------------------------------------------------------
 * 任务执行日志
 * ------------------------------------------------------------------ */

/**
 * 分页查询执行日志。
 *
 * @param {object} query 查询条件（jobId/jobName/triggerType/status）+ 分页参数
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listJobLogs(query) {
  return request({ url: '/iot/job/log/list', method: 'get', params: query });
}

/**
 * 删除执行日志。
 *
 * @param {string|number|Array} logIds 日志ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delJobLog(logIds) {
  return request({ url: `/iot/job/log/${logIds}`, method: 'delete' });
}
