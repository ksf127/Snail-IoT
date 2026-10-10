import request, { download } from '@/utils/iotRequest'

/**
 * 分页查询数据集列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listDataset(query) {
  return request({ url: '/iot/dataset/list', method: 'get', params: query })
}

/**
 * 查询数据集下拉选项（供应用绑定选择使用）。
 *
 * @param {object} query 查询条件
 * @returns {Promise} 响应 data 为数据集列表
 */
export function datasetOptions(query) {
  return request({ url: '/iot/dataset/options', method: 'get', params: query })
}

/**
 * 查询数据集详情。
 *
 * @param {number|string} datasetId 数据集ID
 * @returns {Promise} 数据集详情
 */
export function getDataset(datasetId) {
  return request({ url: `/iot/dataset/${datasetId}`, method: 'get' })
}

/**
 * 新增数据集。
 *
 * @param {object} data 数据集数据
 * @returns {Promise} 操作结果
 */
export function addDataset(data) {
  return request({ url: '/iot/dataset', method: 'post', data })
}

/**
 * 修改数据集。
 *
 * @param {object} data 数据集数据
 * @returns {Promise} 操作结果
 */
export function updateDataset(data) {
  return request({ url: '/iot/dataset', method: 'put', data })
}

/**
 * 删除数据集。
 *
 * @param {Array|string} datasetIds 数据集ID集合
 * @returns {Promise} 操作结果
 */
export function delDataset(datasetIds) {
  return request({ url: `/iot/dataset/${datasetIds}`, method: 'delete' })
}

/**
 * 导出数据集 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportDataset(query) {
  return download('/iot/dataset/export', query)
}

/**
 * 查询数据集的角色数据权限。
 *
 * @param {number|string} datasetId 数据集ID
 * @returns {Promise} 角色数据权限列表
 */
export function getDatasetRoles(datasetId) {
  return request({ url: `/iot/dataset/${datasetId}/roles`, method: 'get' })
}

/**
 * 保存数据集的角色数据权限。
 *
 * @param {number|string} datasetId 数据集ID
 * @param {Array} scopes 角色数据权限列表 [{ roleId, dataScope }]
 * @returns {Promise} 操作结果
 */
export function saveDatasetRoles(datasetId, scopes) {
  return request({ url: `/iot/dataset/${datasetId}/roles`, method: 'put', data: scopes })
}