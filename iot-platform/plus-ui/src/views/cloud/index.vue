<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ============================ 云账号 ============================ -->
      <el-tab-pane label="云账号" name="account">
        <div class="search-bar">
          <el-form :inline="true" :model="accountQuery">
            <el-form-item label="账号名称">
              <el-input v-model="accountQuery.accountName" placeholder="请输入账号名称" clearable />
            </el-form-item>
            <el-form-item label="云平台">
              <el-select v-model="accountQuery.provider" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="(label, value) in providerMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleAccountQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetAccountQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button
                v-hasPermi="['iot:cloud:account:add']"
                type="primary"
                :icon="Plus"
                @click="openAccountDialog()"
              >
                新增云账号
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadAccounts" />
          </div>

          <el-table v-loading="accountLoading" :data="accountList" border>
            <el-table-column prop="accountName" label="账号名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="云平台" width="110">
              <template #default="{ row }">
                <el-tag :type="providerTag(row.provider)">{{ providerMap[row.provider] || row.provider }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="regionId" label="默认地域" width="130" />
            <el-table-column prop="accessKey" label="AccessKeyId" width="180" show-overflow-tooltip />
            <el-table-column prop="endpoint" label="接入端点" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'">
                  {{ row.status === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button v-hasPermi="['iot:cloud:account:test']" link type="primary" @click="handleTest(row)">
                  测试连接
                </el-button>
                <el-button v-hasPermi="['iot:cloud:account:edit']" link type="primary" @click="openAccountDialog(row)">
                  编辑
                </el-button>
                <el-button
                  v-hasPermi="['iot:cloud:account:remove']"
                  link
                  type="danger"
                  @click="handleDeleteAccount(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="accountQuery.pageNum"
              v-model:page-size="accountQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="accountTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadAccounts"
              @current-change="loadAccounts"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ============================ 云资源 ============================ -->
      <el-tab-pane label="云资源" name="resource" lazy>
        <div class="search-bar">
          <el-form :inline="true" :model="resourceQuery">
            <el-form-item label="云账号">
              <el-select v-model="resourceQuery.accountId" placeholder="全部" clearable style="width: 180px">
                <el-option
                  v-for="item in accountList"
                  :key="item.accountId"
                  :label="item.accountName"
                  :value="item.accountId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="资源类型">
              <el-select v-model="resourceQuery.resourceType" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="(label, value) in resourceTypeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键字">
              <el-input v-model="resourceQuery.keyword" placeholder="实例ID / 实例名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleResourceQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetResourceQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button
                v-hasPermi="['iot:cloud:resource:add']"
                type="primary"
                :icon="Plus"
                @click="openResourceDialog"
              >
                创建资源
              </el-button>
              <el-button
                v-hasPermi="['iot:cloud:resource:sync']"
                type="success"
                :icon="Refresh"
                :disabled="!resourceQuery.accountId"
                @click="handleSyncResources"
              >
                同步资源
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadResources" />
          </div>

          <el-table v-loading="resourceLoading" :data="resourceList" border>
            <el-table-column prop="instanceId" label="实例ID" width="190" show-overflow-tooltip />
            <el-table-column prop="instanceName" label="实例名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="资源类型" width="110">
              <template #default="{ row }">
                {{ resourceTypeMap[row.resourceType] || row.resourceType }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="resourceStatusTag(row.status)">{{ row.status || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="regionId" label="地域" width="120" />
            <el-table-column prop="spec" label="规格" width="140" show-overflow-tooltip />
            <el-table-column prop="privateIp" label="内网IP" width="130" />
            <el-table-column prop="publicIp" label="公网IP" width="130" />
            <el-table-column prop="lastSyncTime" label="最近同步" width="170" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status !== 'Running'"
                  v-hasPermi="['iot:cloud:resource:operate']"
                  link
                  type="primary"
                  @click="handleStartResource(row)"
                >
                  启动
                </el-button>
                <el-button
                  v-else
                  v-hasPermi="['iot:cloud:resource:operate']"
                  link
                  type="warning"
                  @click="handleStopResource(row)"
                >
                  停止
                </el-button>
                <el-button
                  v-hasPermi="['iot:cloud:resource:operate']"
                  link
                  type="danger"
                  @click="handleReleaseResource(row)"
                >
                  释放
                </el-button>
                <el-button
                  v-hasPermi="['iot:cloud:resource:remove']"
                  link
                  type="info"
                  @click="handleRemoveResource(row)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="resourceQuery.pageNum"
              v-model:page-size="resourceQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="resourceTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadResources"
              @current-change="loadResources"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ============================ Flink 作业 ============================ -->
      <el-tab-pane label="Flink 作业" name="flink" lazy>
        <div class="search-bar">
          <el-form :inline="true" :model="flinkQuery">
            <el-form-item label="云账号">
              <el-select v-model="flinkQuery.accountId" placeholder="全部" clearable style="width: 180px">
                <el-option
                  v-for="item in flinkAccounts"
                  :key="item.accountId"
                  :label="item.accountName"
                  :value="item.accountId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="flinkQuery.status" placeholder="全部" clearable style="width: 140px">
                <el-option label="RUNNING" value="RUNNING" />
                <el-option label="FINISHED" value="FINISHED" />
                <el-option label="FAILED" value="FAILED" />
                <el-option label="CANCELED" value="CANCELED" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键字">
              <el-input v-model="flinkQuery.keyword" placeholder="作业名称 / 作业ID" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleFlinkQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetFlinkQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button
                v-hasPermi="['iot:cloud:flink:sync']"
                type="success"
                :icon="Refresh"
                :disabled="!flinkQuery.accountId"
                @click="handleSyncJobs"
              >
                同步作业
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadJobs" />
          </div>

          <el-table v-loading="flinkLoading" :data="jobList" border>
            <el-table-column prop="jobName" label="作业名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="flinkJobId" label="Flink 作业ID" width="240" show-overflow-tooltip />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="jobStatusTag(row.status)">{{ row.status || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="parallelism" label="并行度" width="90" />
            <el-table-column label="启动时间" width="170">
              <template #default="{ row }">{{ formatMillis(row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="运行时长" width="130">
              <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
            </el-table-column>
            <el-table-column prop="lastSyncTime" label="最近同步" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-hasPermi="['iot:cloud:flink:operate']"
                  link
                  type="warning"
                  :disabled="row.status !== 'RUNNING'"
                  @click="handleStopJob(row)"
                >
                  停止
                </el-button>
                <el-button
                  v-hasPermi="['iot:cloud:flink:operate']"
                  link
                  type="danger"
                  :disabled="row.status !== 'RUNNING'"
                  @click="handleCancelJob(row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="flinkQuery.pageNum"
              v-model:page-size="flinkQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="flinkTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadJobs"
              @current-change="loadJobs"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 云账号表单 -->
    <el-dialog v-model="accountVisible" :title="accountForm.accountId ? '编辑云账号' : '新增云账号'" width="600px">
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="110px">
        <el-form-item label="账号名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="请输入账号名称" />
        </el-form-item>
        <el-form-item label="云平台" prop="provider">
          <el-select v-model="accountForm.provider" placeholder="请选择云平台" style="width: 100%">
            <el-option v-for="(label, value) in providerMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认地域" prop="regionId">
          <el-input v-model="accountForm.regionId" placeholder="如 cn-hangzhou" />
        </el-form-item>
        <el-form-item v-if="accountForm.provider === 'aliyun'" label="AccessKeyId" prop="accessKey">
          <el-input v-model="accountForm.accessKey" placeholder="阿里云 AccessKeyId" />
        </el-form-item>
        <el-form-item v-if="accountForm.provider === 'aliyun'" label="AccessKeySecret" prop="accessSecret">
          <el-input
            v-model="accountForm.accessSecret"
            type="password"
            show-password
            placeholder="留空表示不修改"
          />
        </el-form-item>
        <el-form-item v-if="accountForm.provider === 'flink'" label="JobManager" prop="endpoint">
          <el-input v-model="accountForm.endpoint" placeholder="如 http://flink-jobmanager:8081" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="accountForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="accountForm.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAccountSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 云资源表单 -->
    <el-dialog v-model="resourceVisible" title="创建云资源" width="560px">
      <el-form ref="resourceFormRef" :model="resourceForm" :rules="resourceRules" label-width="110px">
        <el-form-item label="云账号" prop="accountId">
          <el-select v-model="resourceForm.accountId" placeholder="请选择云账号" style="width: 100%">
            <el-option
              v-for="item in accountList"
              :key="item.accountId"
              :label="item.accountName"
              :value="item.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="resourceForm.resourceType" placeholder="请选择资源类型" style="width: 100%">
            <el-option v-for="(label, value) in resourceTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="实例名称" prop="instanceName">
          <el-input v-model="resourceForm.instanceName" placeholder="请输入实例名称" />
        </el-form-item>
        <el-form-item label="地域" prop="regionId">
          <el-input v-model="resourceForm.regionId" placeholder="如 cn-hangzhou" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="resourceForm.spec" placeholder="如 ecs.g6.large" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="resourceForm.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleResourceSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import {
  addCloudAccount,
  addCloudResource,
  cancelFlinkJob,
  delCloudAccount,
  delCloudResource,
  listCloudAccounts,
  listCloudResources,
  listFlinkJobs,
  releaseCloudResource,
  startCloudResource,
  stopCloudResource,
  stopFlinkJob,
  syncCloudResources,
  syncFlinkJobs,
  testCloudAccount,
  updateCloudAccount
} from '@/api/cloud';

const activeTab = ref('account');

const providerMap = { mock: '内置模拟', aliyun: '阿里云', flink: 'Flink' };
const resourceTypeMap = { ecs: 'ECS 云服务器', 'flink-job': 'Flink 作业' };

const accountLoading = ref(false);
const resourceLoading = ref(false);
const flinkLoading = ref(false);
const submitting = ref(false);

const accountList = ref([]);
const accountTotal = ref(0);
const resourceList = ref([]);
const resourceTotal = ref(0);
const jobList = ref([]);
const flinkTotal = ref(0);

const accountQuery = reactive({ accountName: '', provider: '', pageNum: 1, pageSize: 10 });
const resourceQuery = reactive({ accountId: undefined, resourceType: '', keyword: '', pageNum: 1, pageSize: 10 });
const flinkQuery = reactive({ accountId: undefined, status: '', keyword: '', pageNum: 1, pageSize: 10 });

// 云账号下拉数据（资源/作业 tab 共用），首次进入各 tab 时拉取
const accountsLoaded = ref(false);
const flinkAccounts = computed(() => accountList.value.filter((item) => item.provider === 'flink'));

const accountVisible = ref(false);
const resourceVisible = ref(false);
const accountFormRef = ref();
const resourceFormRef = ref();

const accountForm = reactive({
  accountId: undefined,
  accountName: '',
  provider: 'aliyun',
  regionId: '',
  accessKey: '',
  accessSecret: '',
  endpoint: '',
  status: '0',
  remark: ''
});
const resourceForm = reactive({
  accountId: undefined,
  resourceType: 'ecs',
  instanceName: '',
  regionId: '',
  spec: '',
  remark: ''
});

const accountRules = {
  accountName: [{ required: true, message: '账号名称不能为空', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择云平台', trigger: 'change' }]
};
const resourceRules = {
  accountId: [{ required: true, message: '请选择云账号', trigger: 'change' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
};

function providerTag(provider) {
  return { mock: 'info', aliyun: 'warning', flink: 'primary' }[provider] || 'info';
}

function resourceStatusTag(status) {
  return { Running: 'success', Stopped: 'info', Starting: 'warning', Stopping: 'warning' }[status] || 'info';
}

function jobStatusTag(status) {
  return { RUNNING: 'success', FINISHED: 'info', FAILED: 'danger', CANCELED: 'warning' }[status] || 'info';
}

function formatMillis(millis) {
  if (!millis) return '-';
  const date = new Date(Number(millis));
  if (Number.isNaN(date.getTime())) return '-';
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`;
}

function formatDuration(millis) {
  if (!millis) return '-';
  const seconds = Math.floor(Number(millis) / 1000);
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}h ${m}m ${s}s`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

async function loadAccounts() {
  accountLoading.value = true;
  try {
    const res = await listCloudAccounts(accountQuery);
    accountList.value = res.data.rows || [];
    accountTotal.value = res.data.total || 0;
    accountsLoaded.value = true;
  } finally {
    accountLoading.value = false;
  }
}

async function ensureAccounts() {
  if (!accountsLoaded.value) {
    await loadAccounts();
  }
}

async function loadResources() {
  resourceLoading.value = true;
  try {
    const res = await listCloudResources(resourceQuery);
    resourceList.value = res.data.rows || [];
    resourceTotal.value = res.data.total || 0;
  } finally {
    resourceLoading.value = false;
  }
}

async function loadJobs() {
  flinkLoading.value = true;
  try {
    const res = await listFlinkJobs(flinkQuery);
    jobList.value = res.data.rows || [];
    flinkTotal.value = res.data.total || 0;
  } finally {
    flinkLoading.value = false;
  }
}

function handleAccountQuery() {
  accountQuery.pageNum = 1;
  loadAccounts();
}

function resetAccountQuery() {
  Object.assign(accountQuery, { accountName: '', provider: '', pageNum: 1 });
  loadAccounts();
}

function handleResourceQuery() {
  resourceQuery.pageNum = 1;
  loadResources();
}

function resetResourceQuery() {
  Object.assign(resourceQuery, { accountId: undefined, resourceType: '', keyword: '', pageNum: 1 });
  loadResources();
}

function handleFlinkQuery() {
  flinkQuery.pageNum = 1;
  loadJobs();
}

function resetFlinkQuery() {
  Object.assign(flinkQuery, { accountId: undefined, status: '', keyword: '', pageNum: 1 });
  loadJobs();
}

function handleTabChange(name) {
  if (name === 'resource') {
    ensureAccounts();
    loadResources();
  } else if (name === 'flink') {
    ensureAccounts();
    loadJobs();
  }
}

function resetAccountForm() {
  Object.assign(accountForm, {
    accountId: undefined,
    accountName: '',
    provider: 'aliyun',
    regionId: '',
    accessKey: '',
    accessSecret: '',
    endpoint: '',
    status: '0',
    remark: ''
  });
}

function openAccountDialog(row) {
  if (row) {
    Object.assign(accountForm, {
      accountId: row.accountId,
      accountName: row.accountName,
      provider: row.provider,
      regionId: row.regionId || '',
      accessKey: row.accessKey || '',
      accessSecret: '',
      endpoint: row.endpoint || '',
      status: row.status || '0',
      remark: row.remark || ''
    });
  } else {
    resetAccountForm();
  }
  accountVisible.value = true;
}

async function handleAccountSubmit() {
  await accountFormRef.value.validate();
  submitting.value = true;
  try {
    if (accountForm.accountId) {
      await updateCloudAccount(accountForm);
      ElMessage.success('修改成功');
    } else {
      await addCloudAccount(accountForm);
      ElMessage.success('创建成功');
    }
    accountVisible.value = false;
    loadAccounts();
  } finally {
    submitting.value = false;
  }
}

async function handleTest(row) {
  const res = await testCloudAccount(row.accountId);
  const result = res.data || {};
  if (result.success) {
    ElMessage.success(`连接成功，耗时 ${result.costMs} ms`);
  } else {
    ElMessage.error(`连接失败：${result.message || '未知错误'}`);
  }
}

async function handleDeleteAccount(row) {
  await ElMessageBox.confirm(
    `确认删除云账号「${row.accountName}」吗？其下纳管资源与 Flink 作业将一并移除。`,
    '提示',
    { type: 'warning' }
  );
  await delCloudAccount(row.accountId);
  ElMessage.success('删除成功');
  loadAccounts();
}

function openResourceDialog() {
  Object.assign(resourceForm, {
    accountId: resourceQuery.accountId || undefined,
    resourceType: 'ecs',
    instanceName: '',
    regionId: '',
    spec: '',
    remark: ''
  });
  resourceVisible.value = true;
}

async function handleResourceSubmit() {
  await resourceFormRef.value.validate();
  submitting.value = true;
  try {
    await addCloudResource(resourceForm);
    ElMessage.success('创建成功');
    resourceVisible.value = false;
    loadResources();
  } finally {
    submitting.value = false;
  }
}

async function handleSyncResources() {
  if (!resourceQuery.accountId) {
    ElMessage.warning('请先选择云账号');
    return;
  }
  resourceLoading.value = true;
  try {
    await syncCloudResources(resourceQuery.accountId, {
      resourceType: resourceQuery.resourceType,
      keyword: resourceQuery.keyword
    });
    ElMessage.success('同步完成');
    loadResources();
  } finally {
    resourceLoading.value = false;
  }
}

async function handleStartResource(row) {
  await startCloudResource(row.resourceId);
  ElMessage.success('启动指令已下发');
  loadResources();
}

async function handleStopResource(row) {
  await stopCloudResource(row.resourceId);
  ElMessage.success('停止指令已下发');
  loadResources();
}

async function handleReleaseResource(row) {
  await ElMessageBox.confirm(`确认释放资源「${row.instanceName || row.instanceId}」吗？云端实例将被删除。`, '提示', {
    type: 'warning'
  });
  await releaseCloudResource(row.resourceId);
  ElMessage.success('释放成功');
  loadResources();
}

async function handleRemoveResource(row) {
  await ElMessageBox.confirm(`确认移除本地纳管记录「${row.instanceName || row.instanceId}」吗？`, '提示', {
    type: 'warning'
  });
  await delCloudResource(row.resourceId);
  ElMessage.success('移除成功');
  loadResources();
}

async function handleSyncJobs() {
  if (!flinkQuery.accountId) {
    ElMessage.warning('请先选择 Flink 云账号');
    return;
  }
  flinkLoading.value = true;
  try {
    await syncFlinkJobs(flinkQuery.accountId);
    ElMessage.success('同步完成');
    loadJobs();
  } finally {
    flinkLoading.value = false;
  }
}

async function handleStopJob(row) {
  await stopFlinkJob(row.jobId);
  ElMessage.success('停止指令已下发');
  loadJobs();
}

async function handleCancelJob(row) {
  await ElMessageBox.confirm(`确认取消作业「${row.jobName}」吗？`, '提示', { type: 'warning' });
  await cancelFlinkJob(row.jobId);
  ElMessage.success('取消指令已下发');
  loadJobs();
}

onMounted(() => {
  loadAccounts();
});
</script>
