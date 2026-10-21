import request from '@/utils/iotRequest';

/**
 * 分页查询支付单列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayOrders(query) {
  return request({ url: '/iot/pay/list', method: 'get', params: query });
}

/**
 * 分页查询退款单列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayRefunds(query) {
  return request({ url: '/iot/pay/refund/list', method: 'get', params: query });
}

/**
 * 统一下单（预下单），返回支付单号与二维码链接。
 *
 * @param {object} data { bizOrderNo, channel, subject, amount, currency, remark }
 * @returns {Promise} 支付单信息（含 orderNo / codeUrl / prePayId）
 */
export function createPayOrder(data) {
  return request({ url: '/iot/pay/create', method: 'post', data });
}

/**
 * 按支付单号查单。
 *
 * @param {string} orderNo 支付单号
 * @returns {Promise} 支付单详情
 */
export function queryPayOrder(orderNo) {
  return request({ url: `/iot/pay/query/${orderNo}`, method: 'get' });
}

/**
 * 发起退款。
 *
 * @param {object} data { orderNo, refundAmount, reason }
 * @returns {Promise} 退款单信息
 */
export function refundPayOrder(data) {
  return request({ url: '/iot/pay/refund', method: 'post', data });
}

/**
 * 查询已启用的支付渠道配置。
 *
 * @returns {Promise} 渠道配置列表
 */
export function listPayChannels() {
  return request({ url: '/iot/pay/channels', method: 'get' });
}

/**
 * 分页查询渠道配置。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayChannelPage(query) {
  return request({ url: '/iot/pay/channel/page', method: 'get', params: query });
}

/**
 * 查询渠道配置详情。
 *
 * @param {string|number} configId 配置ID
 * @returns {Promise} 渠道配置详情
 */
export function getPayChannel(configId) {
  return request({ url: `/iot/pay/channel/${configId}`, method: 'get' });
}

/**
 * 新增渠道配置。
 *
 * @param {object} data 渠道配置表单
 * @returns {Promise}
 */
export function addPayChannel(data) {
  return request({ url: '/iot/pay/channel', method: 'post', data });
}

/**
 * 修改渠道配置。
 *
 * @param {object} data 渠道配置表单
 * @returns {Promise}
 */
export function updatePayChannel(data) {
  return request({ url: '/iot/pay/channel', method: 'put', data });
}

/**
 * 删除渠道配置。
 *
 * @param {string|number|Array} configIds 配置ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delPayChannel(configIds) {
  return request({ url: `/iot/pay/channel/${configIds}`, method: 'delete' });
}

/**
 * 分页查询支付商品。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listPayProductPage(query) {
  return request({ url: '/iot/pay/product/list', method: 'get', params: query });
}

/**
 * 查询上架商品（下单可选）。
 *
 * @returns {Promise} 上架商品列表
 */
export function listOnSaleProducts() {
  return request({ url: '/iot/pay/product/on-sale', method: 'get' });
}

/**
 * 查询商品详情。
 *
 * @param {string|number} productId 商品ID
 * @returns {Promise} 商品详情
 */
export function getPayProduct(productId) {
  return request({ url: `/iot/pay/product/${productId}`, method: 'get' });
}

/**
 * 新增商品。
 *
 * @param {object} data 商品表单
 * @returns {Promise}
 */
export function addPayProduct(data) {
  return request({ url: '/iot/pay/product', method: 'post', data });
}

/**
 * 修改商品。
 *
 * @param {object} data 商品表单
 * @returns {Promise}
 */
export function updatePayProduct(data) {
  return request({ url: '/iot/pay/product', method: 'put', data });
}

/**
 * 删除商品。
 *
 * @param {string|number|Array} productIds 商品ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delPayProduct(productIds) {
  return request({ url: `/iot/pay/product/${productIds}`, method: 'delete' });
}
