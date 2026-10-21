<template>
  <div class="channel-config">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="配置键">
          <el-input v-model="query.configKey" placeholder="请输入配置键" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input v-model="query.configName" placeholder="请输入配置名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="query.channel" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in channelOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
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
          <el-button v-hasPermi="['iot:pay:config']" type="primary" :icon="Plus" @click="openDialog()">
            新增配置
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadChannelConfigs" />
      </div>

      <el-table v-loading="loading" :data="configs" border>
        <el-table-column prop="configKey" label="配置键" width="180" show-overflow-tooltip />
        <el-table-column prop="configName" label="配置名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="渠道" width="140">
          <template #default="{ row }">{{ channelLabel(row.channel) }}</template>
        </el-table-column>
        <el-table-column prop="merchantId" label="商户号" width="160" show-overflow-tooltip />
        <el-table-column prop="appId" label="应用ID" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-hasPermi="['iot:pay:config']"
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              :loading="togglingId === row.configId"
              @change="val => handleStatusChange(row, val)"
            />
            <span class="status-text">{{ row.status === '0' ? '启用' : '停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:pay:config']" link type="primary" @click="openDialog(row)">修改</el-button>
            <el-button v-hasPermi="['iot:pay:config']" link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @size-change="handleSizeChange"
          @current-change="loadChannelConfigs"
        />
      </div>

      <el-dialog v-model="visible" :title="form.configId ? '修改渠道配置' : '新增渠道配置'" width="620px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
          <el-form-item label="配置键" prop="configKey">
            <el-input v-model="form.configKey" placeholder="唯一标识，如 wx_native_default" />
          </el-form-item>
          <el-form-item label="配置名称" prop="configName">
            <el-input v-model="form.configName" placeholder="配置名称" />
          </el-form-item>
          <el-form-item label="支付渠道" prop="channel">
            <el-select v-model="form.channel" placeholder="请选择支付渠道" style="width: 100%">
              <el-option v-for="c in channelOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="商户号" prop="merchantId">
            <el-input v-model="form.merchantId" placeholder="微信 mchId（支付宝可留空）" />
          </el-form-item>
          <el-form-item label="应用ID" prop="appId">
            <el-input v-model="form.appId" placeholder="微信 appId / 支付宝 appId" />
          </el-form-item>
          <el-form-item label="应用私钥" prop="privateKey">
            <el-input
              v-model="form.privateKey"
              type="textarea"
              :rows="3"
              :placeholder="form.configId ? '留空表示不修改' : 'PEM 私钥内容'"
            />
          </el-form-item>
          <el-form-item label="平台公钥" prop="publicKey">
            <el-input
              v-model="form.publicKey"
              type="textarea"
              :rows="3"
              :placeholder="form.configId ? '留空表示不修改' : '支付宝公钥（微信可留空）'"
            />
          </el-form-item>
          <el-form-item label="APIv3密钥" prop="apiKey">
            <el-input v-model="form.apiKey" placeholder="微信 APIv3 密钥（支付宝可留空）" />
          </el-form-item>
          <el-form-item label="证书序列号" prop="certPath">
            <el-input v-model="form.certPath" placeholder="微信商户证书序列号（支付宝可留空）" />
          </el-form-item>
          <el-form-item label="回调地址" prop="notifyUrl">
            <el-input v-model="form.notifyUrl" placeholder="支付结果异步通知地址" />
          </el-form-item>
          <el-form-item label="网关地址" prop="gatewayUrl">
            <el-input
              v-model="form.gatewayUrl"
              placeholder="留空使用生产网关；支付宝沙箱填 https://openapi-sandbox.dl.alipaydev.com/gateway.do"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio value="0">启用</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import { addPayChannel, delPayChannel, getPayChannel, listPayChannelPage, updatePayChannel } from '@/api/pay';

const emit = defineEmits(['refresh']);

const channelOptions = [
  { value: 'mock', label: '模拟支付' },
  { value: 'wx_native', label: '微信扫码支付' },
  { value: 'ali_qr', label: '支付宝扫码支付' }
];

const loading = ref(false);
const submitting = ref(false);
const togglingId = ref(null);
const configs = ref([]);
const visible = ref(false);
const formRef = ref();

const query = reactive({
  configKey: '',
  configName: '',
  channel: '',
  status: '',
  pageNum: 1,
  pageSize: 10
});
const total = ref(0);

const emptyForm = () => ({
  configId: undefined,
  configKey: '',
  configName: '',
  channel: '',
  merchantId: '',
  appId: '',
  privateKey: '',
  publicKey: '',
  apiKey: '',
  certPath: '',
  notifyUrl: '',
  gatewayUrl: '',
  status: '0',
  remark: ''
});
const form = reactive(emptyForm());

const rules = {
  configKey: [{ required: true, message: '配置键不能为空', trigger: 'blur' }],
  configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }]
};

function channelLabel(code) {
  const item = channelOptions.find(c => c.value === code);
  return item ? item.label : code;
}

async function loadChannelConfigs() {
  loading.value = true;
  try {
    const res = await listPayChannelPage(query);
    configs.value = res.data.rows || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSizeChange() {
  query.pageNum = 1;
  loadChannelConfigs();
}

function handleQuery() {
  query.pageNum = 1;
  loadChannelConfigs();
}

function resetQuery() {
  Object.assign(query, { configKey: '', configName: '', channel: '', status: '', pageNum: 1 });
  loadChannelConfigs();
}

async function handleStatusChange(row, val) {
  const prev = val === '0' ? '1' : '0';
  togglingId.value = row.configId;
  try {
    await updatePayChannel({ ...row, status: val });
    ElMessage.success(val === '0' ? '已启用' : '已停用');
    emit('refresh');
  } catch (e) {
    // 失败回滚开关状态
    row.status = prev;
  } finally {
    togglingId.value = null;
  }
}

function openDialog(row) {
  Object.assign(form, emptyForm());
  if (row) {
    getPayChannel(row.configId).then(res => {
      Object.assign(form, res.data);
      // 私钥/公钥不返回明文，编辑时留空表示不修改
      form.privateKey = '';
      form.publicKey = '';
    });
  }
  visible.value = true;
}

async function handleSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    if (form.configId) {
      await updatePayChannel(form);
      ElMessage.success('修改成功');
    } else {
      await addPayChannel(form);
      ElMessage.success('新增成功');
    }
    visible.value = false;
    loadChannelConfigs();
    emit('refresh');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除渠道配置「${row.configName || row.configKey}」？`, '提示', {
    type: 'warning'
  });
  await delPayChannel(row.configId);
  ElMessage.success('删除成功');
  loadChannelConfigs();
  emit('refresh');
}

onMounted(loadChannelConfigs);
</script>

<style scoped lang="scss">
.status-text {
  margin-left: 8px;
  color: var(--el-text-color-regular);
}
</style>
