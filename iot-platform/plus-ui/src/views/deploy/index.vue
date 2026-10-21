<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" class="deploy-tabs">
      <el-tab-pane label="部署任务" name="task">
        <div class="search-bar">
          <el-form :inline="true" :model="query">
            <el-form-item label="应用">
              <el-select v-model="query.appCode" placeholder="全部应用" clearable filterable style="width: 180px">
                <el-option
                  v-for="app in appOptions"
                  :key="app.appCode"
                  :label="app.appName"
                  :value="app.appCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="环境">
              <el-select v-model="query.targetEnv" placeholder="全部环境" clearable style="width: 140px">
                <el-option
                  v-for="env in envOptions"
                  :key="env.envCode"
                  :label="env.envName"
                  :value="env.envCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="query.taskType" placeholder="全部类型" clearable style="width: 120px">
                <el-option label="发布" value="deploy" />
                <el-option label="构建" value="build" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
                <el-option
                  v-for="(item, key) in STATUS_MAP"
                  :key="key"
                  :label="item.text"
                  :value="String(key)"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button v-hasPermi="['iot:deploy:release']" type="primary" :icon="Upload" @click="openRelease">
                发布部署
              </el-button>
              <el-button
                v-hasPermi="['iot:deploy:build']"
                type="success"
                plain
                :icon="VideoPlay"
                :loading="triggering"
                @click="openBuild"
              >
                构建应用
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadList" />
          </div>

          <el-table v-loading="loading" :data="list" border>
            <el-table-column prop="taskId" label="任务ID" width="180" show-overflow-tooltip />
            <el-table-column label="应用" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="app-cell">
                  <span class="app-name">{{ appNameOf(row) }}</span>
                  <span class="app-code">{{ row.appCode }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.taskType === 'build' ? 'warning' : 'info'" effect="plain">
                  {{ row.taskType === 'build' ? '构建' : '发布' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" width="120" />
            <el-table-column prop="targetEnv" label="目标环境" width="110" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-hasPermi="['iot:deploy:query']" link type="primary" @click="showLog(row)">
                  查看日志
                </el-button>
                <el-button
                  v-if="row.taskType === 'build' && row.status === '2' && row.buildNumber"
                  link
                  type="success"
                  @click="downloadJar(row)"
                >
                  下载 JAR
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="query.pageNum"
              v-model:page-size="query.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadList"
              @current-change="loadList"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="应用管理" name="app" lazy>
        <AppManager />
      </el-tab-pane>

      <el-tab-pane label="环境管理" name="env" lazy>
        <EnvManager />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="releaseVisible" title="发布部署" width="460px">
      <el-form ref="releaseFormRef" :model="releaseForm" :rules="releaseRules" label-width="100px">
        <el-form-item label="应用" prop="appCode">
          <el-select v-model="releaseForm.appCode" placeholder="请选择应用" style="width: 100%">
            <el-option
              v-for="app in appOptions"
              :key="app.appCode"
              :label="`${app.appName}（${app.appCode}）`"
              :value="app.appCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="releaseForm.version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="目标环境" prop="targetEnv">
          <el-select v-model="releaseForm.targetEnv" placeholder="请选择目标环境" style="width: 100%">
            <el-option
              v-for="env in envOptions"
              :key="env.envCode"
              :label="`${env.envName}（${env.envCode}）`"
              :value="env.envCode"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="releaseVisible = false">取消</el-button>
        <el-button type="primary" :loading="releasing" @click="handleRelease">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" title="部署日志" width="720px">
      <pre class="log-content">{{ logContent || '暂无日志' }}</pre>
    </el-dialog>

    <el-dialog v-model="buildFormVisible" title="构建应用" width="480px">
      <el-form ref="buildFormRef" :model="buildForm" :rules="buildRules" label-width="100px">
        <el-form-item label="应用" prop="appCode">
          <el-select v-model="buildForm.appCode" placeholder="请选择应用" style="width: 100%">
            <el-option
              v-for="app in appOptions"
              :key="app.appCode"
              :label="`${app.appName}（${app.appCode}）`"
              :value="app.appCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="版本 / Tag" prop="imageTag">
          <el-input v-model="buildForm.imageTag" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="目标环境" prop="envCode">
          <el-select v-model="buildForm.envCode" placeholder="请选择目标环境" style="width: 100%">
            <el-option
              v-for="env in envOptions"
              :key="env.envCode"
              :label="`${env.envName}（${env.envCode}）`"
              :value="env.envCode"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="只构建所选的应用，其它应用不受影响；构建完成后可在任务列表下载 JAR。"
      />
      <template #footer>
        <el-button @click="buildFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="triggering" @click="submitBuild">开始构建</el-button>
      </template>
    </el-dialog>

    <BuildLogDrawer
      v-model:visible="buildVisible"
      :app-name="buildTarget.appName"
      :env-code="buildTarget.envCode"
      :image-tag="buildTarget.imageTag"
      :info="buildInfo"
      :status-text="buildStatusText"
      :status-tag="buildStatusTag"
      :log="buildLog"
      @closed="stopBuildPolling"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, Upload, VideoPlay } from '@element-plus/icons-vue'
import { buildArtifactUrl, deployLog, listDeploy, listDeployApp, listDeployEnv, releaseDeploy } from '@/api/deploy'
import AppManager from './components/AppManager.vue'
import BuildLogDrawer from './components/BuildLogDrawer.vue'
import EnvManager from './components/EnvManager.vue'
import { useAppBuild } from './composables/useAppBuild'

const activeTab = ref('task')
const loading = ref(false)
const releasing = ref(false)
const releaseVisible = ref(false)
const logVisible = ref(false)
const releaseFormRef = ref()
const list = ref([])
const total = ref(0)
const logContent = ref('')
const appOptions = ref([])
const envOptions = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  appCode: undefined,
  targetEnv: undefined,
  taskType: undefined,
  status: undefined
})
const releaseForm = reactive({ appCode: '', version: '', targetEnv: 'dev' })

const releaseRules = {
  appCode: [{ required: true, message: '应用不能为空', trigger: 'change' }],
  version: [{ required: true, message: '版本号不能为空', trigger: 'blur' }],
  targetEnv: [{ required: true, message: '目标环境不能为空', trigger: 'change' }]
}

/* ---------------- 构建应用 ---------------- */

const buildFormVisible = ref(false)
const buildFormRef = ref()
const buildForm = reactive({ appCode: '', imageTag: '1.0.0', envCode: 'dev' })

const buildRules = {
  appCode: [{ required: true, message: '应用不能为空', trigger: 'change' }],
  imageTag: [{ required: true, message: '版本 / Tag 不能为空', trigger: 'blur' }],
  envCode: [{ required: true, message: '目标环境不能为空', trigger: 'change' }]
}

const {
  visible: buildVisible,
  triggering,
  log: buildLog,
  info: buildInfo,
  target: buildTarget,
  statusText: buildStatusText,
  statusTag: buildStatusTag,
  start: startBuild,
  stop: stopBuildPolling
} = useAppBuild({ onFinished: loadList })

function appNameOf(row) {
  return row.appName || appOptions.value.find((item) => item.appCode === row.appCode)?.appName || row.appCode || '-'
}

function downloadJar(row) {
  const url = buildArtifactUrl(row)
  if (!url) {
    ElMessage.warning('该构建任务没有归档 JAR')
    return
  }
  const link = document.createElement('a')
  link.href = url
  link.download = `${row.appCode}-${row.version}.jar`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const STATUS_MAP = {
  0: { text: '待执行', tag: 'info' },
  1: { text: '执行中', tag: 'warning' },
  2: { text: '成功', tag: 'success' },
  3: { text: '失败', tag: 'danger' }
}

function statusText(status) {
  return STATUS_MAP[status]?.text ?? '未知'
}

function statusTagType(status) {
  return STATUS_MAP[status]?.tag ?? 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await listDeploy(query)
    list.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [appRes, envRes] = await Promise.all([listDeployApp(), listDeployEnv()])
  appOptions.value = appRes.data || []
  envOptions.value = envRes.data || []
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  Object.assign(query, {
    pageNum: 1,
    appCode: undefined,
    targetEnv: undefined,
    taskType: undefined,
    status: undefined
  })
  loadList()
}

function openRelease() {
  releaseVisible.value = true
  Object.assign(releaseForm, {
    appCode: appOptions.value[0]?.appCode || '',
    version: '',
    targetEnv: envOptions.value[0]?.envCode || 'dev'
  })
  loadOptions()
}

async function handleRelease() {
  await releaseFormRef.value.validate()
  releasing.value = true
  try {
    await releaseDeploy(releaseForm)
    ElMessage.success('部署任务已创建')
    releaseVisible.value = false
    loadList()
  } finally {
    releasing.value = false
  }
}

async function showLog(row) {
  const res = await deployLog(row.taskId)
  logContent.value = res.data
  logVisible.value = true
}

async function openBuild() {
  await loadOptions()
  Object.assign(buildForm, {
    appCode: appOptions.value[0]?.appCode || '',
    imageTag: '1.0.0',
    envCode: envOptions.value[0]?.envCode || 'dev'
  })
  buildFormVisible.value = true
}

async function submitBuild() {
  await buildFormRef.value.validate()
  const app = appOptions.value.find((item) => item.appCode === buildForm.appCode)
  const env = envOptions.value.find((item) => item.envCode === buildForm.envCode)
  if (!app) {
    ElMessage.warning('请选择要构建的应用')
    return
  }
  const started = await startBuild(app, env, buildForm.imageTag)
  if (started) {
    buildFormVisible.value = false
  }
}

onMounted(() => {
  loadList()
  loadOptions()
})
</script>

<style scoped>
.app-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.app-name {
  color: var(--el-text-color-primary);
}

.app-code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.log-content {
  max-height: 420px;
  padding: 12px;
  margin: 0;
  overflow: auto;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 4px;
}
</style>
