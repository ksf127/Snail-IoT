<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="应用名称">
          <el-input v-model="query.appName" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="应用编码">
          <el-input v-model="query.appCode" placeholder="请输入应用编码" clearable />
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
          <el-button v-hasPermi="['iot:app:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:app:export']" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="appCode" label="应用编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="appName" label="应用名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="appType" label="应用类型" width="110" />
        <el-table-column prop="accessUrl" label="访问地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="datasetCount" label="数据集数" width="90" align="center" />
        <el-table-column prop="roleCount" label="角色数" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:app:auth']" link type="warning" @click="openDatasetDialog(row)">
              绑定数据集
            </el-button>
            <el-button v-hasPermi="['iot:app:auth']" link type="primary" @click="openRoleDialog(row)">
              授权角色
            </el-button>
            <el-button v-hasPermi="['iot:app:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:app:remove']" link type="danger" @click="handleDelete(row)">
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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="form.appCode" placeholder="请输入应用编码" />
        </el-form-item>
        <el-form-item label="应用类型">
          <el-select v-model="form.appType" placeholder="请选择应用类型" style="width: 100%">
            <el-option label="Web" value="web" />
            <el-option label="移动端" value="mobile" />
            <el-option label="小程序" value="miniapp" />
          </el-select>
        </el-form-item>
        <el-form-item label="访问地址">
          <el-input v-model="form.accessUrl" placeholder="如 https://app.example.com" />
        </el-form-item>
        <el-form-item label="应用密钥">
          <el-input v-model="form.appSecret" placeholder="应用接入密钥" show-password />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-value="0"
            inactive-value="1"
            active-text="正常"
            inactive-text="停用"
          />
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

    <el-dialog v-model="datasetVisible" title="绑定数据集" width="560px">
      <el-checkbox-group v-model="selectedDatasets" style="display: flex; flex-direction: column">
        <el-checkbox
          v-for="d in datasetOptions"
          :key="d.datasetId"
          :value="d.datasetId"
          :label="d.datasetId"
          style="margin-right: 0; height: 32px"
        >
          {{ d.datasetName }}
          <el-text size="small" type="info">（{{ d.groupName || '未分组' }}）</el-text>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="datasetVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindSubmitting" @click="handleDatasetSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="授权角色" width="560px">
      <el-checkbox-group v-model="selectedRoles" style="display: flex; flex-direction: column">
        <el-checkbox
          v-for="r in roleOptions"
          :key="r.roleId"
          :value="r.roleId"
          :label="r.roleId"
          style="margin-right: 0; height: 32px"
        >
          {{ r.roleName }}
          <el-text size="small" type="info">（{{ r.roleKey }}）</el-text>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindSubmitting" @click="handleRoleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  addApp,
  delApp,
  exportApp,
  getAppDatasets,
  getAppRoles,
  listApp,
  saveAppDatasets,
  saveAppRoles,
  updateApp
} from '@/api/app'
import { datasetOptions as fetchDatasetOptions } from '@/api/dataset'
import { roleOptionSelect } from '@/api/system/roleOption'
import { saveBlob } from '@/utils/save'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增应用')
const formRef = ref()
const list = ref([])
const total = ref(0)

const datasetOptions = ref([])
const roleOptions = ref([])

const datasetVisible = ref(false)
const roleVisible = ref(false)
const bindSubmitting = ref(false)
const bindAppId = ref()
const selectedDatasets = ref([])
const selectedRoles = ref([])

const query = reactive({
  appName: '',
  appCode: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  appId: undefined,
  appCode: '',
  appName: '',
  appType: 'web',
  accessUrl: '',
  appSecret: '',
  status: '0',
  remark: ''
})

const formRules = {
  appCode: [{ required: true, message: '应用编码不能为空', trigger: 'blur' }],
  appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listApp(query)
    list.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadDatasetOptions() {
  const res = await fetchDatasetOptions({})
  datasetOptions.value = res.data || []
}

async function loadRoleOptions() {
  const res = await roleOptionSelect()
  roleOptions.value = res.data || []
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  Object.assign(query, { appName: '', appCode: '', pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改应用'
    Object.assign(form, {
      appId: row.appId,
      appCode: row.appCode,
      appName: row.appName,
      appType: row.appType,
      accessUrl: row.accessUrl,
      appSecret: row.appSecret,
      status: row.status,
      remark: row.remark
    })
  } else {
    dialogTitle.value = '新增应用'
    Object.assign(form, {
      appId: undefined,
      appCode: '',
      appName: '',
      appType: 'web',
      accessUrl: '',
      appSecret: '',
      status: '0',
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.appId) {
      await updateApp(form)
      ElMessage.success('修改成功')
    } else {
      await addApp(form)
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
      await delApp(row.appId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function handleExport() {
  const blob = await exportApp(query)
  saveBlob(blob, `应用列表_${Date.now()}.xlsx`)
  ElMessage.success('导出成功')
}

async function openDatasetDialog(row) {
  bindAppId.value = row.appId
  const res = await getAppDatasets(row.appId)
  selectedDatasets.value = res.data || []
  datasetVisible.value = true
}

async function handleDatasetSubmit() {
  bindSubmitting.value = true
  try {
    await saveAppDatasets(bindAppId.value, selectedDatasets.value)
    ElMessage.success('保存成功')
    datasetVisible.value = false
    loadList()
  } finally {
    bindSubmitting.value = false
  }
}

async function openRoleDialog(row) {
  bindAppId.value = row.appId
  const res = await getAppRoles(row.appId)
  selectedRoles.value = res.data || []
  roleVisible.value = true
}

async function handleRoleSubmit() {
  bindSubmitting.value = true
  try {
    await saveAppRoles(bindAppId.value, selectedRoles.value)
    ElMessage.success('保存成功')
    roleVisible.value = false
    loadList()
  } finally {
    bindSubmitting.value = false
  }
}

onMounted(() => {
  loadList()
  loadDatasetOptions()
  loadRoleOptions()
})
</script>