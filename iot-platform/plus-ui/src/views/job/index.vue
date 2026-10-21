<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ============================ 任务列表 ============================ -->
      <el-tab-pane label="任务列表" name="info">
        <div class="search-bar">
          <el-form :inline="true" :model="infoQuery">
            <el-form-item label="任务名称">
              <el-input v-model="infoQuery.jobName" placeholder="请输入任务名称" clearable />
            </el-form-item>
            <el-form-item label="任务类型">
              <el-select v-model="infoQuery.jobType" placeholder="全部" clearable style="width: 150px">
                <el-option v-for="(label, value) in jobTypeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="触发方式">
              <el-select v-model="infoQuery.triggerType" placeholder="全部" clearable style="width: 150px">
                <el-option v-for="(label, value) in triggerTypeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="infoQuery.status" placeholder="全部" clearable style="width: 110px">
                <el-option label="启用" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleInfoQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetInfoQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button v-hasPermi="['iot:job:info:add']" type="primary" :icon="Plus" @click="openInfoDialog()">
                新增任务
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadInfos" />
          </div>

          <el-table v-loading="infoLoading" :data="infoList" border>
            <el-table-column prop="jobName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="jobGroup" label="任务分组" width="110" />
            <el-table-column label="任务类型" width="130">
              <template #default="{ row }">
                <el-tag :type="row.jobType === 'LOCAL' ? 'primary' : 'warning'">
                  {{ jobTypeMap[row.jobType] || row.jobType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="触发方式" width="140">
              <template #default="{ row }">
                {{ triggerTypeMap[row.triggerType] || row.triggerType }}
              </template>
            </el-table-column>
            <el-table-column label="调度规则" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.triggerType === 'FIXED_RATE' ? `每 ${row.intervalSeconds} 秒` : row.cronExpr || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="handlerName" label="执行器" width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'">
                  {{ row.status === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastFireTime" label="最近触发" width="170" />
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-hasPermi="['iot:job:info:trigger']"
                  link
                  type="success"
                  @click="handleTrigger(row)"
                >
                  执行一次
                </el-button>
                <el-button
                  v-hasPermi="['iot:job:info:changeStatus']"
                  link
                  :type="row.status === '0' ? 'warning' : 'primary'"
                  @click="handleChangeStatus(row)"
                >
                  {{ row.status === '0' ? '停用' : '启用' }}
                </el-button>
                <el-button v-hasPermi="['iot:job:info:edit']" link type="primary" @click="openInfoDialog(row)">
                  编辑
                </el-button>
                <el-button v-hasPermi="['iot:job:info:remove']" link type="danger" @click="handleDeleteInfo(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="infoQuery.pageNum"
              v-model:page-size="infoQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="infoTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadInfos"
              @current-change="loadInfos"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ============================ 执行日志 ============================ -->
      <el-tab-pane label="执行日志" name="log" lazy>
        <div class="search-bar">
          <el-form :inline="true" :model="logQuery">
            <el-form-item label="任务名称">
              <el-input v-model="logQuery.jobName" placeholder="请输入任务名称" clearable />
            </el-form-item>
            <el-form-item label="触发方式">
              <el-select v-model="logQuery.triggerType" placeholder="全部" clearable style="width: 150px">
                <el-option v-for="(label, value) in allTriggerTypeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="执行状态">
              <el-select v-model="logQuery.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAIL" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleLogQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetLogQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button
                v-hasPermi="['iot:job:log:remove']"
                type="danger"
                :icon="Delete"
                :disabled="!selectedLogIds.length"
                @click="handleDeleteLogs"
              >
                删除日志
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadLogs" />
          </div>

          <el-table v-loading="logLoading" :data="logList" border @selection-change="handleLogSelection">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="jobName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="触发方式" width="140">
              <template #default="{ row }">
                {{ allTriggerTypeMap[row.triggerType] || row.triggerType }}
              </template>
            </el-table-column>
            <el-table-column prop="handlerName" label="执行器" width="150" show-overflow-tooltip />
            <el-table-column label="执行状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
                  {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="170" />
            <el-table-column label="耗时" width="110">
              <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
            </el-table-column>
            <el-table-column prop="result" label="执行结果" min-width="180" show-overflow-tooltip />
            <el-table-column prop="message" label="异常信息" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openLogDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="logQuery.pageNum"
              v-model:page-size="logQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="logTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadLogs"
              @current-change="loadLogs"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 任务表单 -->
    <el-dialog v-model="infoVisible" :title="infoForm.jobId ? '编辑任务' : '新增任务'" width="620px">
      <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="110px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="infoForm.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务分组" prop="jobGroup">
          <el-input v-model="infoForm.jobGroup" placeholder="默认 DEFAULT" />
        </el-form-item>
        <el-form-item label="任务类型" prop="jobType">
          <el-select v-model="infoForm.jobType" placeholder="请选择任务类型" style="width: 100%">
            <el-option v-for="(label, value) in jobTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行器" prop="handlerName">
          <el-select
            v-model="infoForm.handlerName"
            placeholder="请选择执行器"
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option v-for="(desc, name) in handlerMap" :key="name" :label="`${name}（${desc}）`" :value="name" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发方式" prop="triggerType">
          <el-select v-model="infoForm.triggerType" placeholder="请选择触发方式" style="width: 100%">
            <el-option v-for="(label, value) in triggerTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="infoForm.triggerType === 'CRON'" label="Cron 表达式" prop="cronExpr">
          <el-input v-model="infoForm.cronExpr" placeholder="如 0 0/5 * * * ?" />
        </el-form-item>
        <el-form-item v-if="infoForm.triggerType === 'FIXED_RATE'" label="间隔秒数" prop="intervalSeconds">
          <el-input-number v-model="infoForm.intervalSeconds" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="infoForm.jobType === 'SNAILJOB'" label="SnailJob 任务ID" prop="snailJobId">
          <el-input v-model="infoForm.snailJobId" placeholder="SnailJob 服务端任务ID" />
        </el-form-item>
        <el-form-item label="任务参数" prop="jobParams">
          <el-input v-model="infoForm.jobParams" type="textarea" :rows="3" placeholder='JSON 文本，如 {"days":7}' />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="infoForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="infoForm.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleInfoSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 日志详情 -->
    <el-dialog v-model="logDetailVisible" title="执行日志详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务名称">{{ logDetail.jobName }}</el-descriptions-item>
        <el-descriptions-item label="触发方式">
          {{ allTriggerTypeMap[logDetail.triggerType] || logDetail.triggerType }}
        </el-descriptions-item>
        <el-descriptions-item label="执行器">{{ logDetail.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="logDetail.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ logDetail.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ logDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ logDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ formatDuration(logDetail.duration) }}</el-descriptions-item>
        <el-descriptions-item label="执行结果">{{ logDetail.result || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常信息">{{ logDetail.message || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="logDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  addJobInfo,
  changeJobStatus,
  delJobInfo,
  delJobLog,
  getJobInfo,
  listJobHandlers,
  listJobInfos,
  listJobLogs,
  triggerJob,
  updateJobInfo
} from '@/api/job';

const activeTab = ref('info');

const jobTypeMap = { LOCAL: '本地调度', SNAILJOB: 'SnailJob 调度' };
const triggerTypeMap = { CRON: 'Cron 表达式', FIXED_RATE: '固定频率', MANUAL: '手动触发' };
const allTriggerTypeMap = { ...triggerTypeMap, SNAILJOB: 'SnailJob 触发' };

const infoLoading = ref(false);
const logLoading = ref(false);
const submitting = ref(false);

const infoList = ref([]);
const infoTotal = ref(0);
const logList = ref([]);
const logTotal = ref(0);
const selectedLogIds = ref([]);
const handlerMap = ref({});

const infoQuery = reactive({ jobName: '', jobType: '', triggerType: '', status: '', pageNum: 1, pageSize: 10 });
const logQuery = reactive({ jobName: '', triggerType: '', status: '', pageNum: 1, pageSize: 10 });

const infoVisible = ref(false);
const logDetailVisible = ref(false);
const infoFormRef = ref();

const infoForm = reactive({
  jobId: undefined,
  jobName: '',
  jobGroup: 'DEFAULT',
  jobType: 'LOCAL',
  handlerName: '',
  triggerType: 'CRON',
  cronExpr: '',
  intervalSeconds: 60,
  jobParams: '',
  snailJobId: undefined,
  status: '0',
  remark: ''
});
const logDetail = reactive({});

const infoRules = {
  jobName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
  jobType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  handlerName: [{ required: true, message: '请选择执行器', trigger: 'change' }],
  triggerType: [{ required: true, message: '请选择触发方式', trigger: 'change' }]
};

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

async function loadInfos() {
  infoLoading.value = true;
  try {
    const res = await listJobInfos(infoQuery);
    infoList.value = res.data.rows || [];
    infoTotal.value = res.data.total || 0;
  } finally {
    infoLoading.value = false;
  }
}

async function loadHandlers() {
  if (Object.keys(handlerMap.value).length) {
    return;
  }
  const res = await listJobHandlers();
  handlerMap.value = res.data || {};
}

async function loadLogs() {
  logLoading.value = true;
  try {
    const res = await listJobLogs(logQuery);
    logList.value = res.data.rows || [];
    logTotal.value = res.data.total || 0;
  } finally {
    logLoading.value = false;
  }
}

function handleTabChange(name) {
  if (name === 'log') {
    loadLogs();
  }
}

function handleInfoQuery() {
  infoQuery.pageNum = 1;
  loadInfos();
}

function resetInfoQuery() {
  Object.assign(infoQuery, { jobName: '', jobType: '', triggerType: '', status: '', pageNum: 1 });
  loadInfos();
}

function handleLogQuery() {
  logQuery.pageNum = 1;
  loadLogs();
}

function resetLogQuery() {
  Object.assign(logQuery, { jobName: '', triggerType: '', status: '', pageNum: 1 });
  loadLogs();
}

function resetInfoForm() {
  Object.assign(infoForm, {
    jobId: undefined,
    jobName: '',
    jobGroup: 'DEFAULT',
    jobType: 'LOCAL',
    handlerName: '',
    triggerType: 'CRON',
    cronExpr: '',
    intervalSeconds: 60,
    jobParams: '',
    snailJobId: undefined,
    status: '0',
    remark: ''
  });
}

async function openInfoDialog(row) {
  await loadHandlers();
  if (row) {
    const res = await getJobInfo(row.jobId);
    const detail = res.data || row;
    Object.assign(infoForm, {
      jobId: detail.jobId,
      jobName: detail.jobName,
      jobGroup: detail.jobGroup || 'DEFAULT',
      jobType: detail.jobType,
      handlerName: detail.handlerName,
      triggerType: detail.triggerType,
      cronExpr: detail.cronExpr || '',
      intervalSeconds: detail.intervalSeconds || 60,
      jobParams: detail.jobParams || '',
      snailJobId: detail.snailJobId,
      status: detail.status || '0',
      remark: detail.remark || ''
    });
  } else {
    resetInfoForm();
  }
  infoVisible.value = true;
}

async function handleInfoSubmit() {
  await infoFormRef.value.validate();
  submitting.value = true;
  try {
    if (infoForm.jobId) {
      await updateJobInfo(infoForm);
      ElMessage.success('修改成功');
    } else {
      await addJobInfo(infoForm);
      ElMessage.success('创建成功');
    }
    infoVisible.value = false;
    loadInfos();
  } finally {
    submitting.value = false;
  }
}

async function handleChangeStatus(row) {
  const target = row.status === '0' ? '1' : '0';
  await changeJobStatus(row.jobId, target);
  ElMessage.success(target === '0' ? '已启用' : '已停用');
  loadInfos();
}

async function handleTrigger(row) {
  const res = await triggerJob(row.jobId);
  const result = res.data || {};
  if (result.status === 'SUCCESS') {
    ElMessage.success(`执行成功：${result.result || '无输出'}`);
  } else {
    ElMessage.error(`执行失败：${result.message || '未知错误'}`);
  }
  loadInfos();
}

async function handleDeleteInfo(row) {
  await ElMessageBox.confirm(`确认删除任务「${row.jobName}」吗？其执行日志将一并移除。`, '提示', {
    type: 'warning'
  });
  await delJobInfo(row.jobId);
  ElMessage.success('删除成功');
  loadInfos();
}

function handleLogSelection(rows) {
  selectedLogIds.value = rows.map((item) => item.logId);
}

function openLogDetail(row) {
  Object.assign(logDetail, row);
  logDetailVisible.value = true;
}

async function handleDeleteLogs() {
  if (!selectedLogIds.value.length) {
    ElMessage.warning('请先选择要删除的日志');
    return;
  }
  await ElMessageBox.confirm(`确认删除选中的 ${selectedLogIds.value.length} 条日志吗？`, '提示', { type: 'warning' });
  await delJobLog(selectedLogIds.value.join(','));
  ElMessage.success('删除成功');
  loadLogs();
}

onMounted(() => {
  loadInfos();
});
</script>
