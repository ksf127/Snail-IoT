<template>
  <el-drawer
    :model-value="visible"
    size="65%"
    @update:model-value="emit('update:visible', $event)"
    @closed="emit('closed')"
  >
    <template #header>
      <div class="build-header">
        <span>Jenkins 构建日志</span>
        <el-tag v-if="appName" type="info">{{ appName }}</el-tag>
        <el-tag v-if="envCode" type="info">{{ envCode }}</el-tag>
        <el-tag v-if="imageTag" type="info">{{ imageTag }}</el-tag>
        <el-tag v-if="info.number" type="info">#{{ info.number }}</el-tag>
        <el-tag :type="statusTag">{{ statusText }}</el-tag>
      </div>
    </template>
    <div class="build-body">
      <el-alert
        v-if="hint"
        :title="hint"
        type="info"
        :closable="false"
        show-icon
        class="build-hint"
      />
      <pre ref="logRef" class="build-log">{{ log || '正在触发流水线…' }}</pre>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  appName: { type: String, default: '' },
  envCode: { type: String, default: '' },
  imageTag: { type: String, default: '' },
  statusText: { type: String, default: '' },
  statusTag: { type: String, default: 'info' },
  info: { type: Object, default: () => ({}) },
  log: { type: String, default: '' },
  hint: { type: String, default: '' }
})

const emit = defineEmits(['update:visible', 'closed'])

const logRef = ref()

// 日志更新后自动滚动到底部
watch(
  () => props.log,
  () => {
    requestAnimationFrame(() => {
      if (logRef.value) {
        logRef.value.scrollTop = logRef.value.scrollHeight
      }
    })
  }
)
</script>

<style scoped>
.build-header {
  display: flex;
  gap: 10px;
  align-items: center;
  font-weight: 600;
}

.build-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: calc(100vh - 130px);
}

.build-hint {
  flex: none;
}

.build-log {
  flex: 1;
  min-height: 0;
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
