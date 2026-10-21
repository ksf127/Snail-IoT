<template>
  <div class="product-manage">
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="商品名称">
          <el-input v-model="query.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="商品类型">
          <el-select v-model="query.productType" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" value="0" />
            <el-option label="下架" value="1" />
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
          <el-button v-hasPermi="['iot:pay:product']" type="primary" :icon="Plus" @click="openDialog()">
            新增商品
          </el-button>
        </div>
        <el-button :icon="Refresh" circle @click="loadProducts" />
      </div>

      <el-table v-loading="loading" :data="products" border>
        <el-table-column prop="productNo" label="商品编号" width="200" show-overflow-tooltip />
        <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="商品类型" width="120">
          <template #default="{ row }">{{ typeLabel(row.productType) }}</template>
        </el-table-column>
        <el-table-column prop="price" label="售价(元)" width="110" />
        <el-table-column prop="currency" label="币种" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="商品描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['iot:pay:product']" link type="primary" @click="openDialog(row)">修改</el-button>
            <el-button v-hasPermi="['iot:pay:product']" link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @current-change="loadProducts"
        />
      </div>

      <el-dialog v-model="visible" :title="form.productId ? '修改商品' : '新增商品'" width="560px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="商品编号" prop="productNo">
            <el-input v-model="form.productNo" placeholder="留空自动生成" />
          </el-form-item>
          <el-form-item label="商品名称" prop="productName">
            <el-input v-model="form.productName" placeholder="商品名称" />
          </el-form-item>
          <el-form-item label="商品类型" prop="productType">
            <el-select v-model="form.productType" placeholder="请选择商品类型" style="width: 100%">
              <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="售价(元)" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-input v-model="form.currency" placeholder="CNY" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio value="0">上架</el-radio>
              <el-radio value="1">下架</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="商品描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="商品描述" />
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
import { addPayProduct, delPayProduct, getPayProduct, listPayProductPage, updatePayProduct } from '@/api/pay';

const typeOptions = [
  { value: 'virtual_service', label: '虚拟服务' },
  { value: 'virtual_card', label: '虚拟卡券' },
  { value: 'membership', label: '会员权益' }
];

const loading = ref(false);
const submitting = ref(false);
const products = ref([]);
const total = ref(0);
const visible = ref(false);
const formRef = ref();

const query = reactive({
  productName: '',
  productType: '',
  status: '',
  pageNum: 1,
  pageSize: 10
});

const emptyForm = () => ({
  productId: undefined,
  productNo: '',
  productName: '',
  productType: 'virtual_service',
  price: 0.01,
  currency: 'CNY',
  status: '0',
  description: '',
  remark: ''
});
const form = reactive(emptyForm());

const rules = {
  productName: [{ required: true, message: '商品名称不能为空', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  price: [{ required: true, message: '售价不能为空', trigger: 'blur' }]
};

function typeLabel(code) {
  const item = typeOptions.find(t => t.value === code);
  return item ? item.label : code;
}

async function loadProducts() {
  loading.value = true;
  try {
    const res = await listPayProductPage(query);
    products.value = res.data.rows || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSizeChange() {
  query.pageNum = 1;
  loadProducts();
}

function handleQuery() {
  query.pageNum = 1;
  loadProducts();
}

function resetQuery() {
  Object.assign(query, { productName: '', productType: '', status: '', pageNum: 1 });
  loadProducts();
}

function openDialog(row) {
  Object.assign(form, emptyForm());
  if (row) {
    getPayProduct(row.productId).then(res => {
      Object.assign(form, res.data);
    });
  }
  visible.value = true;
}

async function handleSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    if (form.productId) {
      await updatePayProduct(form);
      ElMessage.success('修改成功');
    } else {
      await addPayProduct(form);
      ElMessage.success('新增成功');
    }
    visible.value = false;
    loadProducts();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除商品「${row.productName}」？`, '提示', { type: 'warning' });
  await delPayProduct(row.productId);
  ElMessage.success('删除成功');
  loadProducts();
}

onMounted(loadProducts);
</script>
