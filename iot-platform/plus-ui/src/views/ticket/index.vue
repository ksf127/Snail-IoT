<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="工单列表" name="ticket">
        <div class="search-bar">
          <el-form :inline="true" :model="query">
            <el-form-item label="工单编号">
              <el-input v-model="query.ticketNo" placeholder="请输入工单编号" clearable />
            </el-form-item>
            <el-form-item label="标题">
              <el-input v-model="query.title" placeholder="请输入标题" clearable />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="query.ticketType" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="(label, value) in typeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
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
              <el-button v-hasPermi="['iot:ticket:add']" type="primary" :icon="Plus" @click="openCreateDialog">
                新建工单
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadTickets" />
          </div>

          <el-table v-loading="loading" :data="ticketList" border>
            <el-table-column prop="ticketNo" label="工单编号" width="180" show-overflow-tooltip />
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                {{ typeMap[row.ticketType] || row.ticketType }}
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="优先级" width="90">
              <template #default="{ row }">
                <el-tag :type="priorityTag(row.priority)">{{ priorityMap[row.priority] || row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="110" />
            <el-table-column prop="handlerName" label="处理人" width="110" />
            <el-table-column label="规则处理" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.autoProcessed === '1'" type="success">已自动</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button v-hasPermi="['iot:ticket:query']" link type="primary" @click="openDetail(row)">
                  详情
                </el-button>
                <el-button
                  v-if="row.status === '0'"
                  v-hasPermi="['iot:ticket:handle']"
                  link
                  type="primary"
                  @click="handleSubmit(row)"
                >
                  提交
                </el-button>
                <el-button
                  v-if="row.status === '0' || row.status === '1'"
                  v-hasPermi="['iot:ticket:handle']"
                  link
                  type="primary"
                  @click="openHandleDialog(row)"
                >
                  办理
                </el-button>
                <el-button
                  v-if="row.ticketType === 'rule'"
                  v-hasPermi="['iot:ticket:handle']"
                  link
                  type="warning"
                  @click="handleRunRule(row)"
                >
                  跑规则
                </el-button>
                <el-button
                  v-if="row.ticketType === 'flow'"
                  v-hasPermi="['iot:ticket:handle']"
                  link
                  type="info"
                  @click="handleSyncFlow(row)"
                >
                  同步流程
                </el-button>
                <el-button
                  v-if="row.ticketType === 'flow' && row.status === '1'"
                  v-hasPermi="['iot:ticket:handle']"
                  link
                  type="info"
                  @click="openFlowDialog(row)"
                >
                  流程办理
                </el-button>
                <el-button v-hasPermi="['iot:ticket:edit']" link type="primary" @click="openEditDialog(row)">
                  编辑
                </el-button>
                <el-button v-hasPermi="['iot:ticket:remove']" link type="danger" @click="handleDelete(row)">
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
              @size-change="loadTickets"
              @current-change="loadTickets"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="规则配置" name="rule" lazy>
        <div class="search-bar">
          <el-form :inline="true" :model="ruleQuery">
            <el-form-item label="规则编码">
              <el-input v-model="ruleQuery.ruleCode" placeholder="请输入规则编码" clearable />
            </el-form-item>
            <el-form-item label="规则名称">
              <el-input v-model="ruleQuery.ruleName" placeholder="请输入规则名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleRuleQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetRuleQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button
                v-hasPermi="['iot:ticket:rule:add']"
                type="primary"
                :icon="Plus"
                @click="openRuleDialog()"
              >
                新增规则
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadRules" />
          </div>

          <el-table v-loading="ruleLoading" :data="ruleList" border>
            <el-table-column prop="ruleCode" label="规则编码" width="200" show-overflow-tooltip />
            <el-table-column prop="ruleName" label="规则名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="规则类型" width="120">
              <template #default="{ row }">
                {{ ruleTypeMap[row.ruleType] || row.ruleType }}
              </template>
            </el-table-column>
            <el-table-column label="匹配条件" min-width="200">
              <template #default="{ row }">
                {{ fieldMap[row.matchField] || row.matchField }}
                {{ operatorMap[row.matchOperator] || row.matchOperator }}
                {{ row.matchValue }}
              </template>
            </el-table-column>
            <el-table-column prop="targetValue" label="目标值" width="100" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'info'">
                  {{ row.status === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button v-hasPermi="['iot:ticket:rule:edit']" link type="primary" @click="openRuleDialog(row)">
                  修改
                </el-button>
                <el-button v-hasPermi="['iot:ticket:rule:remove']" link type="danger" @click="handleDeleteRule(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="ruleQuery.pageNum"
              v-model:page-size="ruleQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="ruleTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadRules"
              @current-change="loadRules"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="formVisible" :title="form.ticketId ? '编辑工单' : '新建工单'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="工单内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入工单内容" />
        </el-form-item>
        <el-form-item label="工单类型" prop="ticketType">
          <el-select v-model="form.ticketType" placeholder="请选择工单类型" style="width: 100%">
            <el-option v-for="(label, value) in typeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程编码" prop="flowCode">
          <el-input
            v-model="form.flowCode"
            :disabled="form.ticketType !== 'flow'"
            placeholder="流程工单必填，对应流程定义编码"
          />
        </el-form-item>
        <el-form-item label="工单分类" prop="category">
          <el-input v-model="form.category" placeholder="如：network / urgent" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option v-for="(label, value) in priorityMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望完成" prop="expectedTime">
          <el-date-picker
            v-model="form.expectedTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择期望完成时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handleVisible" title="工单办理" width="520px">
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="办理动作" prop="action">
          <el-select v-model="handleForm.action" placeholder="请选择办理动作" style="width: 100%">
            <el-option label="受理" value="accept" />
            <el-option label="处理中" value="handle" />
            <el-option label="完成" value="finish" />
            <el-option label="关闭" value="close" />
            <el-option label="驳回" value="reject" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整优先级" prop="priority">
          <el-select v-model="handleForm.priority" placeholder="可选：调整优先级" clearable style="width: 100%">
            <el-option v-for="(label, value) in priorityMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理意见" prop="message">
          <el-input v-model="handleForm.message" type="textarea" :rows="3" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleHandleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="flowVisible" title="流程任务办理" width="520px">
      <el-form ref="flowFormRef" :model="flowForm" :rules="flowRules" label-width="100px">
        <el-form-item label="流程任务ID" prop="processTaskId">
          <el-input-number v-model="flowForm.processTaskId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="办理意见" prop="message">
          <el-input v-model="flowForm.message" type="textarea" :rows="3" placeholder="请输入办理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="flowVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFlowSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="工单编号">{{ detail.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeMap[detail.ticketType] || detail.ticketType }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detail.category }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ priorityMap[detail.priority] || detail.priority }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[detail.status] || detail.status }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detail.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="命中规则">{{ detail.ruleCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程编码">{{ detail.flowCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程实例">{{ detail.processInstanceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程状态">{{ detail.businessStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ detail.content }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">流转记录</el-divider>
      <el-timeline v-loading="recordLoading">
        <el-timeline-item
          v-for="rec in records"
          :key="rec.recordId"
          :timestamp="rec.createTime"
          placement="top"
        >
          <div>
            <strong>{{ rec.nodeName || rec.action }}</strong>
            <span v-if="rec.operatorName"> · {{ rec.operatorName }}</span>
          </div>
          <div v-if="rec.message" class="record-message">{{ rec.message }}</div>
        </el-timeline-item>
        <el-empty v-if="!records.length" description="暂无流转记录" />
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="ruleVisible" :title="ruleForm.ruleId ? '编辑规则' : '新增规则'" width="560px">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleFormRules" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="ruleForm.ruleCode" placeholder="唯一编码，如 TICKET_RULE_XXX" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option v-for="(label, value) in ruleTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配字段" prop="matchField">
          <el-select v-model="ruleForm.matchField" placeholder="请选择匹配字段" style="width: 100%">
            <el-option v-for="(label, value) in fieldMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配操作" prop="matchOperator">
          <el-select v-model="ruleForm.matchOperator" placeholder="请选择匹配操作" style="width: 100%">
            <el-option v-for="(label, value) in operatorMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配值" prop="matchValue">
          <el-input v-model="ruleForm.matchValue" placeholder="in 操作以英文逗号分隔" />
        </el-form-item>
        <el-form-item label="目标值" prop="targetValue">
          <el-input v-model="ruleForm.targetValue" placeholder="assign 为处理人ID，priority 为优先级编码" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="ruleForm.sortNo" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="ruleForm.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRuleFormSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  addTicket,
  addTicketRule,
  completeFlowTask,
  delTicket,
  delTicketRule,
  getTicket,
  handleTicket,
  listTicketRecords,
  listTicketRules,
  listTickets,
  runTicketRule,
  submitTicket,
  syncTicketFlow,
  updateTicket,
  updateTicketRule
} from '@/api/ticket';

const activeTab = ref('ticket');

const typeMap = { normal: '普通工单', rule: '规则工单', flow: '流程工单' };
const statusMap = { 0: '待受理', 1: '处理中', 2: '已完成', 3: '已关闭', 4: '已驳回' };
const priorityMap = { 0: '低', 1: '中', 2: '高', 3: '紧急' };
const ruleTypeMap = { assign: '自动分配', priority: '调整优先级', close: '自动关闭' };
const fieldMap = { category: '分类', ticketType: '工单类型', priority: '优先级', source: '来源', title: '标题' };
const operatorMap = { eq: '等于', ne: '不等于', contains: '包含', in: '属于' };

const loading = ref(false);
const ruleLoading = ref(false);
const recordLoading = ref(false);
const submitting = ref(false);

const ticketList = ref([]);
const total = ref(0);
const ruleList = ref([]);
const ruleTotal = ref(0);
const records = ref([]);
const detail = ref(null);

const query = reactive({ ticketNo: '', title: '', ticketType: '', status: '', pageNum: 1, pageSize: 10 });
const ruleQuery = reactive({ ruleCode: '', ruleName: '', pageNum: 1, pageSize: 10 });

const formVisible = ref(false);
const handleVisible = ref(false);
const flowVisible = ref(false);
const detailVisible = ref(false);
const ruleVisible = ref(false);

const formRef = ref();
const handleFormRef = ref();
const flowFormRef = ref();
const ruleFormRef = ref();

const form = reactive({
  ticketId: undefined,
  ticketNo: '',
  title: '',
  content: '',
  ticketType: 'normal',
  flowCode: '',
  category: '',
  priority: '1',
  expectedTime: '',
  remark: ''
});
const handleForm = reactive({ ticketId: undefined, action: 'accept', priority: '', message: '' });
const flowForm = reactive({ ticketId: undefined, action: 'complete', processTaskId: undefined, message: '' });
const ruleForm = reactive({
  ruleId: undefined,
  ruleCode: '',
  ruleName: '',
  ruleType: 'assign',
  matchField: 'category',
  matchOperator: 'eq',
  matchValue: '',
  targetValue: '',
  sortNo: 0,
  status: '0',
  remark: ''
});

const rules = {
  title: [{ required: true, message: '工单标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '工单内容不能为空', trigger: 'blur' }],
  ticketType: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  flowCode: [
    {
      validator: (rule, value, callback) => {
        if (form.ticketType === 'flow' && !value) {
          callback(new Error('流程工单必须填写流程编码'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};
const handleRules = { action: [{ required: true, message: '请选择办理动作', trigger: 'change' }] };
const flowRules = { processTaskId: [{ required: true, message: '流程任务ID不能为空', trigger: 'blur' }] };
const ruleFormRules = {
  ruleCode: [{ required: true, message: '规则编码不能为空', trigger: 'blur' }],
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  matchField: [{ required: true, message: '请选择匹配字段', trigger: 'change' }],
  matchOperator: [{ required: true, message: '请选择匹配操作', trigger: 'change' }]
};

function statusTag(status) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }[status] || 'info';
}

function priorityTag(priority) {
  return { 0: 'info', 1: 'primary', 2: 'warning', 3: 'danger' }[priority] || 'info';
}

async function loadTickets() {
  loading.value = true;
  try {
    const res = await listTickets(query);
    ticketList.value = res.data.rows || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
}

async function loadRules() {
  ruleLoading.value = true;
  try {
    const res = await listTicketRules(ruleQuery);
    ruleList.value = res.data.rows || [];
    ruleTotal.value = res.data.total || 0;
  } finally {
    ruleLoading.value = false;
  }
}

function handleQuery() {
  query.pageNum = 1;
  loadTickets();
}

function resetQuery() {
  Object.assign(query, { ticketNo: '', title: '', ticketType: '', status: '', pageNum: 1 });
  loadTickets();
}

function handleRuleQuery() {
  ruleQuery.pageNum = 1;
  loadRules();
}

function resetRuleQuery() {
  Object.assign(ruleQuery, { ruleCode: '', ruleName: '', pageNum: 1 });
  loadRules();
}

function handleTabChange(name) {
  if (name === 'rule') {
    loadRules();
  }
}

function resetForm() {
  Object.assign(form, {
    ticketId: undefined,
    ticketNo: '',
    title: '',
    content: '',
    ticketType: 'normal',
    flowCode: '',
    category: '',
    priority: '1',
    expectedTime: '',
    remark: ''
  });
}

function openCreateDialog() {
  resetForm();
  formVisible.value = true;
}

async function openEditDialog(row) {
  const res = await getTicket(row.ticketId);
  const data = res.data || {};
  Object.assign(form, {
    ticketId: data.ticketId,
    ticketNo: data.ticketNo,
    title: data.title,
    content: data.content,
    ticketType: data.ticketType,
    flowCode: data.flowCode || '',
    category: data.category || '',
    priority: data.priority || '1',
    expectedTime: data.expectedTime || '',
    remark: data.remark || ''
  });
  formVisible.value = true;
}

async function handleFormSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    if (form.ticketId) {
      await updateTicket(form);
      ElMessage.success('修改成功');
    } else {
      await addTicket(form);
      ElMessage.success('创建成功');
    }
    formVisible.value = false;
    loadTickets();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除工单「${row.title}」吗？`, '提示', { type: 'warning' });
  await delTicket(row.ticketId);
  ElMessage.success('删除成功');
  loadTickets();
}

async function handleSubmit(row) {
  await ElMessageBox.confirm(`确认提交工单「${row.title}」吗？`, '提示', { type: 'warning' });
  await submitTicket(row.ticketId, { flowCode: row.flowCode, handler: row.handlerId });
  ElMessage.success('提交成功');
  loadTickets();
}

async function handleRunRule(row) {
  await runTicketRule(row.ticketId);
  ElMessage.success('规则引擎已执行');
  loadTickets();
}

async function handleSyncFlow(row) {
  await syncTicketFlow(row.ticketId);
  ElMessage.success('流程状态已同步');
  loadTickets();
}

function openHandleDialog(row) {
  Object.assign(handleForm, { ticketId: row.ticketId, action: 'accept', priority: '', message: '' });
  handleVisible.value = true;
}

async function handleHandleSubmit() {
  await handleFormRef.value.validate();
  submitting.value = true;
  try {
    await handleTicket(handleForm);
    ElMessage.success('办理成功');
    handleVisible.value = false;
    loadTickets();
  } finally {
    submitting.value = false;
  }
}

function openFlowDialog(row) {
  Object.assign(flowForm, {
    ticketId: row.ticketId,
    action: 'complete',
    processTaskId: undefined,
    message: ''
  });
  flowVisible.value = true;
}

async function handleFlowSubmit() {
  await flowFormRef.value.validate();
  submitting.value = true;
  try {
    await completeFlowTask(flowForm);
    ElMessage.success('流程办理成功');
    flowVisible.value = false;
    loadTickets();
  } finally {
    submitting.value = false;
  }
}

async function openDetail(row) {
  const res = await getTicket(row.ticketId);
  detail.value = res.data;
  detailVisible.value = true;
  recordLoading.value = true;
  try {
    const recRes = await listTicketRecords({ ticketId: row.ticketId });
    records.value = recRes.data || [];
  } finally {
    recordLoading.value = false;
  }
}

function resetRuleForm() {
  Object.assign(ruleForm, {
    ruleId: undefined,
    ruleCode: '',
    ruleName: '',
    ruleType: 'assign',
    matchField: 'category',
    matchOperator: 'eq',
    matchValue: '',
    targetValue: '',
    sortNo: 0,
    status: '0',
    remark: ''
  });
}

function openRuleDialog(row) {
  if (row) {
    Object.assign(ruleForm, {
      ruleId: row.ruleId,
      ruleCode: row.ruleCode,
      ruleName: row.ruleName,
      ruleType: row.ruleType,
      matchField: row.matchField,
      matchOperator: row.matchOperator,
      matchValue: row.matchValue || '',
      targetValue: row.targetValue || '',
      sortNo: row.sortNo || 0,
      status: row.status || '0',
      remark: row.remark || ''
    });
  } else {
    resetRuleForm();
  }
  ruleVisible.value = true;
}

async function handleRuleFormSubmit() {
  await ruleFormRef.value.validate();
  submitting.value = true;
  try {
    if (ruleForm.ruleId) {
      await updateTicketRule(ruleForm);
      ElMessage.success('修改成功');
    } else {
      await addTicketRule(ruleForm);
      ElMessage.success('创建成功');
    }
    ruleVisible.value = false;
    loadRules();
  } finally {
    submitting.value = false;
  }
}

async function handleDeleteRule(row) {
  await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」吗？`, '提示', { type: 'warning' });
  await delTicketRule(row.ruleId);
  ElMessage.success('删除成功');
  loadRules();
}

onMounted(() => {
  loadTickets();
});
</script>

<style scoped>
.record-message {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-top: 4px;
}
</style>
