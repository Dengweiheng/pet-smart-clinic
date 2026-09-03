<template>
  <div class="mall-container">
    <el-tabs v-model="activeTab" class="mall-tabs">
      <el-tab-pane label="药品选购" name="shop">
        <div class="shop-filter">
          <el-input v-model="keyword" placeholder="搜索药品名称..." class="search-input">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-radio-group v-model="isRx" size="large">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="0">OTC</el-radio-button>
            <el-radio-button :value="1">处方药</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="large" @click="loadDrugs" style="background: #FF8A65; border-color: #FF8A65;">查询</el-button>
          <el-button type="success" size="large" @click="activeTab = 'cart'" style="background: #FFB74D; border-color: #FFB74D;">我的购物车</el-button>
        </div>

        <el-row :gutter="20" class="drug-grid">
          <el-col v-for="drug in drugs" :key="drug.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="drug-card">
              <div class="drug-info">
                <div class="drug-header">
                  <h3 class="drug-name">{{ drug.name }}</h3>
                  <el-tag v-if="drug.isRx" size="small" type="danger">处方药</el-tag>
                </div>
                <p class="drug-code">编码: {{ drug.drugCode }}</p>
                <div class="drug-tags">
                  <el-tag size="small" :type="drug.isRx ? 'warning' : 'success'">
                    {{ drug.isRx ? '需处方' : 'OTC' }}
                  </el-tag>
                </div>
                <div class="drug-footer">
                  <span class="price">¥{{ drug.price?.toFixed(2) }}</span>
                  <div class="actions">
                    <el-button size="small" @click="showDetail(drug)">详情</el-button>
                    <el-button type="primary" circle @click="addCart(drug.id)" style="background: #FF8A65; border-color: #FF8A65;">
                      <el-icon><ShoppingCart /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="购物车" name="cart">
        <el-card class="cart-summary-card">
          <template #header>
            <div class="card-header">
              <span>您的购物清单</span>
              <el-button type="primary" @click="activeTab = 'checkout'" :disabled="cartItems.length === 0" style="background: #FF8A65; border-color: #FF8A65;">前往结算确认</el-button>
            </div>
          </template>
          <el-table :data="cartItems" border stripe>
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="120" align="center" />
            <el-table-column label="单价" width="100">
              <template #default="{ row }">¥{{ row.unitPrice ? row.unitPrice.toFixed(2) : '0.00' }}</template>
            </el-table-column>
            <el-table-column label="处方合规" width="100" align="center">
              <template #default="{ row }">
                <el-tooltip :content="row.isRx ? '该药品需凭处方购买' : '非处方药，可直接购买'" placement="top">
                  <el-tag v-if="row.isRx" :type="isInPrescription(row.drugCode) ? 'success' : 'danger'" size="small">
                    {{ isInPrescription(row.drugCode) ? '已关联' : '缺处方' }}
                  </el-tag>
                  <el-tag v-else type="info" size="small">OTC</el-tag>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">¥{{ (row.unitPrice * row.quantity).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="danger" size="small" circle @click="removeFromCart(row.drugId)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="cart-total" v-if="cartItems.length > 0">
            <span>合计金额：</span>
            <span class="total-price">¥{{ cartTotal.toFixed(2) }}</span>
          </div>
          <el-empty v-else description="购物车空空如也" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="结算中心" name="checkout">
        <el-card class="checkout-card">
          <template #header><div class="card-header">订单详情确认</div></template>
          <el-form label-position="top">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="收货用户">
                  <el-input :model-value="currentUsername" readonly disabled class="full-width" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="关联宠物">
                  <el-select v-model="petId" placeholder="请选择宠物" class="full-width">
                    <el-option v-for="p in pets" :key="p.id" :label="p.name" :value="Number(p.id)" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="电子处方 (处方药必填)">
                  <el-select v-model="prescriptionId" placeholder="选择有效处方" clearable class="full-width">
                    <el-option v-for="pre in prescriptions" :key="pre.prescriptionId" :label="'处方: ' + pre.diagnosis" :value="pre.prescriptionId" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <div class="risk-warning">
              <el-alert title="法律合规告知" type="warning" :closable="false" show-icon style="background: #FFF3E0; color: #E65100;">
                根据《兽药管理条例》，处方药必须凭执业兽医处方购买。请确认您已获得正规处方。
              </el-alert>
              <el-checkbox v-model="riskConfirmed" class="confirm-checkbox">
                已核对处方信息，确认遵医嘱用药
              </el-checkbox>
            </div>

            <div class="order-actions">
              <el-button type="warning" @click="validateBeforeOrder" size="large">处方合规校验</el-button>
              <el-button type="primary" size="large" @click="createOrder" style="background: #FF8A65; border-color: #FF8A65;">正式提交订单</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card v-if="orderNo" class="order-result-card">
          <template #header><div class="card-header">订单回执: {{ orderNo }}</div></template>
          <div class="status-steps">
            <el-button type="success" @click="payOrder" style="background: #FFB74D; border-color: #FFB74D;">确认完成支付</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的订单" name="orders">
        <el-card class="orders-card">
          <template #header><div class="card-header">历史订单列表</div></template>
          <el-table :data="ordersList" border stripe v-loading="loadingOrders">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="orderNo" label="订单编号" min-width="220" />
            <el-table-column prop="totalAmount" label="总额" width="120">
              <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.orderStatus === 'COMPLETED' ? 'success' : (row.orderStatus === 'SHIPPED' ? 'primary' : (row.orderStatus === 'PAID' ? 'warning' : 'info'))">
                  {{ row.orderStatus === 'PAID' ? '待发货' : (row.orderStatus === 'SHIPPED' ? '已发货' : (row.orderStatus === 'COMPLETED' ? '已完成' : '待付款')) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单日期" width="180" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button v-if="row.orderStatus === 'SHIPPED'" type="success" size="small" @click="confirmReceipt(row.orderNo)" style="background: #FFB74D; border-color: #FFB74D;">
                  确认收货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Medication Detail Dialog -->
    <el-dialog v-model="detailVisible" width="500px" title="药品说明详情" class="detail-dialog" destroy-on-close>
      <div v-if="selectedDrug" class="detail-body">
        <div class="detail-tag-header">
          <el-tag :type="selectedDrug.isRx ? 'danger' : 'success'">{{ selectedDrug.isRx ? '处方药 (Rx)' : '非处方药 (OTC)' }}</el-tag>
          <span class="code">编号: {{ selectedDrug.drugCode }}</span>
        </div>
        <h2 class="name">{{ selectedDrug.name }}</h2>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="适应症 (Indications)">
            {{ selectedDrug.indication || '暂无详细描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="用法用量 (Dosage)">
            {{ selectedDrug.dosageInstruction || '请遵医嘱使用' }}
          </el-descriptions-item>
          <el-descriptions-item label="禁忌症 (Contraindications)">
            <span class="text-danger">{{ selectedDrug.contraindication || '暂无明确禁忌' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="单价">
            <span class="price">¥{{ selectedDrug.price?.toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-footer">
          <el-button type="primary" class="full-width" @click="addCart(selectedDrug.id); detailVisible = false" style="background: #FF8A65; border-color: #FF8A65;">加入购物车</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { http } from '../api';
import { Search, ShoppingCart, Warning, Delete } from '@element-plus/icons-vue';
import { userState } from '../store';

const route = useRoute();
const activeTab = ref('shop');
const keyword = ref('');
const isRx = ref<number | undefined>(undefined);
const drugs = ref<any[]>([]);

const currentUsername = userState.username || '当前用户';
const currentUserId = userState.id;
const userId = ref<number | null>(Number(currentUserId));
const petId = ref<number | null>(null);
const prescriptionId = ref('');

const users = ref<any[]>([]);
const pets = ref<any[]>([]);
const prescriptions = ref<any[]>([]);
const currentPrescriptionItems = ref<any[]>([]);
const ordersList = ref<any[]>([]);
const loadingOrders = ref(false);

const detailVisible = ref(false);
const selectedDrug = ref<any>(null);

function showDetail(drug: any) {
  selectedDrug.value = drug;
  detailVisible.value = true;
}

onMounted(async () => {
  try {
    const [uRes, pRes, preRes] = await Promise.all([
      http.get('/api/vet/users'),
      http.get('/api/vet/pets', { params: { userId: currentUserId } }),
      http.get('/api/vet/prescriptions', { params: { userId: currentUserId } })
    ]);
    users.value = uRes.data;
    pets.value = pRes.data;
    prescriptions.value = preRes.data;
    
    if (userId.value === null && users.value.length > 0) userId.value = Number(users.value[0].id);
    if (pets.value.length > 0) petId.value = Number(pets.value[0].id);
    
    await loadDrugs();
  } catch (e) { console.error('Failed to load reference data', e); }
});

const riskConfirmed = ref(false);
const orderNo = ref('');
const result = ref('');

watch(() => route.query.keyword, (newVal) => {
  if (newVal !== undefined) {
    keyword.value = newVal as string;
    loadDrugs();
  }
}, { immediate: true });

watch(isRx, () => { loadDrugs(); });
watch([userId, petId], () => {
  if (userId.value && petId.value) loadCart();
});

watch(prescriptionId, async (newVal) => {
  if (newVal) {
    try {
      const { data } = await http.get(`/api/vet/prescriptions/${newVal}`);
      currentPrescriptionItems.value = data.items || [];
    } catch (e) { currentPrescriptionItems.value = []; }
  } else {
    currentPrescriptionItems.value = [];
  }
});

const isInPrescription = (drugCode: string) => {
  if (!prescriptionId.value) return false;
  return currentPrescriptionItems.value.some(item => item.drugCode === drugCode);
};

const cartItems = ref<any[]>([]);
const cartTotal = computed(() => {
  return cartItems.value.reduce((acc, cur) => acc + (cur.unitPrice * cur.quantity), 0);
});

async function loadDrugs() {
  const { data } = await http.get('/api/mall/drugs', { params: { keyword: keyword.value || undefined, isRx: isRx.value } });
  drugs.value = data;
}

async function addCart(drugId: number) {
  try {
    await http.post('/api/mall/cart/items', { userId: userId.value, petId: petId.value, drugId, quantity: 1 });
    ElMessage.success('已加入购物车');
    loadCart();
  } catch(e) { ElMessage.error('加入失败'); }
}

async function loadCart() {
  if (!userId.value || !petId.value) return;
  try {
    const { data } = await http.get('/api/mall/cart/items', { params: { userId: userId.value, petId: petId.value } });
    cartItems.value = data;
  } catch (e) {
    console.error('加载购物车失败', e);
  }
}

async function removeFromCart(drugId: number) {
  try {
    await http.delete('/api/mall/cart/items', { params: { userId: userId.value, petId: petId.value, drugId } });
    ElMessage.success('已从购物车移除');
    loadCart();
  } catch (e) {
    ElMessage.error('移除失败');
  }
}

async function createOrder() {
  if (!riskConfirmed.value) { ElMessage.warning('请先勾选确认用药风险提示'); return; }
  try {
    const { data } = await http.post('/api/mall/orders', { userId: userId.value, petId: petId.value, prescriptionId: prescriptionId.value });
    orderNo.value = data.orderNo;
    ElMessage.success('订单已创建');
  } catch(e) { ElMessage.error('订单创建失败'); }
}

async function validateBeforeOrder() {
  try {
    const { data: cartItemsData } = await http.get('/api/mall/cart/items', { params: { userId: userId.value, petId: petId.value } });
    const items = cartItemsData.map((x: any) => ({
      drugCode: x.drugCode,
      quantity: x.quantity 
    })).filter((x: any) => !!x.drugCode);

    const { data } = await http.post('/api/vet/prescriptions/validate-purchase', {
      petId: String(petId.value),
      prescriptionId: prescriptionId.value ? String(prescriptionId.value) : undefined,
      items,
    });
    result.value = `[审方结果]\n${JSON.stringify(data, null, 2)}`;
    if (data.pass) ElMessage.success('电子处方审核通过'); 
    else ElMessage.error('审核未通过：' + (data.reason || '处方不匹配'));
  } catch(e) { ElMessage.error('校验请求失败'); }
}

async function payOrder() {
  try {
    await http.post('/api/mall/orders/pay', { orderNo: orderNo.value });
    ElMessage.success('支付成功，等待商家发货');
    loadOrders();
  } catch(e) { ElMessage.error('支付失败'); }
}

async function confirmReceipt(no: string) {
  try {
    await http.post('/api/mall/orders/confirm-receipt', { orderNo: no });
    ElMessage.success('收货成功，订单已完成');
    loadOrders();
  } catch(e) { ElMessage.error('操作失败'); }
}

async function loadOrders() {
  if (!userId.value) return;
  loadingOrders.value = true;
  try {
    const { data } = await http.get('/api/mall/orders', { params: { userId: userId.value } });
    ordersList.value = data;
  } finally { loadingOrders.value = false; }
}

watch(activeTab, (val) => {
  if (val === 'orders') loadOrders();
});

watch(userId, () => {
  if (activeTab.value === 'orders') loadOrders();
});
</script>

<style scoped>
.mall-container { padding: 20px; }
.shop-filter { display: flex; gap: 16px; margin-bottom: 32px; align-items: center; background: #fff; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(255, 138, 101, 0.06); }
.search-input { width: 400px; }
.drug-card { transition: all 0.3s ease; height: 100%; border-radius: 12px; border: none; overflow: hidden; }
.drug-card:hover { transform: translateY(-5px); box-shadow: 0 12px 24px rgba(255, 138, 101, 0.15); }
:deep(.el-card__body) { padding: 15px !important; }
.drug-info { padding: 0; }
.drug-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; gap: 8px; min-height: 50px; }
.drug-name { font-size: 17px; color: #2C3E50; font-weight: 700; margin: 0; flex: 1; line-height: 1.3; }
.drug-code { font-size: 11px; color: #94a3b8; margin-bottom: 8px; }
.drug-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.price { font-size: 22px; font-weight: 700; color: #FF8A65; }
.actions { display: flex; align-items: center; gap: 8px; }

.detail-body { padding: 10px; }
.detail-tag-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.detail-tag-header .code { font-size: 13px; color: #64748b; }
.detail-body .name { margin: 0 0 20px 0; font-size: 24px; color: #2C3E50; border-bottom: 2px solid #FF8A65; padding-bottom: 8px; display: inline-block; }
.text-danger { color: #ef4444; }
.dialog-footer { margin-top: 24px; }
.checkout-card, .cart-summary-card, .order-result-card { border-radius: 12px; margin-bottom: 24px; }
.cart-total { margin-top: 20px; text-align: right; padding-top: 15px; border-top: 1px dashed #FFD6A5; }
.cart-total span { font-size: 16px; color: #5D4037; }
.total-price { color: #FF8A65; font-weight: 800; font-size: 20px; }
.risk-warning { margin: 24px 0; }
.confirm-checkbox { margin-top: 16px; font-weight: 600; color: #5D4037; }
.order-actions { display: flex; gap: 12px; margin-top: 32px; }
.status-steps { display: flex; gap: 12px; }
.full-width { width: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; color: #2C3E50; }
</style>