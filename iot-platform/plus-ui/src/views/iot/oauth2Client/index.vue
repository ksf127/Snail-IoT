<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="客户端标识">
          <el-input v-model="query.clientId" placeholder="请输入客户端标识" clearable />
        </el-form-item>
        <el-form-item label="客户端名称">
          <el-input v-model="query.clientName" placeholder="请输入客户端名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <div>
          <el-button v-hasPermi="['iot:oauth2Client:add']" type="primary" :icon="Plus" @click="openDialog()">
            新增
          </el-button>
          <el-button v-hasPermi="['iot:oauth2Client:export']" :icon="Download" @click="handleExport">导出</el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="clientId" label="客户端标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="clientName" label="客户端名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="deviceType" label="设备类型" width="100" />
        <el-table-column prop="contractScopes" label="签约 Scope" min-width="200" show-overflow-tooltip />
        <el-table-column prop="allowGrantTypes" label="授权类型" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:oauth2Client:edit']" link type="primary" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['iot:oauth2Client:remove']" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="140px">
        <el-form-item label="客户端标识" prop="clientId">
          <el-input v-model="form.clientId" placeholder="如 iot-web" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="客户端密钥" prop="clientSecret">
          <el-input v-model="form.clientSecret" placeholder="请输入客户端密钥" />
        </el-form-item>
        <el-form-item label="客户端名称">
          <el-input v-model="form.clientName" placeholder="请输入客户端名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="form.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="PC" value="pc" />
            <el-option label="Android" value="android" />
            <el-option label="iOS" value="ios" />
            <el-option label="H5" value="h5" />
          </el-select>
        </el-form-item>
        <el-form-item label="签约 Scope">
          <el-input v-model="form.contractScopes" placeholder="逗号分隔，如 openid,userid,userinfo" />
        </el-form-item>
        <el-form-item label="允许重定向地址">
          <el-input v-model="form.allowRedirectUris" type="textarea" :rows="2" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="允许授权类型">
          <el-input v-model="form.allowGrantTypes" type="textarea" :rows="2" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="授权对象">
          <el-input v-model="form.subjectId" placeholder="为空时默认取当前登录用户ID" />
        </el-form-item>
        <el-form-item label="AccessToken 有效期">
          <el-input-number v-model="form.accessTokenTimeout" :min="0" :step="600" style="width: 200px" />
          <span class="unit-tip">秒</span>
        </el-form-item>
        <el-form-item label="RefreshToken 有效期">
          <el-input-number v-model="form.refreshTokenTimeout" :min="0" :step="600" style="width: 200px" />
          <span class="unit-tip">秒</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
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
import { Download, Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  addOauth2Client,
  delOauth2Client,
  exportOauth2Client,
  listOauth2Client,
  updateOauth2Client
} from '@/api/oauth2Client';
import { saveBlob } from '@/utils/save';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('新增客户端');
const formRef = ref();
const list = ref([]);

const query = reactive({ clientId: '', clientName: '', status: undefined });

const defaultForm = () => ({
  id: undefined,
  clientId: '',
  clientSecret: '',
  clientName: '',
  deviceType: 'pc',
  contractScopes: 'openid,userid,userinfo',
  allowRedirectUris: '',
  allowGrantTypes: 'authorization_code,password,refresh_token,client_credentials',
  subjectId: '',
  accessTokenTimeout: 7200,
  refreshTokenTimeout: 2592000,
  status: '0',
  remark: ''
});

const form = reactive(defaultForm());

const formRules = {
  clientId: [{ required: true, message: '客户端标识不能为空', trigger: 'blur' }],
  clientSecret: [{ required: true, message: '客户端密钥不能为空', trigger: 'blur' }]
};

async function loadList() {
  loading.value = true;
  try {
    const res = await listOauth2Client(query);
    list.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  Object.assign(query, { clientId: '', clientName: '', status: undefined });
  loadList();
}

function openDialog(row) {
  dialogVisible.value = true;
  if (row) {
    dialogTitle.value = '修改客户端';
    Object.assign(form, row);
  } else {
    dialogTitle.value = '新增客户端';
    Object.assign(form, defaultForm());
  }
}

async function handleSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    if (form.id) {
      await updateOauth2Client(form);
      ElMessage.success('修改成功');
    } else {
      await addOauth2Client(form);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    loadList();
  } finally {
    submitting.value = false;
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除客户端「${row.clientName || row.clientId}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await delOauth2Client(row.id);
      ElMessage.success('删除成功');
      loadList();
    })
    .catch(() => {});
}

async function handleExport() {
  const blob = await exportOauth2Client(query);
  saveBlob(blob, `OAuth2客户端_${Date.now()}.xlsx`);
  ElMessage.success('导出成功');
}

onMounted(loadList);
</script>

<style scoped>
.unit-tip {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
