<template>
  <div class="admin-dashboard">
    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="运营概览" name="overview">
        <el-row :gutter="24" class="stat-cards">
          <el-col :span="6" v-for="stat in dashboardStats" :key="stat.title">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <el-icon :size="40" :color="stat.color"><component :is="stat.icon" /></el-icon>
                <div class="stat-info">
                  <span class="stat-label">{{ stat.title }}</span>
                  <span class="stat-value">{{ stat.value }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-card class="chart-mock">
          <template #header><div class="card-header">近 7 日平台订单增长趋势 (真实业务数据)</div></template>
          <div ref="chartRef" style="height: 400px; width: 100%;"></div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="用户权限管理" name="users">
        <el-card>
          <template #header>
            <div class="table-header">
              <span>系统注册用户列表</span>
              <el-button type="primary" size="small" @click="fetchUsers">刷新数据</el-button>
            </div>
          </template>
          <el-table :data="users" v-loading="loading">
            <el-table-column prop="id" label="UID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="role" label="角色">
              <template #default="s">
                <el-tag :type="s.row.role === 'ADMIN' ? 'danger' : (s.row.role === 'PHARMACIST' || s.row.role === 'VET' ? 'warning' : 'info')">
                  {{ s.row.role === 'ADMIN' ? '系统管理员' : (s.row.role === 'PHARMACIST' || s.row.role === 'VET' ? '执业药师' : '普通用户') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="s">
                <el-switch v-model="s.row.status" active-value="ACTIVE" inactive-value="DISABLED" @change="toggleUserStatus(s.row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="s">
                <el-button size="small" v-if="s.row.role === 'PHARMACIST'" @click="pharmacistAudit(s.row)">资格审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="全量药品管理" name="drugs">
        <el-card>
          <template #header>
            <div class="table-header">
              <span>平台药品档案管理 (CRUD)</span>
              <el-button type="success" size="small" @click="openDrugDialog()">新增药品</el-button>
            </div>
          </template>
          <el-table :data="drugs" v-loading="loading">
            <el-table-column prop="drugCode" label="编码" width="140" />
            <el-table-column prop="name" label="品名" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="isRx" label="处方药" width="80">
              <template #default="s">
                <el-tag :type="s.row.isRx ? 'warning' : 'success'">{{ s.row.isRx ? 'RX' : 'OTC' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="120">
              <template #default="s">
                <span style="color: #f59e0b; font-weight: 700;">¥{{ s.row.price?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="s">
                <el-tag :type="s.row.status === 'ON_SALE' ? 'success' : 'info'">
                  {{ s.row.status === 'ON_SALE' ? '上架中' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="管理操作" width="120">
              <template #default="s">
                <el-button size="small" icon="Edit" circle @click="openDrugDialog(s.row)" />
                <el-button size="small" type="danger" icon="Delete" circle @click="handleDeleteDrug(s.row)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="处方合规监控" name="prescriptions">
        <el-table :data="prescriptions" v-loading="loading">
          <el-table-column prop="prescriptionId" label="处方ID" width="100" />
          <el-table-column prop="vetId" label="医生ID" width="100" />
          <el-table-column prop="diagnosis" label="临床结论" show-overflow-tooltip />
          <el-table-column prop="status" label="状态">
            <template #default="s">
              <el-tag :type="s.row.status === 'ISSUED' ? 'success' : 'warning'">
                {{ s.row.status === 'ISSUED' ? '已发' : '状态未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="issuedAt" label="签发日期" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="订单管理" name="orders_admin">
        <el-card>
          <template #header>
            <div class="table-header">
              <span>全量订单列表</span>
              <el-button type="primary" size="small" @click="fetchAdminOrders">刷新数据</el-button>
            </div>
          </template>
          <el-table :data="adminOrders" v-loading="loading">
            <el-table-column prop="orderNo" label="订单编号" width="200" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="totalAmount" label="总额" width="120">
              <template #default="{ row }">¥{{ row.totalAmount ? row.totalAmount.toFixed(2) : '0.00' }}</template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.orderStatus === 'COMPLETED' ? 'success' : (row.orderStatus === 'SHIPPED' ? 'primary' : (row.orderStatus === 'PAID' ? 'warning' : 'info'))">
                  {{ row.orderStatus === 'PAID' ? '待发货' : (row.orderStatus === 'SHIPPED' ? '已发货' : (row.orderStatus === 'COMPLETED' ? '已完成' : '待付款')) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180" />
            <el-table-column label="管理操作" width="120" align="center">
              <template #default="{ row }">
                <el-button v-if="row.orderStatus === 'PAID'" type="primary" size="small" @click="handleShip(row.orderNo)">
                  发货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 药品编辑/新增对话框 -->
    <el-dialog v-model="drugDialogVisible" :title="currentDrug.id ? '编辑药品资料' : '新增药品档案'" width="600px">
      <el-form :model="currentDrug" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="药品编码"><el-input v-model="currentDrug.drugCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="药品名称"><el-input v-model="currentDrug.name" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类"><el-input v-model="currentDrug.category" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价 (元)">
              <el-input-number v-model="currentDrug.price" :precision="2" :step="0.1" :min="0" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="处方性质">
              <el-radio-group v-model="currentDrug.isRx">
                <el-radio :label="0">OTC</el-radio>
                <el-radio :label="1">RX</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售状态">
              <el-radio-group v-model="currentDrug.status">
                <el-radio label="ON_SALE">上架</el-radio>
                <el-radio label="OFF_SHELF">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适应症"><el-input v-model="currentDrug.indication" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="用法用量"><el-input v-model="currentDrug.dosageInstruction" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="禁忌"><el-input v-model="currentDrug.contraindication" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drugDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDrug">保存档案</el-button>
      </template>
    </el-dialog>


    <!-- 药师审核对话框 -->
    <el-dialog v-model="auditVisible" title="执业药师资质审核" width="400px">
      <el-form label-width="120px">
        <el-form-item label="审核对象"><el-tag>{{ currentVet?.username }}</el-tag></el-form-item>
        <el-form-item label="审核结果">
          <el-select v-model="auditStatus">
            <el-option label="批准执业" value="APPROVED" />
            <el-option label="拒绝执业" value="REJECTED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAudit">提交决定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import { http } from '../api';

const activeTab = ref('overview');
const loading = ref(false);
const users = ref<any[]>([]);
const drugs = ref<any[]>([]);
const prescriptions = ref<any[]>([]);
const adminOrders = ref<any[]>([]);

const fetchAdminOrders = async () => {
  loading.value = true;
  try {
    const { data } = await http.get('/api/mall/orders/admin/all');
    adminOrders.value = data;
  } catch(e) { ElMessage.error('订单加载失败'); }
  loading.value = false;
};

const handleShip = async (orderNo: string) => {
  try {
    await http.put('/api/mall/orders/ship', { orderNo });
    ElMessage.success('已标记为发货');
    fetchAdminOrders();
  } catch(e) { ElMessage.error('发货操作失败'); }
};
const dashboardData = ref<any>({});

const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

const drugDialogVisible = ref(false);
const currentDrug = ref<any>({});
const inventoryVisible = ref(false);
const selectedDrugStock = ref<any>({});
const newStock = ref(0);

const auditVisible = ref(false);
const currentVet = ref<any>(null);
const auditStatus = ref('APPROVED');

const dashboardStats = computed(() => [
  { title: '全台在线', value: dashboardData.value.onlineUsers || 0, icon: 'User', color: '#6366f1' },
  { title: '累计处方额', value: dashboardData.value.prescriptionCount || 0, icon: 'DocumentChecked', color: '#10b981' },
  { title: '药品 SKU', value: drugs.value.length, icon: 'Goods', color: '#f59e0b' },
  { title: '累计订单', value: dashboardData.value.totalOrders || 0, icon: 'TrendCharts', color: '#ef4444' }
]);

function initChart() {
  if (!chartRef.value || !dashboardData.value.orderTrend) return;
  if (!chartInstance) chartInstance = echarts.init(chartRef.value);
  
  const dates = Object.keys(dashboardData.value.orderTrend);
  const counts = Object.values(dashboardData.value.orderTrend);

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { 
      type: 'category', 
      data: dates.map(d => d.substring(5)), // Show MM-DD
      axisLabel: { color: '#64748b' }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.2 },
      color: '#6366f1',
      lineStyle: { width: 3 }
    }]
  };
  chartInstance.setOption(option);
}

async function fetchDashboard() {
  const { data } = await http.get('/api/admin/dashboard');
  dashboardData.value = data;
  setTimeout(initChart, 50);
}

async function fetchUsers() {
  loading.value = true;
  const { data } = await http.get('/api/admin/users');
  users.value = data;
  loading.value = false;
}

async function fetchDrugs() {
  loading.value = true;
  const { data } = await http.get('/api/admin/drugs');
  drugs.value = data;
  loading.value = false;
}

async function fetchPrescriptions() {
  const { data } = await http.get('/api/admin/prescriptions/monitor');
  prescriptions.value = data;
}

function openDrugDialog(row?: any) {
  currentDrug.value = row ? { ...row } : { drugCode: '', name: '', category: '抗感染', isRx: 1, status: 'ON_SALE', price: 0.00 };
  drugDialogVisible.value = true;
}

async function saveDrug() {
  if (currentDrug.value.id) {
    await http.put(`/api/admin/drugs/${currentDrug.value.id}`, currentDrug.value);
  } else {
    await http.post('/api/admin/drugs', currentDrug.value);
  }
  ElMessage.success('档案已更新');
  drugDialogVisible.value = false;
  fetchDrugs();
}

async function handleDeleteDrug(row: any) {
  await ElMessageBox.confirm('确定要永久删除该药品档案吗？', '严正警告');
  await http.delete(`/api/admin/drugs/${row.id}`);
  ElMessage.warning('档案已彻底移除');
  fetchDrugs();
}


async function toggleUserStatus(user: any) {
  try {
    await http.put(`/api/admin/users/${user.id}/status`, { status: user.status });
    ElMessage.success(`用户 ${user.username} 状态已更新为: ${user.status === 'ACTIVE' ? '正常' : '停用'}`);
  } catch(e) { 
    ElMessage.error('状态更新失败');
    fetchUsers(); // Rollback UI if failed
  }
}

async function pharmacistAudit(user: any) {
  currentVet.value = user;
  auditVisible.value = true;
}

async function submitAudit() {
  await http.put('/api/admin/vets/qualification', { vetId: String(currentVet.value.id), status: auditStatus.value });
  ElMessage.success('资格审核结论已提交');
  auditVisible.value = false;
  fetchUsers();
}

onMounted(() => {
  fetchDashboard(); fetchUsers(); fetchDrugs(); fetchPrescriptions(); fetchAdminOrders();
  setTimeout(initChart, 200);
});

watch(activeTab, (val) => {
  if (val === 'overview') setTimeout(initChart, 50);
  if (val === 'users') fetchUsers();
  if (val === 'drugs') fetchDrugs();
  if (val === 'prescriptions') fetchPrescriptions();
  if (val === 'orders_admin') fetchAdminOrders();
});

onUnmounted(() => {
  if (chartInstance) chartInstance.dispose();
});
</script>

<style scoped>
.admin-tabs { background: #fff; padding: 32px; border-radius: 12px; box-shadow: var(--shadow-sm); min-height: 80vh; color: #000; }
.stat-cards { margin-bottom: 32px; }
.stat-card { border: 1.5px solid #cbd5e1 !important; }
.stat-content { display: flex; align-items: center; gap: 20px; }
.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 15px; color: #334155; font-weight: 700; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 800; color: #0f172a; }
.table-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #e2e8f0; margin-bottom: 16px; }
.table-header span { font-size: 18px; font-weight: 800; color: #000; }
.chart-mock { margin-top: 24px; border: 1px solid #cbd5e1 !important; }
.card-header { font-size: 18px; font-weight: 800; color: #000; }

:deep(.el-table) { font-size: 15px; color: #000 !important; }
:deep(.el-table th) { background-color: #f1f5f9 !important; color: #000 !important; font-weight: 900 !important; }
:deep(.el-table td) { padding: 16px 0 !important; color: #000 !important; font-weight: 600 !important; }
:deep(.el-tag) { font-size: 13px !important; font-weight: 800 !important; }

.admin-action-card { height: 100%; }
.action-footer { margin-top: 24px; text-align: right; }
.dashboard-buttons { display: flex; flex-wrap: wrap; gap: 12px; }
.log-card { margin-top: 32px; }
.console-box { background: #0f172a; color: #38bdf8; padding: 24px; border-radius: 12px; font-size: 14px; font-family: 'Fira Code', monospace; height: 320px; overflow-y: auto; white-space: pre-wrap; }
</style>
