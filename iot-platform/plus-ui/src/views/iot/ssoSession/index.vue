<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <div>
          <el-button
            v-hasPermi="['iot:sso:logout']"
            type="danger"
            :icon="SwitchButton"
            :loading="loggingOut"
            @click="handleLogout"
          >
            全局登出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="userId" label="用户ID" width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名" min-width="140" />
        <el-table-column prop="clientId" label="客户端" min-width="160" show-overflow-tooltip />
        <el-table-column label="登录时间" width="180">
          <template #default="{ row }">{{ formatTime(row.loginTime) }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="120">
          <template #default="{ row }">{{ formatExpire(row.expireIn) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { Refresh, SwitchButton } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, ref } from 'vue';
import { listSsoSessions, ssoLogout } from '@/api/ssoSession';

const loading = ref(false);
const loggingOut = ref(false);
const list = ref([]);

function formatTime(timestamp) {
  if (!timestamp) {
    return '-';
  }
  const date = new Date(Number(timestamp));
  const pad = n => String(n).padStart(2, '0');
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
}

function formatExpire(seconds) {
  if (!seconds) {
    return '-';
  }
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return hours > 0 ? `${hours} 小时 ${minutes} 分` : `${minutes} 分`;
}

async function loadList() {
  loading.value = true;
  try {
    const res = await listSsoSessions();
    list.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function handleLogout() {
  ElMessageBox.confirm('确认退出当前用户全部平台会话吗？', '提示', { type: 'warning' })
    .then(async () => {
      loggingOut.value = true;
      try {
        await ssoLogout();
        ElMessage.success('已退出全部平台会话');
        loadList();
      } finally {
        loggingOut.value = false;
      }
    })
    .catch(() => {});
}

onMounted(loadList);
</script>
