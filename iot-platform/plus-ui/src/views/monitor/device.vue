<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">设备总数</div>
          <div class="stat-value">{{ overview?.total ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">在线设备</div>
          <div class="stat-value" style="color: #67c23a">{{ overview?.online ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">离线设备</div>
          <div class="stat-value" style="color: #f56c6c">{{ overview?.offline ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">在线率</div>
          <div class="stat-value" style="color: #e6a23c">{{ onlineRatePercent }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="table-card" style="margin-top: 16px">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="query.deviceName"
            placeholder="按设备名称筛选"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
        </div>
        <div>
          <el-switch v-model="autoRefresh" active-text="自动刷新" />
          <el-button :icon="Refresh" circle style="margin-left: 12px" @click="loadAll" />
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="deviceCode" label="设备编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="protocol" label="协议" width="100" />
        <el-table-column label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="onlineTagType(row.onlineStatus)">{{ onlineText(row.onlineStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
        <el-table-column prop="lastOnlineTime" label="最近在线" width="180" />
        <el-table-column prop="lastReportTime" label="最近上报" width="180" />
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
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { listDevice } from '@/api/device'
import { deviceOverview } from '@/api/monitor'

const loading = ref(false)
const autoRefresh = ref(false)
const list = ref([])
const total = ref(0)
const overview = ref(null)
let timer = null

const query = reactive({ deviceName: '', pageNum: 1, pageSize: 10 })

const onlineRatePercent = computed(() => {
  const rate = overview.value?.onlineRate
  return rate === undefined || rate === null ? 0 : Number((rate * 100).toFixed(2))
})

function onlineText(status) {
  return { 0: '离线', 1: '在线', 2: '未知' }[status] ?? '未知'
}

function onlineTagType(status) {
  return { 0: 'info', 1: 'success', 2: 'warning' }[status] ?? 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await listDevice(query)
    list.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadOverview() {
  try {
    const res = await deviceOverview()
    overview.value = res.data
  } catch (e) {
    overview.value = null
  }
}

function loadAll() {
  loadOverview()
  loadList()
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function startTimer() {
  stopTimer()
  timer = setInterval(loadAll, 15000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

watch(autoRefresh, (val) => {
  if (val) {
    startTimer()
  } else {
    stopTimer()
  }
})

onMounted(loadAll)
onBeforeUnmount(stopTimer)
</script>

<style scoped>
.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}
</style>
