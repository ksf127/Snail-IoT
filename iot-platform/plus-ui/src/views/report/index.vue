<template>
  <div class="app-container">
    <el-card shadow="never" header="设备数据统计报表">
      <el-form :inline="true" :model="reportQuery">
        <el-form-item label="业务类型">
          <el-select v-model="reportQuery.bizType" placeholder="全部" clearable style="width: 180px">
            <el-option v-for="t in bizTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="reportQuery.beginTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="reportQuery.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadReport">统计</el-button>
          <el-button :icon="Download" @click="handleBatchDownload">批量下载</el-button>
        </el-form-item>
      </el-form>

      <el-descriptions v-if="reportData" :column="3" border>
        <el-descriptions-item v-for="(value, key) in reportData" :key="key" :label="key">
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无统计数据" />
    </el-card>

    <el-card shadow="never" header="批量上传" style="margin-top: 16px">
      <el-form :inline="true">
        <el-form-item label="业务类型">
          <el-select v-model="uploadBizType" placeholder="请选择业务类型" style="width: 180px">
            <el-option v-for="t in bizTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :show-file-list="true"
            :on-change="onFileChange"
            :on-remove="() => (uploadFile = null)"
          >
            <el-button :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持单个 Excel 文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleBatchUpload">开始导入</el-button>
        </el-form-item>
      </el-form>
      <div v-if="uploadResult">
        <el-alert
          :type="uploadResult.status === 'FAIL' ? 'error' : uploadResult.status === 'RUNNING' ? 'warning' : 'success'"
          :closable="false"
          :title="`任务 ${uploadResult.taskId}｜${uploadResult.status}：成功 ${uploadResult.successCount ?? 0} 条，失败 ${uploadResult.failCount ?? 0} 条`"
        />
        <el-progress
          :percentage="uploadResult.progress ?? 0"
          :status="uploadResult.status === 'FAIL' ? 'exception' : uploadResult.status === 'SUCCESS' ? 'success' : ''"
          style="margin: 12px 0"
        />
        <el-table
          v-if="uploadErrorRows.length"
          :data="uploadErrorRows"
          size="small"
          border
          max-height="240"
        >
          <el-table-column prop="index" label="序号" width="80" />
          <el-table-column prop="message" label="失败原因" />
        </el-table>
        <el-button v-if="uploadResult.status === 'RUNNING'" size="small" @click="refreshUploadResult">
          刷新进度
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Search, Upload } from '@element-plus/icons-vue'
import { batchDownload, batchUpload, chunkUploadFile, deviceDataReport, getUploadResult, listBizTypes } from '@/api/report'
import { saveBlob } from '@/utils/save'

const bizTypes = ref([])
const reportData = ref(null)
const uploading = ref(false)
const uploadBizType = ref('')
const uploadFile = ref(null)
const uploadResult = ref(null)
const uploadErrorRows = computed(() =>
  (uploadResult.value?.errors || []).map((message, index) => ({ index: index + 1, message }))
)
let progressTimer = null
const CHUNK_THRESHOLD = 5 * 1024 * 1024

const reportQuery = reactive({
  bizType: '',
  beginTime: '',
  endTime: ''
})

async function loadBizTypes() {
  const res = await listBizTypes()
  bizTypes.value = res.data || []
}

async function loadReport() {
  const res = await deviceDataReport(reportQuery)
  reportData.value = res.data
}

function onFileChange(file) {
  uploadFile.value = file.raw
}

async function handleBatchUpload() {
  if (!uploadBizType.value) {
    ElMessage.warning('请先选择业务类型')
    return
  }
  if (!uploadFile.value) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }
  uploading.value = true
  try {
    const useChunk = uploadFile.value.size > CHUNK_THRESHOLD
    const res = useChunk
      ? await chunkUploadFile(uploadBizType.value, uploadFile.value)
      : await batchUpload(uploadBizType.value, uploadFile.value)
    uploadResult.value = res.data
    ElMessage.success(useChunk ? '分片上传完成，正在后台导入' : '导入完成')
    if (res.data?.status === 'RUNNING') {
      startProgressPolling()
    }
  } finally {
    uploading.value = false
  }
}

function stopProgressPolling() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

function startProgressPolling() {
  stopProgressPolling()
  progressTimer = setInterval(async () => {
    try {
      const res = await getUploadResult(uploadResult.value.taskId)
      uploadResult.value = res.data
      if (res.data.status !== 'RUNNING') {
        stopProgressPolling()
      }
    } catch (e) {
      stopProgressPolling()
    }
  }, 1000)
}

async function refreshUploadResult() {
  const res = await getUploadResult(uploadResult.value.taskId)
  uploadResult.value = res.data
}

async function handleBatchDownload() {
  const bizType = reportQuery.bizType || (bizTypes.value[0] ?? '')
  if (!bizType) {
    ElMessage.warning('请先选择业务类型')
    return
  }
  const blob = await batchDownload(bizType, {
    beginTime: reportQuery.beginTime,
    endTime: reportQuery.endTime
  })
  saveBlob(blob, `批量导出_${bizType}_${Date.now()}.xlsx`)
  ElMessage.success('下载成功')
}

onMounted(async () => {
  await loadBizTypes()
  loadReport()
})

onUnmounted(stopProgressPolling)
</script>
