import request from '@/utils/iotRequest';

/* ------------------------------------------------------------------
 * 云账号（云平台纳管：mock / aliyun / flink）
 * ------------------------------------------------------------------ */

/**
 * 分页查询云账号列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listCloudAccounts(query) {
  return request({ url: '/iot/cloud/account/list', method: 'get', params: query });
}

/**
 * 查询云账号详情。
 *
 * @param {string|number} accountId 云账号ID
 * @returns {Promise} 云账号详情
 */
export function getCloudAccount(accountId) {
  return request({ url: `/iot/cloud/account/${accountId}`, method: 'get' });
}

/**
 * 新增云账号。
 *
 * @param {object} data 云账号表单
 * @returns {Promise}
 */
export function addCloudAccount(data) {
  return request({ url: '/iot/cloud/account', method: 'post', data });
}

/**
 * 修改云账号。
 *
 * @param {object} data 云账号表单
 * @returns {Promise}
 */
export function updateCloudAccount(data) {
  return request({ url: '/iot/cloud/account', method: 'put', data });
}

/**
 * 删除云账号（级联删除其纳管资源与 Flink 作业）。
 *
 * @param {string|number|Array} accountIds 云账号ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delCloudAccount(accountIds) {
  return request({ url: `/iot/cloud/account/${accountIds}`, method: 'delete' });
}

/**
 * 云账号连通性检测。
 *
 * @param {string|number} accountId 云账号ID
 * @returns {Promise} 响应 data 为 { success, message, costMs }
 */
export function testCloudAccount(accountId) {
  return request({ url: `/iot/cloud/account/test/${accountId}`, method: 'post' });
}

/**
 * 查询云账号可用的地域列表。
 *
 * @param {string|number} accountId 云账号ID
 * @returns {Promise} 地域列表 [{ regionId, regionName }]
 */
export function listCloudRegions(accountId) {
  return request({ url: `/iot/cloud/account/regions/${accountId}`, method: 'get' });
}

/* ------------------------------------------------------------------
 * 云资源（纳管资源清单）
 * ------------------------------------------------------------------ */

/**
 * 分页查询纳管资源。
 *
 * @param {object} query 查询条件（accountId/provider/resourceType/keyword）+ 分页参数
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listCloudResources(query) {
  return request({ url: '/iot/cloud/resource/list', method: 'get', params: query });
}

/**
 * 查询云资源详情。
 *
 * @param {string|number} resourceId 资源ID
 * @returns {Promise} 资源详情
 */
export function getCloudResource(resourceId) {
  return request({ url: `/iot/cloud/resource/${resourceId}`, method: 'get' });
}

/**
 * 从云平台同步资源清单（按实例ID upsert 到本地）。
 *
 * @param {string|number} accountId 云账号ID
 * @param {object} [query] 可选同步过滤条件 { resourceType, regionId, keyword }
 * @returns {Promise} 同步后的资源列表
 */
export function syncCloudResources(accountId, query) {
  return request({ url: `/iot/cloud/resource/sync/${accountId}`, method: 'post', params: query });
}

/**
 * 在云平台创建资源并纳管。
 *
 * @param {object} data 资源表单 { accountId, resourceType, instanceName, regionId, spec }
 * @returns {Promise}
 */
export function addCloudResource(data) {
  return request({ url: '/iot/cloud/resource', method: 'post', data });
}

/**
 * 启动资源。
 *
 * @param {string|number} resourceId 资源ID
 * @returns {Promise}
 */
export function startCloudResource(resourceId) {
  return request({ url: `/iot/cloud/resource/start/${resourceId}`, method: 'post' });
}

/**
 * 停止资源。
 *
 * @param {string|number} resourceId 资源ID
 * @returns {Promise}
 */
export function stopCloudResource(resourceId) {
  return request({ url: `/iot/cloud/resource/stop/${resourceId}`, method: 'post' });
}

/**
 * 释放资源（云端删除 + 本地移除）。
 *
 * @param {string|number} resourceId 资源ID
 * @returns {Promise}
 */
export function releaseCloudResource(resourceId) {
  return request({ url: `/iot/cloud/resource/release/${resourceId}`, method: 'delete' });
}

/**
 * 移除本地纳管记录（不操作云端）。
 *
 * @param {string|number|Array} resourceIds 资源ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delCloudResource(resourceIds) {
  return request({ url: `/iot/cloud/resource/${resourceIds}`, method: 'delete' });
}

/* ------------------------------------------------------------------
 * Flink 作业
 * ------------------------------------------------------------------ */

/**
 * 分页查询 Flink 作业。
 *
 * @param {object} query 查询条件（accountId/status/keyword）+ 分页参数
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listFlinkJobs(query) {
  return request({ url: '/iot/cloud/flink/list', method: 'get', params: query });
}

/**
 * 查询 Flink 作业详情。
 *
 * @param {string|number} jobId 作业ID
 * @returns {Promise} 作业详情
 */
export function getFlinkJob(jobId) {
  return request({ url: `/iot/cloud/flink/${jobId}`, method: 'get' });
}

/**
 * 从 Flink JobManager 同步作业清单。
 *
 * @param {string|number} accountId 云账号ID（须为 flink 类型）
 * @returns {Promise} 同步后的作业列表
 */
export function syncFlinkJobs(accountId) {
  return request({ url: `/iot/cloud/flink/sync/${accountId}`, method: 'post' });
}

/**
 * 优雅停止 Flink 作业。
 *
 * @param {string|number} jobId 作业ID
 * @returns {Promise}
 */
export function stopFlinkJob(jobId) {
  return request({ url: `/iot/cloud/flink/stop/${jobId}`, method: 'post' });
}

/**
 * 取消 Flink 作业。
 *
 * @param {string|number} jobId 作业ID
 * @returns {Promise}
 */
export function cancelFlinkJob(jobId) {
  return request({ url: `/iot/cloud/flink/cancel/${jobId}`, method: 'post' });
}
