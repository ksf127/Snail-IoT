import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createBuildTask,
  finishBuildTask,
  getJenkinsBuildInfo,
  getJenkinsConsole,
  triggerAppBuild
} from '@/api/deploy'

/**
 * 通用应用构建：按应用登记构建配置触发 Jenkins 参数化 Job，轮询实时日志，
 * 并在部署任务列表中登记一条构建任务记录。
 *
 * @param {object} options { onFinished } 构建结束回调（用于刷新任务列表）
 * @returns 构建状态与 start/stop 方法
 */
export function useAppBuild(options = {}) {
  const visible = ref(false)
  const triggering = ref(false)
  const log = ref('')
  const info = reactive({ number: 0, building: false, result: '' })
  const target = reactive({ appName: '', appCode: '', envCode: '', imageTag: '' })

  // 触发前记录最近一次构建号，用于区分本次构建
  let baselineBuildNumber = 0
  let pollTimer = null
  let queuedPolls = 0
  let buildTaskId = null
  let settled = true

  const statusText = computed(() => {
    if (!info.number) return '排队中'
    if (info.building) return '构建中'
    return info.result === 'SUCCESS' ? '成功' : '失败'
  })

  const statusTag = computed(() => {
    if (!info.number) return 'info'
    if (info.building) return 'warning'
    return info.result === 'SUCCESS' ? 'success' : 'danger'
  })

  /**
   * 触发构建。
   *
   * @param {object} app 应用（含构建配置）
   * @param {object} env 目标环境（含 k8sNamespace）
   * @param {string} imageTag 镜像 tag / 版本号
   * @returns {Promise<boolean>} 是否成功入队
   */
  async function start(app, env, imageTag) {
    triggering.value = true
    try {
      const prev = await getJenkinsBuildInfo().catch(() => null)
      baselineBuildNumber = prev?.data?.number || 0
      await triggerAppBuild(buildParams(app, env, imageTag))
      Object.assign(target, {
        appName: app.appName,
        appCode: app.appCode,
        envCode: env?.envCode || '',
        imageTag
      })
      log.value = ''
      queuedPolls = 0
      settled = false
      buildTaskId = null
      Object.assign(info, { number: 0, building: true, result: '' })
      visible.value = true
      // 登记构建任务，让多项目的构建记录统一出现在「部署任务」列表
      try {
        const res = await createBuildTask({
          appCode: app.appCode,
          version: imageTag,
          targetEnv: env?.envCode || ''
        })
        buildTaskId = res.data
      } catch (e) {
        buildTaskId = null
      }
      schedulePoll(1500)
      return true
    } catch (e) {
      ElMessage.error('触发失败：Jenkins 不可达，请确认 iot-jenkins 容器已启动（http://localhost:8090）')
      return false
    } finally {
      triggering.value = false
    }
  }

  /**
   * 按应用配置组装 Jenkins 参数。
   */
  function buildParams(app, env, imageTag) {
    return {
      APP_CODE: app.appCode,
      REPO_URL: app.repoUrl || '',
      REPO_BRANCH: app.repoBranch || 'main',
      BUILD_WORKDIR: app.buildWorkdir || '.',
      BUILD_COMMAND: app.buildCommand || '',
      IMAGE: app.artifact || '',
      IMAGE_TAG: imageTag,
      JAR_PATH: app.jarPath || '',
      DOCKERFILE_PATH: app.dockerfilePath || 'Dockerfile',
      K8S_NAMESPACE: env?.k8sNamespace || '',
      K8S_WORKLOAD: app.k8sWorkload || '',
      K8S_CONTAINER: app.k8sContainer || ''
    }
  }

  function schedulePoll(delay = 2000) {
    clearTimeout(pollTimer)
    pollTimer = setTimeout(pollBuild, delay)
  }

  async function pollBuild() {
    try {
      const { data } = await getJenkinsBuildInfo()
      // 新构建尚未出现在 lastBuild 上，说明还在排队
      if (!data || data.number <= baselineBuildNumber) {
        if (++queuedPolls > 120) {
          ElMessage.warning('等待构建启动超时，请到 Jenkins 页面查看排队情况')
          await settle('3')
          return
        }
        schedulePoll(1500)
        return
      }
      Object.assign(info, data)
      // 抽屉关闭后只跟踪状态，不再拉取控制台日志
      if (visible.value) {
        const consoleRes = await getJenkinsConsole()
        log.value = consoleRes.data || ''
      }
      if (data.building) {
        schedulePoll(1500)
      } else if (data.result === 'SUCCESS') {
        ElMessage.success(`构建 #${data.number} 成功`)
        await settle('2')
      } else {
        ElMessage.error(`构建 #${data.number} 失败：${data.result || '已终止'}`)
        await settle('3')
      }
    } catch (e) {
      schedulePoll(3000)
    }
  }

  /**
   * 结束本次构建：回写任务状态并通知列表刷新。
   *
   * @param {string} status 2成功 3失败
   */
  async function settle(status) {
    if (settled) return
    settled = true
    clearTimeout(pollTimer)
    pollTimer = null
    if (buildTaskId) {
      await finishBuildTask(buildTaskId, status, info.number || undefined).catch(() => {})
    }
    options.onFinished?.()
  }

  /** 停止日志轮询（抽屉关闭时调用），未结束时保留后台状态跟踪 */
  function stop() {
    clearTimeout(pollTimer)
    pollTimer = null
    if (!settled) {
      schedulePoll(3000)
    }
  }

  return { visible, triggering, log, info, target, statusText, statusTag, start, stop }
}
