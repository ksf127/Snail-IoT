import request from '@/utils/iotRequest'

/**
 * 分页查询支付单列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayOrders(query) {
  return request({ url: '/iot/pay/list', method: 'get', params: query })
}

/**
 * 分页查询退款单列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayRefunds(query) {
  return request({ url: '/iot/pay/refund/list', method: 'get', params: query })
}

/**
 * 统一下单（预下单），返回支付单号与二维码链接。
 *
 * @param {object} data { bizOrderNo, channel, subject, amount, currency, remark }
 * @returns {Promise} 支付单信息（含 orderNo / codeUrl / prePayId）
 */
export function createPayOrder(data) {
  return request({ url: '/iot/pay/create', method: 'post', data })
}

/**
 * 按支付单号查单。
 *
 * @param {string} orderNo 支付单号
 * @returns {Promise} 支付单详情
 */
export function queryPayOrder(orderNo) {
  return request({ url: `/iot/pay/query/${orderNo}`, method: 'get' })
}

/**
 * 发起退款。
 *
 * @param {object} data { orderNo, refundAmount, reason }
 * @returns {Promise} 退款单信息
 */
export function refundPayOrder(data) {
  return request({ url: '/iot/pay/refund', method: 'post', data })
}

/**
 * 查询已启用的支付渠道配置。
 *
 * @returns {Promise} 渠道配置列表
 */
export function listPayChannels() {
  return request({ url: '/iot/pay/channels', method: 'get' })
}
