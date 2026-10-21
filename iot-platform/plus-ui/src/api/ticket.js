import request from '@/utils/iotRequest';

/**
 * 分页查询工单列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listTickets(query) {
  return request({ url: '/iot/ticket/list', method: 'get', params: query });
}

/**
 * 查询工单流转记录。
 *
 * @param {object} query { ticketId, action }
 * @returns {Promise} 响应 data 为流转记录列表
 */
export function listTicketRecords(query) {
  return request({ url: '/iot/ticket/records', method: 'get', params: query });
}

/**
 * 查询工单详情。
 *
 * @param {string|number} ticketId 工单ID
 * @returns {Promise} 工单详情
 */
export function getTicket(ticketId) {
  return request({ url: `/iot/ticket/${ticketId}`, method: 'get' });
}

/**
 * 新建工单。
 *
 * @param {object} data 工单表单
 * @returns {Promise}
 */
export function addTicket(data) {
  return request({ url: '/iot/ticket', method: 'post', data });
}

/**
 * 修改工单。
 *
 * @param {object} data 工单表单
 * @returns {Promise}
 */
export function updateTicket(data) {
  return request({ url: '/iot/ticket', method: 'put', data });
}

/**
 * 删除工单。
 *
 * @param {string|number|Array} ticketIds 工单ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delTicket(ticketIds) {
  return request({ url: `/iot/ticket/${ticketIds}`, method: 'delete' });
}

/**
 * 提交工单（规则工单触发规则引擎、流程工单发起流程）。
 *
 * @param {string|number} ticketId 工单ID
 * @param {object} query { flowCode, handler }
 * @returns {Promise}
 */
export function submitTicket(ticketId, query) {
  return request({ url: `/iot/ticket/submit/${ticketId}`, method: 'post', params: query });
}

/**
 * 执行（或重跑）规则引擎。
 *
 * @param {string|number} ticketId 工单ID
 * @returns {Promise}
 */
export function runTicketRule(ticketId) {
  return request({ url: `/iot/ticket/rule/${ticketId}`, method: 'post' });
}

/**
 * 人工办理工单。
 *
 * @param {object} data { ticketId, action, handlerId, handlerName, message, priority }
 * @returns {Promise}
 */
export function handleTicket(data) {
  return request({ url: '/iot/ticket/handle', method: 'post', data });
}

/**
 * 办理流程任务。
 *
 * @param {object} data { ticketId, action, processTaskId, message }
 * @returns {Promise}
 */
export function completeFlowTask(data) {
  return request({ url: '/iot/ticket/flow/complete', method: 'post', data });
}

/**
 * 同步流程状态回写工单。
 *
 * @param {string|number} ticketId 工单ID
 * @returns {Promise} 最新工单详情
 */
export function syncTicketFlow(ticketId) {
  return request({ url: `/iot/ticket/flow/sync/${ticketId}`, method: 'post' });
}

/**
 * 分页查询工单规则列表。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listTicketRules(query) {
  return request({ url: '/iot/ticket/rule/list', method: 'get', params: query });
}

/**
 * 查询启用的工单规则。
 *
 * @returns {Promise} 启用规则列表
 */
export function listEnabledTicketRules() {
  return request({ url: '/iot/ticket/rule/enabled', method: 'get' });
}

/**
 * 查询工单规则详情。
 *
 * @param {string|number} ruleId 规则ID
 * @returns {Promise} 规则详情
 */
export function getTicketRule(ruleId) {
  return request({ url: `/iot/ticket/rule/${ruleId}`, method: 'get' });
}

/**
 * 新增工单规则。
 *
 * @param {object} data 规则表单
 * @returns {Promise}
 */
export function addTicketRule(data) {
  return request({ url: '/iot/ticket/rule', method: 'post', data });
}

/**
 * 修改工单规则。
 *
 * @param {object} data 规则表单
 * @returns {Promise}
 */
export function updateTicketRule(data) {
  return request({ url: '/iot/ticket/rule', method: 'put', data });
}

/**
 * 删除工单规则。
 *
 * @param {string|number|Array} ruleIds 规则ID，多个以英文逗号分隔
 * @returns {Promise}
 */
export function delTicketRule(ruleIds) {
  return request({ url: `/iot/ticket/rule/${ruleIds}`, method: 'delete' });
}
