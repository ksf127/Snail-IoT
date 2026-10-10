<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="设备名称">
          <el-input v-model="query.deviceName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="设备编码">
          <el-input v-model="query.deviceCode" placeholder="请输入设备编码" clearable />
        </el-form-item>
        <el-form-item label="在线状态">
          <el-select v-model="query.onlineStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="未知" :value="2" />
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
          <el-button v-hasPermi="['iot:device:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:device:export']" :icon="Download" @click="handleExport">
            导出
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="deviceCode" label="设备编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="productName" label="所属产品" min-width="140" show-overflow-tooltip />
        <el-table-column prop="protocol" label="协议" width="100" />
        <el-table-column prop="firmwareVersion" label="固件版本" width="110" />
        <el-table-column label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="onlineTagType(row.onlineStatus)">{{ onlineText(row.onlineStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
        <el-table-column prop="lastReportTime" label="最近上报" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:device:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:device:remove']" link type="danger" @click="handleDelete(row)">
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
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="所属产品" prop="productId">
          <el-input v-model="form.productId" placeholder="请输入产品ID" />
        </el-form-item>
        <el-form-item label="协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="请选择协议" style="width: 100%">
            <el-option label="MQTT" value="MQTT" />
            <el-option label="HTTPS" value="HTTPS" />
          </el-select>
        </el-form-item>
        <el-form-item label="固件版本">
          <el-input v-model="form.firmwareVersion" placeholder="请输入固件版本" />
        </el-form-item>
        <el-form-item label="IP 地址">
          <el-input v-model="form.ipAddress" placeholder="请输入 IP 地址" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.location" placeholder="请输入安装位置" />
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
import { addDevice, delDevice, exportDevice, listDevice, updateDevice } from '@/api/device'
import { saveBlob } from '@/utils/save'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const formRef = ref()
const list = ref([])
const total = ref(0)

const query = reactive({
  deviceName: '',
  deviceCode: '',
  onlineStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  deviceId: undefined,
  deviceCode: '',
  deviceName: '',
  productId: '',
  protocol: 'MQTT',
  firmwareVersion: '',
  ipAddress: '',
  location: '',
  remark: ''
})

const formRules = {
  deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  productId: [{ required: true, message: '所属产品不能为空', trigger: 'blur' }],
  protocol: [{ required: true, message: '协议不能为空', trigger: 'change' }]
}

function onlineText(status) {
  return { 0: '离线', 1: '在线', 2: '未知' }[status] ?? '未知'
}

function onlineTagType(status) {
  return { 0: 'info', 1: 'success', 2: 'warning' }[status] ?? 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await listDevice(query)
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
  Object.assign(query, { deviceName: '', deviceCode: '', onlineStatus: undefined, pageNum: 1 })
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改设备'
    Object.assign(form, row)
  } else {
    dialogTitle.value = '新增设备'
    Object.assign(form, {
      deviceId: undefined,
      deviceCode: '',
      deviceName: '',
      productId: '',
      protocol: 'MQTT',
      firmwareVersion: '',
      ipAddress: '',
      location: '',
      remark: ''
    })
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.deviceId) {
      await updateDevice(form)
      ElMessage.success('修改成功')
    } else {
      await addDevice(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除设备「${row.deviceName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delDevice(row.deviceId)
      ElMessage.success('删除成功')
      loadList()
    })
    .catch(() => {})
}

async function handleExport() {
  const blob = await exportDevice(query)
  saveBlob(blob, `设备列表_${Date.now()}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>
