<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <div>
          <el-button v-hasPermi="['iot:deploy:release']" type="primary" :icon="Upload" @click="openRelease">
            发布部署
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="taskId" label="任务ID" width="180" show-overflow-tooltip />
        <el-table-column prop="appCode" label="应用编码" width="160" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column prop="targetEnv" label="目标环境" width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:deploy:query']" link type="primary" @click="showLog(row)">
              查看日志
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

    <el-dialog v-model="releaseVisible" title="发布部署" width="460px">
      <el-form ref="releaseFormRef" :model="releaseForm" :rules="releaseRules" label-width="100px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="releaseForm.appCode" placeholder="请输入应用编码" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="releaseForm.version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="目标环境" prop="targetEnv">
          <el-select v-model="releaseForm.targetEnv" placeholder="请选择目标环境" style="width: 100%">
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Upload } from '@element-plus/icons-vue'
import { deployLog, listDeploy, releaseDeploy } from '@/api/deploy'

const loading = ref(false)
const releasing = ref(false)
const releaseVisible = ref(false)
const logVisible = ref(false)
const releaseFormRef = ref()
const list = ref([])
const total = ref(0)
const logContent = ref('')

const query = reactive({ pageNum: 1, pageSize: 10 })
const releaseForm = reactive({ appCode: '', version: '', targetEnv: 'dev' })

const releaseRules = {
  appCode: [{ required: true, message: '应用编码不能为空', trigger: 'blur' }],
  version: [{ required: true, message: '版本号不能为空', trigger: 'blur' }],
  targetEnv: [{ required: true, message: '目标环境不能为空', trigger: 'change' }]
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

function openRelease() {
  releaseVisible.value = true
  Object.assign(releaseForm, { appCode: '', version: '', targetEnv: 'dev' })
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

onMounted(loadList)
</script>

<style scoped>
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
