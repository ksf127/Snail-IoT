<template>
  <div class="app-container">
    <div class="table-toolbar" style="margin-bottom: 16px">
      <span class="tip-text">数据来自当前节点 JVM 运行指标，默认每 15 秒刷新一次</span>
      <el-button :icon="Refresh" circle @click="loadMetrics" />
    </div>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" header="基础信息">
          <el-descriptions v-if="metrics" :column="1" border>
            <el-descriptions-item label="操作系统">{{ metrics.os }}</el-descriptions-item>
            <el-descriptions-item label="Java 版本">{{ metrics.javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="CPU 核心数">{{ metrics.availableProcessors }}</el-descriptions-item>
            <el-descriptions-item label="系统 CPU 负载">{{ metrics.cpuLoad }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ uptimeText }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无指标" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" header="JVM 内存">
          <template v-if="metrics">
            <div class="mem-item">
              <span>已用 / 总量</span>
              <span>{{ usedText }} / {{ totalText }}</span>
            </div>
            <el-progress
              :percentage="usedPercent"
              :color="progressColor"
              :stroke-width="18"
              text-inside
            />
            <div class="mem-item" style="margin-top: 16px">
              <span>最大可用</span>
              <span>{{ maxText }}</span>
            </div>
            <div class="mem-item">
              <span>空闲</span>
              <span>{{ freeText }}</span>
            </div>
          </template>
          <el-empty v-else description="暂无指标" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { serverMetrics } from '@/api/monitor'

const metrics = ref(null)
let timer = null

const progressColor = [
  { color: '#67c23a', percentage: 60 },
  { color: '#e6a23c', percentage: 85 },
  { color: '#f56c6c', percentage: 100 }
]

function toMb(v) {
  return v ? (v / 1024 / 1024).toFixed(1) + ' MB' : '-'
}

const totalText = computed(() => toMb(metrics.value?.jvmTotalMemory))
const freeText = computed(() => toMb(metrics.value?.jvmFreeMemory))
const maxText = computed(() => toMb(metrics.value?.jvmMaxMemory))
const usedText = computed(() => {
  if (!metrics.value) return '-'
  return toMb((metrics.value.jvmTotalMemory || 0) - (metrics.value.jvmFreeMemory || 0))
})

const usedPercent = computed(() => {
  if (!metrics.value?.jvmTotalMemory) return 0
  const used = metrics.value.jvmTotalMemory - (metrics.value.jvmFreeMemory || 0)
  return Number(((used / metrics.value.jvmTotalMemory) * 100).toFixed(2))
})

const uptimeText = computed(() => {
  if (!metrics.value?.uptimeMs) return '-'
  const s = Math.floor(metrics.value.uptimeMs / 1000)
  return `${Math.floor(s / 3600)} 小时 ${Math.floor((s % 3600) / 60)} 分钟`
})

async function loadMetrics() {
  try {
    const res = await serverMetrics()
    metrics.value = res.data
  } catch (e) {
    metrics.value = null
  }
}

onMounted(() => {
  loadMetrics()
  timer = setInterval(loadMetrics, 15000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.tip-text {
  color: #909399;
  font-size: 13px;
}

.mem-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #606266;
}
</style>
