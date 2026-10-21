<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="应用编码">
          <el-input v-model="query.appCode" placeholder="请输入应用编码" clearable />
        </el-form-item>
        <el-form-item label="应用名称">
          <el-input v-model="query.appName" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
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
          <el-button v-hasPermi="['iot:deploy:app:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增应用
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="appCode" label="应用编码" width="150" show-overflow-tooltip />
        <el-table-column prop="appName" label="应用名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="artifact" label="制品 / 镜像" min-width="200" show-overflow-tooltip />
        <el-table-column prop="k8sWorkload" label="工作负载" width="130" show-overflow-tooltip />
        <el-table-column prop="k8sContainer" label="容器" width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:deploy:build']" link type="success" @click="openBuild(row)">
              构建
            </el-button>
            <el-button v-hasPermi="['iot:deploy:app:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:deploy:app:remove']" link type="danger" @click="handleDelete(row)">
              删除
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="form.appCode" placeholder="如 my-service" :disabled="!!form.appId" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="代码仓库">
          <el-input v-model="form.repoUrl" placeholder="如 https://github.com/xxx/yyy.git" />
        </el-form-item>
        <el-form-item label="代码分支">
          <el-input v-model="form.repoBranch" placeholder="如 main" />
        </el-form-item>
        <el-form-item label="构建方式">
          <el-select v-model="form.buildType" style="width: 100%">
            <el-option label="Maven" value="maven" />
            <el-option label="Node" value="node" />
          </el-select>
        </el-form-item>
        <el-form-item label="构建命令">
          <el-input
            v-model="form.buildCommand"
            type="textarea"
            :rows="3"
            placeholder="在构建目录内执行；留空表示由 Dockerfile 完成构建"
          />
        </el-form-item>
        <el-form-item label="构建目录">
          <el-input v-model="form.buildWorkdir" placeholder="相对代码根目录，默认 ." />
        </el-form-item>
        <el-form-item label="Dockerfile">
          <el-input v-model="form.dockerfilePath" placeholder="相对代码根目录，如 iot-platform/plus-ui/Dockerfile" />
        </el-form-item>
        <el-form-item label="制品 / 镜像">
          <el-input v-model="form.artifact" placeholder="如 iot-platform/iot-platform-admin（不含 tag）" />
        </el-form-item>
        <el-form-item label="JAR 路径">
          <el-input v-model="form.jarPath" placeholder="如 iot-platform/iot-platform-admin/target/*.jar，留空不归档" />
        </el-form-item>
        <el-form-item label="工作负载">
          <el-input v-model="form.k8sWorkload" placeholder="Kubernetes Deployment 名称" />
        </el-form-item>
        <el-form-item label="容器名称">
          <el-input v-model="form.k8sContainer" placeholder="留空时取 Deployment 首个容器" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="buildDialogVisible" title="构建应用" width="440px">
      <el-form label-width="100px">
        <el-form-item label="应用">{{ buildRow.appName }}（{{ buildRow.appCode }}）</el-form-item>
        <el-form-item label="版本 / Tag">
          <el-input v-model="buildTag" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="目标环境">
          <el-select v-model="buildEnvCode" placeholder="请选择目标环境" style="width: 100%">
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
        <el-button @click="buildDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="triggering" @click="submitBuild">开始构建</el-button>
      </template>
    </el-dialog>

    <BuildLogDrawer
      v-model:visible="buildDrawerVisible"
      :app-name="buildTarget.appName"
      :env-code="buildTarget.envCode"
      :image-tag="buildTarget.imageTag"
      :info="buildInfo"
      :status-text="buildStatusText"
      :status-tag="buildStatusTag"
      :log="buildLog"
      :hint="buildHint"
      @closed="stopBuildPolling"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { addDeployApp, delDeployApp, listDeployAppPage, listDeployEnv, updateDeployApp } from '@/api/deploy'
import BuildLogDrawer from './BuildLogDrawer.vue'
import { useAppBuild } from '../composables/useAppBuild'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增应用')
const formRef = ref()
const list = ref([])
const total = ref(0)

const query = reactive({
  appCode: '',
  appName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  appId: undefined,
  appCode: '',
  appName: '',
  repoUrl: '',
  repoBranch: 'main',
  buildType: 'maven',
  buildCommand: '',
  buildWorkdir: '.',
  dockerfilePath: 'Dockerfile',
  artifact: '',
  jarPath: '',
  k8sWorkload: '',
  k8sContainer: '',
  status: '0',
  sortNo: 0,
  remark: ''
})

const formRules = {
  appCode: [{ required: true, message: '应用编码不能为空', trigger: 'blur' }],
  appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }]
}

/* ---------------- 行内构建 ---------------- */

const envOptions = ref([])
const buildDialogVisible = ref(false)
const buildRow = ref({})
const buildTag = ref('1.0.0')
const buildEnvCode = ref('')

const {
  visible: buildDrawerVisible,
  triggering,
  log: buildLog,
  hint: buildHint,
  info: buildInfo,
  target: buildTarget,
  statusText: buildStatusText,
  statusTag: buildStatusTag,
  start: startBuild,
  stop: stopBuildPolling
} = useAppBuild()

async function loadList() {
  loading.value = true
  try {
    const res = await listDeployAppPage(query)
    list.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  Object.assign(query, { appCode: '', appName: '', status: '', pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改应用'
    Object.assign(form, row)
  } else {
    dialogTitle.value = '新增应用'
    Object.assign(form, {
      appId: undefined,
      appCode: '',
      appName: '',
      repoUrl: '',
      repoBranch: 'main',
      buildType: 'maven',
      buildCommand: '',
      buildWorkdir: '.',
      dockerfilePath: 'Dockerfile',
      artifact: '',
      jarPath: '',
      k8sWorkload: '',
      k8sContainer: '',
      status: '0',
      sortNo: 0,
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.appId) {
      await updateDeployApp(form)
      ElMessage.success('修改成功')
    } else {
      await addDeployApp(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除应用「${row.appName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delDeployApp(row.appId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function loadEnvOptions() {
  if (envOptions.value.length) return
  const res = await listDeployEnv()
  envOptions.value = res.data || []
}

async function openBuild(row) {
  buildRow.value = row
  buildTag.value = '1.0.0'
  await loadEnvOptions()
  buildEnvCode.value = envOptions.value[0]?.envCode || 'dev'
  buildDialogVisible.value = true
}

async function submitBuild() {
  if (!buildTag.value) {
    ElMessage.warning('请填写版本 / Tag')
    return
  }
  const env = envOptions.value.find((item) => item.envCode === buildEnvCode.value)
  const started = await startBuild(buildRow.value, env, buildTag.value)
  if (started) {
    buildDialogVisible.value = false
  }
}

onMounted(loadList)
</script>
