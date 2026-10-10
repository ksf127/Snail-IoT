<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="支付单" name="order">
        <div class="search-bar">
          <el-form :inline="true" :model="orderQuery">
            <el-form-item label="支付单号">
              <el-input v-model="orderQuery.orderNo" placeholder="请输入支付单号" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="orderQuery.status" placeholder="全部" clearable style="width: 140px">
                <el-option
                  v-for="(label, value) in orderStatusMap"
                  :key="value"
                  :label="label"
                  :value="value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleOrderQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetOrderQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div>
              <el-button v-hasPermi="['iot:pay:create']" type="primary" :icon="Plus" @click="openCreateDialog">
                下单
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="loadOrders" />
          </div>

          <el-table v-loading="orderLoading" :data="orderList" border>
            <el-table-column prop="orderNo" label="支付单号" width="200" show-overflow-tooltip />
            <el-table-column prop="bizOrderNo" label="业务单号" width="160" show-overflow-tooltip />
            <el-table-column prop="subject" label="商品标题" min-width="140" show-overflow-tooltip />
            <el-table-column prop="channel" label="渠道" width="110" />
            <el-table-column prop="amount" label="金额" width="110" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="orderStatusTag(row.status)">{{ orderStatusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="payTime" label="支付时间" width="180" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button v-hasPermi="['iot:pay:query']" link type="primary" @click="handleQueryOrder(row)">
                  查单
                </el-button>
                <el-button v-hasPermi="['iot:pay:refund']" link type="danger" @click="openRefundDialog(row)">
                  退款
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="orderQuery.pageNum"
              v-model:page-size="orderQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="orderTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadOrders"
              @current-change="loadOrders"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="退款单" name="refund">
        <div class="search-bar">
          <el-form :inline="true" :model="refundQuery">
            <el-form-item label="支付单号">
              <el-input v-model="refundQuery.orderNo" placeholder="请输入支付单号" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleRefundQuery">查询</el-button>
              <el-button :icon="Refresh" @click="resetRefundQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <div class="table-toolbar">
            <div />
            <el-button :icon="Refresh" circle @click="loadRefunds" />
          </div>

          <el-table v-loading="refundLoading" :data="refundList" border>
            <el-table-column prop="refundNo" label="退款单号" width="200" show-overflow-tooltip />
            <el-table-column prop="orderNo" label="支付单号" width="200" show-overflow-tooltip />
            <el-table-column prop="refundAmount" label="退款金额" width="120" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="refundStatusTag(row.status)">{{ refundStatusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="退款原因" min-width="140" show-overflow-tooltip />
            <el-table-column prop="refundTime" label="退款时间" width="180" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="refundQuery.pageNum"
              v-model:page-size="refundQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="refundTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadRefunds"
              @current-change="loadRefunds"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="createVisible" title="下单" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="业务单号" prop="bizOrderNo">
          <el-input v-model="createForm.bizOrderNo" placeholder="业务系统订单号" />
        </el-form-item>
        <el-form-item label="支付渠道" prop="channel">
          <el-select v-model="createForm.channel" placeholder="请选择支付渠道" style="width: 100%">
            <el-option
              v-for="c in channels"
              :key="c.configKey"
              :label="c.configName || c.channel"
              :value="c.channel"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品标题" prop="subject">
          <el-input v-model="createForm.subject" placeholder="订单标题" />
        </el-form-item>
        <el-form-item label="金额(元)" prop="amount">
          <el-input-number v-model="createForm.amount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="createForm.currency" placeholder="CNY" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="createForm.remark" type="textarea" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payResultVisible" title="支付信息" width="460px">
      <el-descriptions v-if="payResult" :column="1" border>
        <el-descriptions-item label="支付单号">{{ payResult.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="渠道">{{ payResult.channel }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ payResult.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ orderStatusMap[payResult.status] || payResult.status }}</el-descriptions-item>
        <el-descriptions-item label="二维码链接">
          <a :href="payResult.codeUrl" target="_blank">{{ payResult.codeUrl }}</a>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="payResultVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="退款" width="480px">
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="100px">
        <el-form-item label="支付单号">
          <el-input v-model="refundForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" placeholder="退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" :loading="refunding" @click="handleRefund">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createPayOrder,
  listPayChannels,
  listPayOrders,
  listPayRefunds,
  queryPayOrder,
  refundPayOrder
} from '@/api/pay'

const activeTab = ref('order')

const orderStatusMap = { 0: '待支付', 1: '已支付', 2: '已关闭', 3: '已退款' }
const refundStatusMap = { 0: '退款中', 1: '退款成功', 2: '退款失败' }

const orderLoading = ref(false)
const refundLoading = ref(false)
const creating = ref(false)
const refunding = ref(false)

const orderList = ref([])
const orderTotal = ref(0)
const refundList = ref([])
const refundTotal = ref(0)
const channels = ref([])

const orderQuery = reactive({ orderNo: '', status: '', pageNum: 1, pageSize: 10 })
const refundQuery = reactive({ orderNo: '', pageNum: 1, pageSize: 10 })

const createVisible = ref(false)
const payResultVisible = ref(false)
const refundVisible = ref(false)
const createFormRef = ref()
const refundFormRef = ref()
const payResult = ref(null)

const createForm = reactive({
  bizOrderNo: '',
  channel: '',
  subject: '',
  amount: 0.01,
  currency: 'CNY',
  remark: ''
})
const refundForm = reactive({ orderNo: '', refundAmount: 0.01, reason: '' })

const createRules = {
  bizOrderNo: [{ required: true, message: '业务单号不能为空', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }],
  amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }]
}
const refundRules = {
  refundAmount: [{ required: true, message: '退款金额不能为空', trigger: 'blur' }]
}

function orderStatusTag(status) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[status] || 'info'
}

function refundStatusTag(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info'
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const res = await listPayOrders(orderQuery)
    orderList.value = res.data.rows || []
    orderTotal.value = res.data.total || 0
  } finally {
    orderLoading.value = false
  }
}

async function loadRefunds() {
  refundLoading.value = true
  try {
    const res = await listPayRefunds(refundQuery)
    refundList.value = res.data.rows || []
    refundTotal.value = res.data.total || 0
  } finally {
    refundLoading.value = false
  }
}

async function loadChannels() {
  const res = await listPayChannels()
  channels.value = res.data || []
}

function handleOrderQuery() {
  orderQuery.pageNum = 1
  loadOrders()
}

function handleTabChange(name) {
  if (name === 'refund') {
    loadRefunds()
  }
}

function resetOrderQuery() {
  Object.assign(orderQuery, { orderNo: '', status: '', pageNum: 1 })
  loadOrders()
}

function handleRefundQuery() {
  refundQuery.pageNum = 1
  loadRefunds()
}

function resetRefundQuery() {
  Object.assign(refundQuery, { orderNo: '', pageNum: 1 })
  loadRefunds()
}

function openCreateDialog() {
  Object.assign(createForm, {
    bizOrderNo: '',
    channel: '',
    subject: '',
    amount: 0.01,
    currency: 'CNY',
    remark: ''
  })
  createVisible.value = true
}

async function handleCreate() {
  await createFormRef.value.validate()
  creating.value = true
  try {
    const res = await createPayOrder(createForm)
    payResult.value = res.data
    createVisible.value = false
    payResultVisible.value = true
    ElMessage.success('下单成功')
    loadOrders()
  } finally {
    creating.value = false
  }
}

async function handleQueryOrder(row) {
  const res = await queryPayOrder(row.orderNo)
  payResult.value = res.data
  payResultVisible.value = true
}

function openRefundDialog(row) {
  Object.assign(refundForm, { orderNo: row.orderNo, refundAmount: row.amount, reason: '' })
  refundVisible.value = true
}

async function handleRefund() {
  await refundFormRef.value.validate()
  refunding.value = true
  try {
    await refundPayOrder(refundForm)
    ElMessage.success('退款已提交')
    refundVisible.value = false
    loadOrders()
    loadRefunds()
  } finally {
    refunding.value = false
  }
}

onMounted(async () => {
  await loadChannels()
  loadOrders()
})
</script>
