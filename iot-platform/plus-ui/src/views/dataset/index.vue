<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="数据集名称">
          <el-input v-model="query.datasetName" placeholder="请输入数据集名称" clearable />
        </el-form-item>
        <el-form-item label="数据集编码">
          <el-input v-model="query.datasetCode" placeholder="请输入数据集编码" clearable />
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
          <el-button v-hasPermi="['iot:dataset:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:dataset:export']" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="datasetCode" label="数据集编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="datasetName" label="数据集名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="groupName" label="所属分组" min-width="140" show-overflow-tooltip />
        <el-table-column prop="datasetType" label="类型" width="110" />
        <el-table-column prop="sourceTable" label="来源表" min-width="140" show-overflow-tooltip />
        <el-table-column prop="rowCount" label="数据量" width="100" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:dataset:auth']" link type="warning" @click="openAuthDialog(row)">
              数据权限
            </el-button>
            <el-button v-hasPermi="['iot:dataset:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:dataset:remove']" link type="danger" @click="handleDelete(row)">
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
        <el-form-item label="数据集名称" prop="datasetName">
          <el-input v-model="form.datasetName" placeholder="请输入数据集名称" />
        </el-form-item>
        <el-form-item label="数据集编码" prop="datasetCode">
          <el-input v-model="form.datasetCode" placeholder="请输入数据集编码" />
        </el-form-item>
        <el-form-item label="所属分组" prop="groupId">
          <el-select v-model="form.groupId" placeholder="请选择所属分组" style="width: 100%">
            <el-option
              v-for="g in groupOptions"
              :key="g.groupId"
              :label="g.groupName"
              :value="g.groupId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.datasetType" placeholder="请选择类型" style="width: 100%">
            <el-option label="设备数据" value="device" />
            <el-option label="业务数据" value="business" />
            <el-option label="外部数据" value="external" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源表">
          <el-input v-model="form.sourceTable" placeholder="如 iot_device" />
        </el-form-item>
        <el-form-item label="数据量">
          <el-input-number v-model="form.rowCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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

    <el-dialog v-model="authVisible" title="角色数据权限" width="640px">
      <div class="auth-toolbar">
        <el-button type="primary" :icon="Plus" @click="addScopeRow">新增角色授权</el-button>
      </div>
      <el-table :data="scopes" border>
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.roleId" placeholder="请选择角色" style="width: 100%">
              <el-option
                v-for="r in roleOptions"
                :key="r.roleId"
                :label="r.roleName"
                :value="r.roleId"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数据权限" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.dataScope" placeholder="请选择数据权限" style="width: 100%">
              <el-option
                v-for="d in dataScopeOptions"
                :key="d.value"
                :label="d.label"
                :value="d.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" @click="scopes.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="authVisible = false">取消</el-button>
        <el-button type="primary" :loading="authSubmitting" @click="handleAuthSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  addDataset,
  delDataset,
  exportDataset,
  getDatasetRoles,
  listDataset,
  saveDatasetRoles,
  updateDataset
} from '@/api/dataset'
import { listGroup } from '@/api/group'
import { roleOptionSelect } from '@/api/system/roleOption'
import { saveBlob } from '@/utils/save'

const dataScopeOptions = [
  { value: '1', label: '全部数据权限' },
  { value: '2', label: '自定数据权限' },
  { value: '3', label: '本部门数据权限' },
  { value: '4', label: '本部门及以下数据权限' },
  { value: '5', label: '仅本人数据权限' }
]

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据集')
const formRef = ref()
const list = ref([])
const total = ref(0)
const groupOptions = ref([])
const roleOptions = ref([])

const authVisible = ref(false)
const authSubmitting = ref(false)
const authDatasetId = ref()
const scopes = ref([])

const query = reactive({
  datasetName: '',
  datasetCode: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  datasetId: undefined,
  datasetCode: '',
  datasetName: '',
  groupId: undefined,
  datasetType: 'device',
  sourceTable: '',
  description: '',
  rowCount: 0,
  status: '0',
  remark: ''
})

const formRules = {
  datasetCode: [{ required: true, message: '数据集编码不能为空', trigger: 'blur' }],
  datasetName: [{ required: true, message: '数据集名称不能为空', trigger: 'blur' }],
  groupId: [{ required: true, message: '请选择所属分组', trigger: 'change' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listDataset(query)
    list.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadGroupOptions() {
  const res = await listGroup({})
  groupOptions.value = res.data || []
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
  Object.assign(query, { datasetName: '', datasetCode: '', pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改数据集'
    Object.assign(form, {
      datasetId: row.datasetId,
      datasetCode: row.datasetCode,
      datasetName: row.datasetName,
      groupId: row.groupId,
      datasetType: row.datasetType,
      sourceTable: row.sourceTable,
      description: row.description,
      rowCount: row.rowCount,
      status: row.status,
      remark: row.remark
    })
  } else {
    dialogTitle.value = '新增数据集'
    Object.assign(form, {
      datasetId: undefined,
      datasetCode: '',
      datasetName: '',
      groupId: undefined,
      datasetType: 'device',
      sourceTable: '',
      description: '',
      rowCount: 0,
      status: '0',
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.datasetId) {
      await updateDataset(form)
      ElMessage.success('修改成功')
    } else {
      await addDataset(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除数据集「${row.datasetName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delDataset(row.datasetId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function handleExport() {
  const blob = await exportDataset(query)
  saveBlob(blob, `数据集列表_${Date.now()}.xlsx`)
  ElMessage.success('导出成功')
}

async function openAuthDialog(row) {
  authDatasetId.value = row.datasetId
  const res = await getDatasetRoles(row.datasetId)
  scopes.value = (res.data || []).map((s) => ({ roleId: s.roleId, dataScope: s.dataScope }))
  authVisible.value = true
}

function addScopeRow() {
  scopes.value.push({ roleId: undefined, dataScope: '5' })
}

async function handleAuthSubmit() {
  const valid = scopes.value.filter((s) => s.roleId)
  await saveDatasetRoles(authDatasetId.value, valid)
  ElMessage.success('保存成功')
  authVisible.value = false
}

onMounted(() => {
  loadList()
  loadGroupOptions()
  loadRoleOptions()
})
</script>