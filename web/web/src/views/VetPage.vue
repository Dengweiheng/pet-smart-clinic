<template>
  <div class="vet-portal">
    <el-row :gutter="24">
      <el-col :span="10" v-if="userState.role === 'USER' || userState.role === 'ADMIN'">
        <el-card class="consultation-creation">
          <template #header><div class="card-header"><el-icon color="#FF8A65"><Phone /></el-icon>发起在线咨询 (由执业药师接诊)</div></template>
          <el-form :model="consultationForm" label-position="top">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="用户 (Owner)">
                  <el-input :model-value="currentUsername" readonly disabled class="full-width" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="宠物 (Pet)">
                  <el-select v-model="consultationForm.petId" placeholder="选择宠物" class="full-width" @change="onPetSelect">
                    <el-option v-for="p in petOptions" :key="p.id" :label="p.name" :value="p.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="宠物名"><el-input v-model="consultationForm.petName" /></el-form-item>
            <el-form-item label="主诉症状">
              <el-input v-model="consultationForm.problemDescription" type="textarea" :rows="4" placeholder="请详细描述宠物的病情观察..." />
            </el-form-item>
            <el-button type="primary" size="large" @click="createConsultation" class="full-width" style="background: #FF8A65; border-color: #FF8A65;">
              <el-icon><Phone /></el-icon>&nbsp;建立问诊会话
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14" v-if="userState.role === 'USER' || userState.role === 'ADMIN'">
        <el-card class="consultation-history-card">
          <template #header><div class="card-header">问诊记录与诊断反馈</div></template>
          <el-table :data="historyList" border stripe height="460" class="history-table">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="petName" label="关联宠物" width="80" />
            <el-table-column prop="problemDescription" label="主诉症状" min-width="200" class-name="wrap-cell" />
            <el-table-column prop="diagnosis" label="诊断结果" min-width="200">
              <template #default="{ row }">
                <div v-if="row.diagnosis || row.prescriptionId" class="diagnosis-wrapper">
                  <el-tag type="success" effect="dark" class="diagnosis-tag" style="background: #FF8A65; border-color: #FF8A65;">{{ row.diagnosis || '诊断已完成' }}</el-tag>
                </div>
                <span v-else class="text-muted">等待药师开方...</span>
              </template>
            </el-table-column>
            <el-table-column prop="prescriptionId" label="电子处方" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.prescriptionId" type="danger" size="small" effect="dark" class="rx-id-tag clickable" @click="showPrescriptionDetails(row.prescriptionId)" style="background: #FF7043; border-color: #FF7043;">
                  电子处方
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" v-if="userState.role === 'PHARMACIST' || userState.role === 'VET' || userState.role === 'ADMIN'">
      <el-col :span="24" style="margin-top: 24px">
        <el-card class="prescription-card">
            <template #header>
              <div class="card-header">
                <el-icon color="#FF8A65"><DocumentChecked /></el-icon>
                <span>执业药师在线诊断与开方</span>
              </div>
            </template>
          <el-form :model="prescriptionForm" label-position="top">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="问诊 ID">
                  <el-select v-model="prescriptionForm.consultationId" placeholder="选择问诊记录" class="full-width" @change="onConsultationChange">
                    <el-option v-for="c in consultationOptions" :key="c.consultationId" :label="`编号: ${c.consultationId} (属于: ${c.petName})`" :value="c.consultationId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="执业药师 (Physician/Pharmacist)">
                  <el-input :model-value="currentUsername" readonly disabled class="full-width" />
                </el-form-item>
              </el-col>
            </el-row>

            <div v-if="selectedConsultation" class="symptom-preview-box">
              <div class="preview-label">对应主诉症状：</div>
              <div class="preview-content">{{ selectedConsultation.problemDescription }}</div>
            </div>
            <el-form-item label="临床诊断结论">
              <el-input v-model="prescriptionForm.diagnosis" placeholder="根据问诊情况输入的专业诊断结果" />
            </el-form-item>
            
            <div class="prescription-builder">
              <div class="builder-header">添加处方药物项</div>
              <el-row :gutter="8">
                <el-col :span="10">
                  <el-select v-model="selectedDrug" value-key="id" placeholder="搜索药品" filterable @change="onDrugSelect" class="full-width">
                    <el-option v-for="d in drugOptions" :key="d.id" :label="d.name" :value="d" />
                  </el-select>
                </el-col>
                <el-col :span="10"><el-input v-model="drugName" readonly placeholder="药品名称" /></el-col>
                <el-col :span="4"><el-input-number v-model="quantity" :min="1" class="full-width" /></el-col>
              </el-row>
              <el-row :gutter="8" style="margin-top: 8px">
                <el-col :span="20"><el-input v-model="dosage" placeholder="用法用量 (例: 每日2次，每次1粒, 饭后服)" /></el-col>
                <el-col :span="4"><el-button type="success" plain @click="appendDrug" class="full-width" style="color: #FF8A65; border-color: #FF8A65;">添加</el-button></el-col>
              </el-row>
            </div>

            <el-table :data="prescriptionForm.items" size="small" border style="margin: 20px 0">
              <el-table-column prop="drugName" label="药名" />
              <el-table-column prop="dosage" label="剂量" />
              <el-table-column prop="quantity" label="数量" width="70" />
            </el-table>

            <el-button type="danger" size="large" @click="createPrescription" class="full-width" :disabled="!prescriptionForm.items.length" style="background: #FF8A65; border-color: #FF8A65;">
              签署并下发电子处方
            </el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="prescriptionDetailVisible" title="电子处方详情" width="500px">
      <div v-if="currentPrescription" class="prescription-detail-content">
        <div class="detail-header">
          <div class="detail-row"><span>处方编号：</span><strong>{{ currentPrescription.prescriptionId }}</strong></div>
          <div class="detail-row"><span>开具时间：</span><strong>{{ new Date(currentPrescription.issuedAt).toLocaleString() }}</strong></div>
          <div class="detail-row"><span>临床诊断：</span><el-tag type="success" style="background: #FF8A65; border-color: #FF8A65;">{{ currentPrescription.diagnosis }}</el-tag></div>
        </div>
        
        <el-divider content-position="left">处方药品明细</el-divider>
        <el-table :data="currentPrescription.items" border stripe>
          <el-table-column prop="drugName" label="药品名称" />
          <el-table-column prop="dosage" label="剂量用法" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
        </el-table>

        <div class="detail-footer">
          <p class="disclaimer">提示：请凭此电子处方至商城购买，务必遵医嘱用药。</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { http } from '../api';
import { userState } from '../store';

const currentUsername = userState.username || '当前用户';
const currentUserId = userState.id; 
const consultationForm = ref({ userId: currentUserId, petId: '', petName: '', problemDescription: '' });
const prescriptionForm = ref({ consultationId: '', vetId: currentUserId, vetName: currentUsername, petId: '', diagnosis: '', validDays: 3, items: [] as any[] });
const validateForm = ref({ petId: '', prescriptionId: '', items: [] as any[] });

const consultationOptions = ref<any[]>([]);
const vetOptions = ref<any[]>([]);
const userOptions = ref<any[]>([]);
const petOptions = ref<any[]>([]);
const drugOptions = ref<any[]>([]);
const prescriptionList = ref<any[]>([]);

const historyList = computed(() => {
  return consultationOptions.value.map(c => {
    const pre = prescriptionList.value.find(p => p.consultationId === String(c.consultationId));
    return {
      ...c,
      diagnosis: pre?.diagnosis,
      prescriptionId: pre?.prescriptionId
    };
  });
});

const selectedDrug = ref<any>(null);
const drugCode = ref('');
const drugName = ref('');
const dosage = ref('每日2次，外用喷涂');
const quantity = ref(1);
const result = ref('');

const prescriptionDetailVisible = ref(false);
const currentPrescription = ref<any>(null);

const selectedConsultation = computed(() => {
  return consultationOptions.value.find(c => c.consultationId === prescriptionForm.value.consultationId);
});

async function loadOptions() {
  const isDoc = userState.role === 'VET' || userState.role === 'PHARMACIST';
  try {
    const [cRes, vRes, dRes, uRes, pRes, preRes] = await Promise.all([
      http.get('/api/vet/consultations', { params: isDoc ? {} : { userId: currentUserId } }),
      http.get('/api/vet/vets'),
      http.get('/api/mall/drugs', { params: { isRx: 1 } }),
      http.get('/api/vet/users'),
      http.get('/api/vet/pets', { params: { userId: currentUserId } }),
      http.get('/api/vet/prescriptions', { params: { userId: currentUserId } })
    ]);
    consultationOptions.value = cRes.data;
    vetOptions.value = vRes.data;
    drugOptions.value = dRes.data;
    userOptions.value = uRes.data;
    petOptions.value = pRes.data;
    prescriptionList.value = preRes.data;
  } catch(e) { ElMessage.error('加载基础数据失败'); }
}

function onPetSelect(val: string) {
  const p = petOptions.value.find(x => x.id === val);
  if (p) consultationForm.value.petName = p.name;
}

function onConsultationChange(val: string) {
  const c = consultationOptions.value.find(x => x.consultationId === val);
  if (c) {
    prescriptionForm.value.petId = c.petId;
    validateForm.value.petId = c.petId;
  }
}

function onDrugSelect(drug: any) {
  drugCode.value = drug.drugCode;
  drugName.value = drug.name;
  dosage.value = drug.dosageInstruction || '每日2次';
}

async function createConsultation() {
  try {
    const { data } = await http.post('/api/vet/consultations', consultationForm.value);
    loadOptions();
    prescriptionForm.value.consultationId = data.consultationId;
    prescriptionForm.value.petId = data.petId;
    validateForm.value.petId = data.petId;
    result.value = `[问诊成功建立]\n编号: ${data.consultationId}\n宠物: ${data.petName}`;
    ElMessage.success('问诊会话已建立');
  } catch(e) { ElMessage.error('无法建立会话'); }
}

function appendDrug() {
  if (!drugCode.value || !drugName.value) return;
  prescriptionForm.value.items.push({ drugCode: drugCode.value, drugName: drugName.value, dosage: dosage.value, quantity: quantity.value });
  ElMessage.success('已添加到待开药品列表');
  selectedDrug.value = null;
  drugCode.value = '';
  drugName.value = '';
}

async function createPrescription() {
  try {
    const { data } = await http.post('/api/vet/prescriptions', prescriptionForm.value);
    validateForm.value.prescriptionId = data.prescriptionId;
    result.value = `[处方开立成功]\n处方流水: ${data.prescriptionId}\n有效期: ${prescriptionForm.value.validDays}天\n处方已进入系统自动审校池`;
    ElMessage.success('电子处方已成功签署并生效');
  } catch(e) { ElMessage.error('开具失败'); }
}

async function validatePurchase() {
  try {
    const { data } = await http.post('/api/vet/prescriptions/validate-purchase', validateForm.value);
    result.value = `[后台校验实时反馈]\n结果: ${data.pass ? '通过 (PASS)' : '拒绝 (REJECT)'}\n详情: ${data.reason || '各项指标匹配'}`;
    if(data.pass) ElMessage.success('校验通过'); else ElMessage.error('校验失败');
  } catch(e) { ElMessage.error('请求校验失败'); }
}

async function showPrescriptionDetails(id: string) {
  try {
    const { data } = await http.get(`/api/vet/prescriptions/${id}`);
    currentPrescription.value = data;
    prescriptionDetailVisible.value = true;
  } catch (err) {
    ElMessage.error('无法获取处方详情');
  }
}

onMounted(loadOptions);
</script>

<style scoped>
.full-width { width: 100%; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 16px; color: #2C3E50; }
.prescription-builder { background: #FFF3E0; padding: 20px; border-radius: 8px; border: 1px dashed #FFB74D; }
.builder-header { font-weight: 600; margin-bottom: 12px; font-size: 14px; color: #5D4037; }
:deep(.wrap-cell) .cell { white-space: normal !important; line-height: 1.6 !important; padding: 8px 0 !important; }
.diagnosis-tag { white-space: normal !important; height: auto !important; padding: 6px 12px !important; line-height: 1.4 !important; }
.diagnosis-wrapper { padding: 4px 0; }
.symptom-preview-box { background: #FFF8F0; border-left: 4px solid #FF8A65; padding: 12px 16px; margin-bottom: 20px; border-radius: 4px; }
.preview-label { font-size: 13px; font-weight: 700; color: #5D4037; margin-bottom: 6px; }
.preview-content { font-size: 15px; line-height: 1.6; color: #2C3E50; }
.rx-id-tag.clickable { cursor: pointer; transition: all 0.3s; }
.rx-id-tag.clickable:hover { transform: scale(1.1); box-shadow: 0 2px 8px rgba(255, 138, 101, 0.4); }
.prescription-detail-content { padding: 10px; }
.detail-header { margin-bottom: 20px; }
.detail-row { margin-bottom: 12px; font-size: 15px; }
.detail-row span { color: #5D4037; width: 90px; display: inline-block; }
.detail-row strong { color: #2C3E50; }
.detail-footer { margin-top: 30px; text-align: center; }
.disclaimer { font-size: 13px; color: #FF8A65; margin-bottom: 20px; font-style: italic; }
</style>