<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="环境编码">
          <el-input v-model="query.envCode" placeholder="请输入环境编码" clearable />
        </el-form-item>
        <el-form-item label="环境名称">
          <el-input v-model="query.envName" placeholder="请输入环境名称" clearable />
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
          <el-button v-hasPermi="['iot:deploy:env:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增环境
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="envCode" label="环境编码" width="130" />
        <el-table-column prop="envName" label="环境名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="k8sNamespace" label="命名空间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="webNodePort" label="NodePort" width="110" />
        <el-table-column prop="clusterName" label="集群" width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:deploy:env:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:deploy:env:remove']" link type="danger" @click="handleDelete(row)">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="环境编码" prop="envCode">
          <el-input v-model="form.envCode" placeholder="如 dev / test / prod" :disabled="!!form.envId" />
        </el-form-item>
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="form.envName" placeholder="请输入环境名称" />
        </el-form-item>
        <el-form-item label="命名空间" prop="k8sNamespace">
          <el-input v-model="form.k8sNamespace" placeholder="Kubernetes Namespace" />
        </el-form-item>
        <el-form-item label="NodePort">
          <el-input-number v-model="form.webNodePort" :min="30000" :max="32767" />
        </el-form-item>
        <el-form-item label="集群名称">
          <el-input v-model="form.clusterName" placeholder="如 docker-desktop" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { addDeployEnv, delDeployEnv, listDeployEnvPage, updateDeployEnv } from '@/api/deploy'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增环境')
const formRef = ref()
const list = ref([])
const total = ref(0)

const query = reactive({
  envCode: '',
  envName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  envId: undefined,
  envCode: '',
  envName: '',
  k8sNamespace: '',
  webNodePort: 30080,
  clusterName: 'docker-desktop',
  status: '0',
  sortNo: 0,
  remark: ''
})

const formRules = {
  envCode: [{ required: true, message: '环境编码不能为空', trigger: 'blur' }],
  envName: [{ required: true, message: '环境名称不能为空', trigger: 'blur' }],
  k8sNamespace: [{ required: true, message: '命名空间不能为空', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listDeployEnvPage(query)
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
  Object.assign(query, { envCode: '', envName: '', status: '', pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改环境'
    Object.assign(form, row)
  } else {
    dialogTitle.value = '新增环境'
    Object.assign(form, {
      envId: undefined,
      envCode: '',
      envName: '',
      k8sNamespace: '',
      webNodePort: 30080,
      clusterName: 'docker-desktop',
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
    if (form.envId) {
      await updateDeployEnv(form)
      ElMessage.success('修改成功')
    } else {
      await addDeployEnv(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除环境「${row.envName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delDeployEnv(row.envId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

onMounted(loadList)
</script>
