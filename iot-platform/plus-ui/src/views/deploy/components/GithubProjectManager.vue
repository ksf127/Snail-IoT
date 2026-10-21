<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="项目名 / 描述"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="开发语言">
          <el-input v-model="query.language" placeholder="如 Java" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-alert
        class="repo-tip"
        type="info"
        :closable="false"
        show-icon
        title="管理员可点击【同步 GitHub 项目】拉取仓库列表并给账号授权；普通账号仅能看到已授权给自己的项目。"
      />
      <div class="table-toolbar">
        <div>
          <el-button
            v-hasPermi="['iot:deploy:github:sync']"
            type="primary"
            :icon="Refresh"
            :loading="syncing"
            @click="handleSync"
          >
            同步 GitHub 项目
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column label="项目" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <a :href="row.htmlUrl" target="_blank" rel="noopener" class="repo-link">{{ row.fullName }}</a>
            <div class="repo-desc">{{ row.description || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="language" label="开发语言" width="120">
          <template #default="{ row }">{{ row.language || '—' }}</template>
        </el-table-column>
        <el-table-column label="可见性" width="100">
          <template #default="{ row }">
            <el-tag :type="row.privateFlag === '1' ? 'warning' : 'success'" effect="plain">
              {{ row.privateFlag === '1' ? '私有' : '公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultBranch" label="默认分支" width="120" />
        <el-table-column prop="pushedAt" label="最近推送" width="180">
          <template #default="{ row }">{{ row.pushedAt || '—' }}</template>
        </el-table-column>
        <el-table-column prop="userCount" label="授权账号" width="100" />
        <el-table-column prop="syncTime" label="同步时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-hasPermi="['iot:deploy:github:auth']"
              link
              type="primary"
              @click="openAuth(row)"
            >
              授权
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

    <el-dialog v-model="authVisible" :title="`授权账号 · ${authRow.fullName || ''}`" width="560px">
      <el-form label-width="90px">
        <el-form-item label="可见账号">
          <el-select
            v-model="selectedUserIds"
            multiple
            filterable
            remote
            reserve-keyword
            :remote-method="searchUsers"
            :loading="userLoading"
            placeholder="输入用户名搜索并选择"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.userId"
              :label="`${user.userName}（${user.nickName || '-'}）`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="保存后立即生效：仅被选中的账号能在「GitHub 项目」页看到该项目，管理员始终可见全部项目。"
      />
      <template #footer>
        <el-button @click="authVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAuth" @click="submitAuth">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import {
  getGithubRepoUsers,
  listGithubRepoPage,
  syncGithubRepos,
  updateGithubRepoUsers
} from '@/api/deploy'
import { listUser, optionSelect } from '@/api/system/user'

const loading = ref(false)
const syncing = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  language: '',
  pageNum: 1,
  pageSize: 10
})

/* ---------------- 授权 ---------------- */

const authVisible = ref(false)
const savingAuth = ref(false)
const authRow = ref({})
const selectedUserIds = ref([])
const userOptions = ref([])
const userLoading = ref(false)

function mergeUsers(users) {
  const merged = new Map(userOptions.value.map((user) => [user.userId, user]))
  users.forEach((user) => merged.set(user.userId, user))
  userOptions.value = Array.from(merged.values())
}

async function searchUsers(keyword) {
  if (!keyword) {
    return
  }
  userLoading.value = true
  try {
    const res = await listUser({ pageNum: 1, pageSize: 20, userName: keyword })
    mergeUsers(res.data.rows || [])
  } finally {
    userLoading.value = false
  }
}

async function openAuth(row) {
  authRow.value = row
  selectedUserIds.value = []
  userOptions.value = []
  authVisible.value = true
  const res = await getGithubRepoUsers(row.repoId)
  const userIds = res.data || []
  selectedUserIds.value = userIds
  if (userIds.length) {
    const authorized = await optionSelect(userIds)
    mergeUsers(authorized.data || [])
  }
}

async function submitAuth() {
  savingAuth.value = true
  try {
    await updateGithubRepoUsers(authRow.value.repoId, selectedUserIds.value)
    ElMessage.success('授权已保存')
    authVisible.value = false
    loadList()
  } finally {
    savingAuth.value = false
  }
}

/* ---------------- 列表 ---------------- */

async function loadList() {
  loading.value = true
  try {
    const res = await listGithubRepoPage(query)
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
  Object.assign(query, { keyword: '', language: '', pageNum: 1 })
  loadList()
}

async function handleSync() {
  syncing.value = true
  try {
    const res = await syncGithubRepos()
    ElMessage.success(`同步完成，共 ${res.data ?? 0} 个项目`)
    query.pageNum = 1
    loadList()
  } finally {
    syncing.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.repo-tip {
  margin-bottom: 12px;
}

.repo-link {
  color: var(--el-color-primary);
  text-decoration: none;
}

.repo-link:hover {
  text-decoration: underline;
}

.repo-desc {
  margin-top: 2px;
  overflow: hidden;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
