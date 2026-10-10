import request, { download } from '@/utils/iotRequest'

/**
 * 分页查询产品列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listProduct(query) {
  return request({ url: '/iot/product/list', method: 'get', params: query })
}

/**
 * 查询产品详情。
 *
 * @param {number|string} productId 产品ID
 * @returns {Promise} 产品详情
 */
export function getProduct(productId) {
  return request({ url: `/iot/product/${productId}`, method: 'get' })
}

/**
 * 新增产品。
 *
 * @param {object} data 产品数据
 * @returns {Promise} 操作结果
 */
export function addProduct(data) {
  return request({ url: '/iot/product', method: 'post', data })
}

/**
 * 修改产品。
 *
 * @param {object} data 产品数据
 * @returns {Promise} 操作结果
 */
export function updateProduct(data) {
  return request({ url: '/iot/product', method: 'put', data })
}

/**
 * 删除产品。
 *
 * @param {Array|string} productIds 产品ID集合
 * @returns {Promise} 操作结果
 */
export function delProduct(productIds) {
  return request({ url: `/iot/product/${productIds}`, method: 'delete' })
}

/**
 * 导出产品 Excel。
 *
 * @param {object} query 查询条件
 * @returns {Promise<Blob>} 文件流
 */
export function exportProduct(query) {
  return download('/iot/product/export', query)
}
