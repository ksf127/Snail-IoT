<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="档案类型">
          <el-input v-model="query.archiveType" placeholder="请输入档案类型" clearable />
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="query.bizId" placeholder="请输入业务ID" clearable />
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
          <el-button v-hasPermi="['iot:archive:add']" type="primary" :icon="FolderAdd" @click="dialogVisible = true">
            归档文件
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="archiveId" label="档案ID" width="180" show-overflow-tooltip />
        <el-table-column prop="archiveType" label="档案类型" width="140" />
        <el-table-column prop="bizId" label="业务ID" width="180" show-overflow-tooltip />
        <el-table-column prop="ossId" label="文件ID" width="180" show-overflow-tooltip />
        <el-table-column prop="archiveTime" label="归档时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" title="归档文件" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="档案类型" prop="archiveType">
          <el-input v-model="form.archiveType" placeholder="请输入档案类型" />
        </el-form-item>
        <el-form-item label="业务ID" prop="bizId">
          <el-input v-model="form.bizId" placeholder="请输入业务主键" />
        </el-form-item>
        <el-form-item label="文件ID" prop="ossId">
          <el-input v-model="form.ossId" placeholder="请输入对象存储文件ID" />
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
import { ElMessage } from 'element-plus'
import { FolderAdd, Refresh, Search } from '@element-plus/icons-vue'
import { archiveFile, listArchive } from '@/api/archive'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const list = ref([])
const total = ref(0)

const query = reactive({ archiveType: '', bizId: '', pageNum: 1, pageSize: 10 })
const form = reactive({ archiveType: '', bizId: '', ossId: '' })

const formRules = {
  archiveType: [{ required: true, message: '档案类型不能为空', trigger: 'blur' }],
  bizId: [{ required: true, message: '业务ID不能为空', trigger: 'blur' }],
  ossId: [{ required: true, message: '文件ID不能为空', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listArchive(query)
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
  Object.assign(query, { archiveType: '', bizId: '', pageNum: 1 })
  loadList()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await archiveFile(form)
    ElMessage.success('归档成功')
    dialogVisible.value = false
    Object.assign(form, { archiveType: '', bizId: '', ossId: '' })
    loadList()
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>
