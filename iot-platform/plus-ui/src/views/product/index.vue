<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="产品名称">
          <el-input v-model="query.productName" placeholder="请输入产品名称" clearable />
        </el-form-item>
        <el-form-item label="产品编码">
          <el-input v-model="query.productCode" placeholder="请输入产品编码" clearable />
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
          <el-button v-hasPermi="['iot:product:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:product:export']" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="productCode" label="产品编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productName" label="产品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="protocol" label="协议" width="100" />
        <el-table-column prop="deviceType" label="设备类型" width="130" />
        <el-table-column prop="dataFormat" label="数据格式" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:product:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:product:remove']" link type="danger" @click="handleDelete(row)">
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
        <el-form-item label="产品编码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入产品编码" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="请选择协议" style="width: 100%">
            <el-option label="MQTT" value="MQTT" />
            <el-option label="HTTPS" value="HTTPS" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="form.deviceType" placeholder="请输入设备类型" />
        </el-form-item>
        <el-form-item label="数据格式">
          <el-input v-model="form.dataFormat" placeholder="如 JSON" />
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
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { addProduct, delProduct, exportProduct, listProduct, updateProduct } from '@/api/product'
import { saveBlob } from '@/utils/save'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增产品')
const formRef = ref()
const list = ref([])
const total = ref(0)

const query = reactive({
  productName: '',
  productCode: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  productId: undefined,
  productCode: '',
  productName: '',
  protocol: 'MQTT',
  deviceType: '',
  dataFormat: 'JSON',
  remark: ''
})

const formRules = {
  productCode: [{ required: true, message: '产品编码不能为空', trigger: 'blur' }],
  productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  protocol: [{ required: true, message: '协议不能为空', trigger: 'change' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await listProduct(query)
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
  Object.assign(query, { productName: '', productCode: '', pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改产品'
    Object.assign(form, row)
  } else {
    dialogTitle.value = '新增产品'
    Object.assign(form, {
      productId: undefined,
      productCode: '',
      productName: '',
      protocol: 'MQTT',
      deviceType: '',
      dataFormat: 'JSON',
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.productId) {
      await updateProduct(form)
      ElMessage.success('修改成功')
    } else {
      await addProduct(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除产品「${row.productName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delProduct(row.productId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function handleExport() {
  const blob = await exportProduct(query)
  saveBlob(blob, `产品列表_${Date.now()}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>
