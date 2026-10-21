import request from '@/utils/iotRequest'
import axios from 'axios'

/** Jenkins 通用构建 Job 路径（经 nginx /jenkins-api/ 反代，免跨域、免登录） */
const JENKINS_JOB = '/jenkins-api/job/snail-app-build'

/**
 * 按应用构建配置触发通用流水线。
 *
 * @param {object} params { APP_CODE, REPO_URL, REPO_BRANCH, BUILD_WORKDIR, BUILD_COMMAND,
 *                          IMAGE, IMAGE_TAG, DOCKERFILE_PATH, K8S_NAMESPACE, K8S_WORKLOAD, K8S_CONTAINER }
 * @returns {Promise} Jenkins 响应（201 表示已入队）
 */
export function triggerAppBuild(params) {
  const form = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => form.append(key, value ?? ''))
  return axios.post(`${JENKINS_JOB}/buildWithParameters`, form, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

/**
 * 查询最近一次通用流水线的构建信息。
 *
 * @returns {Promise} 响应 data 为 { number, building, result, timestamp }
 */
export function getJenkinsBuildInfo() {
  return axios.get(`${JENKINS_JOB}/lastBuild/api/json`, {
    params: { tree: 'number,building,result,timestamp' }
  })
}

/**
 * 查询最近一次通用流水线的控制台日志。
 *
 * @returns {Promise} 响应 data 为纯文本日志
 */
export function getJenkinsConsole() {
  return axios.get(`${JENKINS_JOB}/lastBuild/consoleText`, { params: { _: Date.now() } })
}

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

/**
 * 登记一条构建任务，使构建记录出现在部署任务列表。
 *
 * @param {object} params { appCode, version, targetEnv }
 * @returns {Promise} 任务ID
 */
export function createBuildTask(params) {
  return request({ url: '/iot/deploy/task/build', method: 'post', params })
}

/**
 * 回写构建任务结果。
 *
 * @param {number|string} taskId 任务ID
 * @param {string} status 结果状态（2成功 3失败）
 * @param {number} [buildNumber] Jenkins 构建号
 * @returns {Promise} 操作结果
 */
export function finishBuildTask(taskId, status, buildNumber) {
  return request({
    url: `/iot/deploy/task/${taskId}/finish`,
    method: 'put',
    params: { status, buildNumber }
  })
}

/**
 * 构建产物 JAR 下载地址（Jenkins 归档制品，经 nginx 反代）。
 *
 * @param {object} task { appCode, version, buildNumber } 构建任务
 * @returns {string|null} 下载地址，缺少构建号时返回 null
 */
export function buildArtifactUrl(task) {
  if (!task?.buildNumber) return null
  const fileName = `${task.appCode}-${task.version}.jar`
  return `${JENKINS_JOB}/${task.buildNumber}/artifact/artifacts/${encodeURIComponent(fileName)}`
}

/**
 * 查询已登记的应用列表。
 *
 * @returns {Promise} 应用列表
 */
export function listDeployApp() {
  return request({ url: '/iot/deploy/app/list', method: 'get' })
}

/**
 * 查询已登记的环境列表。
 *
 * @returns {Promise} 环境列表
 */
export function listDeployEnv() {
  return request({ url: '/iot/deploy/env/list', method: 'get' })
}

/**
 * 分页查询部署应用。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listDeployAppPage(query) {
  return request({ url: '/iot/deploy/app/page', method: 'get', params: query })
}

/**
 * 查询部署应用详情。
 *
 * @param {number|string} appId 应用ID
 * @returns {Promise} 应用详情
 */
export function getDeployApp(appId) {
  return request({ url: `/iot/deploy/app/${appId}`, method: 'get' })
}

/**
 * 新增部署应用。
 *
 * @param {object} data 应用数据
 * @returns {Promise} 操作结果
 */
export function addDeployApp(data) {
  return request({ url: '/iot/deploy/app', method: 'post', data })
}

/**
 * 修改部署应用。
 *
 * @param {object} data 应用数据
 * @returns {Promise} 操作结果
 */
export function updateDeployApp(data) {
  return request({ url: '/iot/deploy/app', method: 'put', data })
}

/**
 * 删除部署应用。
 *
 * @param {Array|string} appIds 应用ID集合
 * @returns {Promise} 操作结果
 */
export function delDeployApp(appIds) {
  return request({ url: `/iot/deploy/app/${appIds}`, method: 'delete' })
}

/**
 * 分页查询部署环境。
 *
 * @param {object} query 查询条件 + 分页参数 pageNum/pageSize
 * @returns {Promise} 响应 data 为 { rows, total }
 */
export function listDeployEnvPage(query) {
  return request({ url: '/iot/deploy/env/page', method: 'get', params: query })
}

/**
 * 查询部署环境详情。
 *
 * @param {number|string} envId 环境ID
 * @returns {Promise} 环境详情
 */
export function getDeployEnv(envId) {
  return request({ url: `/iot/deploy/env/${envId}`, method: 'get' })
}

/**
 * 新增部署环境。
 *
 * @param {object} data 环境数据
 * @returns {Promise} 操作结果
 */
export function addDeployEnv(data) {
  return request({ url: '/iot/deploy/env', method: 'post', data })
}

/**
 * 修改部署环境。
 *
 * @param {object} data 环境数据
 * @returns {Promise} 操作结果
 */
export function updateDeployEnv(data) {
  return request({ url: '/iot/deploy/env', method: 'put', data })
}

/**
 * 删除部署环境。
 *
 * @param {Array|string} envIds 环境ID集合
 * @returns {Promise} 操作结果
 */
export function delDeployEnv(envIds) {
  return request({ url: `/iot/deploy/env/${envIds}`, method: 'delete' })
}
