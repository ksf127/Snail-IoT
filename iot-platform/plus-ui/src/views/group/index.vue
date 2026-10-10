<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="分组名称">
          <el-input v-model="query.groupName" placeholder="请输入分组名称" clearable />
        </el-form-item>
        <el-form-item label="分组编码">
          <el-input v-model="query.groupCode" placeholder="请输入分组编码" clearable />
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
          <el-button v-hasPermi="['iot:group:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:group:export']" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="groupCode" label="分组编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="groupName" label="分组名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="parentName" label="父分组" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:group:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:group:remove']" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="分组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组编码" prop="groupCode">
          <el-input v-model="form.groupCode" placeholder="请输入分组编码" />
        </el-form-item>
        <el-form-item label="父分组">
          <el-select v-model="form.parentId" placeholder="请选择父分组（可空）" clearable style="width: 100%">
            <el-option
              v-for="g in parentOptions"
              :key="g.groupId"
              :label="g.groupName"
              :value="g.groupId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderNum" :min="0" :max="9999" style="width: 100%" />
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { addGroup, delGroup, exportGroup, listGroup, updateGroup } from '@/api/group'
import { saveBlob } from '@/utils/save'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分组')
const formRef = ref()
const list = ref([])

const query = reactive({
  groupName: '',
  groupCode: ''
})

const form = reactive({
  groupId: undefined,
  groupCode: '',
  groupName: '',
  parentId: undefined,
  orderNum: 0,
  status: '0',
  remark: ''
})

const formRules = {
  groupCode: [{ required: true, message: '分组编码不能为空', trigger: 'blur' }],
  groupName: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }]
}

// 父分组下拉：排除当前编辑的分组，避免自引用
const parentOptions = computed(() =>
  list.value.filter((g) => g.groupId !== form.groupId)
)

async function loadList() {
  loading.value = true
  try {
    const res = await listGroup(query)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  loadList()
}

function resetQuery() {
  Object.assign(query, { groupName: '', groupCode: '' })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改分组'
    Object.assign(form, {
      groupId: row.groupId,
      groupCode: row.groupCode,
      groupName: row.groupName,
      parentId: row.parentId,
      orderNum: row.orderNum,
      status: row.status,
      remark: row.remark
    })
  } else {
    dialogTitle.value = '新增分组'
    Object.assign(form, {
      groupId: undefined,
      groupCode: '',
      groupName: '',
      parentId: undefined,
      orderNum: 0,
      status: '0',
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.groupId) {
      await updateGroup(form)
      ElMessage.success('修改成功')
    } else {
      await addGroup(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除分组「${row.groupName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delGroup(row.groupId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function handleExport() {
  const blob = await exportGroup(query)
  saveBlob(blob, `分组列表_${Date.now()}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>