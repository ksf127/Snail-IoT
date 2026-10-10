import request from '@/utils/iotRequest'

/**
 * 设备监控总览。
 *
 * @returns {Promise} 响应 data 为 { total, online, offline, unknown, onlineRate }
 */
export function deviceOverview() {
  return request({ url: '/iot/monitor/device/overview', method: 'get' })
}

/**
 * 当前节点服务器运行指标。
 *
 * @returns {Promise} 响应 data 含 jvm 内存、CPU 负载、运行时长等
 */
export function serverMetrics() {
  return request({ url: '/iot/monitor/server/metrics', method: 'get' })
}
