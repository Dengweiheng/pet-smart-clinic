<template>
  <div class="pet-manager">
    <el-card class="main-card">
      <template #header>
        <div class="header-container">
          <div class="title-section">
            <el-icon><Postcard /></el-icon>
            <span>宠物综合档案中心</span>
          </div>
          <div class="action-section">
            <el-select v-model="petId" placeholder="选择已有宠物进行编辑" class="selector" @change="loadAll" clearable @clear="resetForm">
              <el-option v-for="p in petOptions" :key="p.id" :label="p.name" :value="String(p.id)" />
            </el-select>
            <el-button type="success" icon="Plus" @click="resetForm">登记新宠物</el-button>
          </div>
        </div>
      </template>

      <el-form :model="unifiedForm" label-position="top">
        <el-row :gutter="40">
          <!-- Left Column: Basic Info -->
          <el-col :span="10">
            <div class="form-section-title">
              <el-icon><User /></el-icon> 基础身份信息
            </div>
            <el-form-item label="所属用户 (Owner)">
              <el-input :model-value="currentUsername" readonly disabled class="full-width" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="宠物昵称"><el-input v-model="unifiedForm.name" placeholder="例如: 小白" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="物种"><el-input v-model="unifiedForm.species" placeholder="例如: 狗" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="品种"><el-input v-model="unifiedForm.breed" placeholder="例如: 柯基" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="体重 (kg)"><el-input-number v-model="unifiedForm.weightKg" :precision="2" :step="0.1" class="full-width" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="出生日期">
              <el-date-picker v-model="unifiedForm.birthDate" type="date" placeholder="选择日期" class="full-width" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>

          <!-- Divider -->
          <el-col :span="1" class="hidden-xs-only">
            <div class="vertical-divider"></div>
          </el-col>

          <!-- Right Column: Health Record -->
          <el-col :span="13">
            <div class="form-section-title">
              <el-icon><FirstAidKit /></el-icon> 电子健康记录
            </div>
            <el-form-item label="过敏信息 (Allergies)">
              <el-input v-model="unifiedForm.allergies" type="textarea" :rows="3" placeholder="食物过敏、药物过敏等..." />
            </el-form-item>
            <el-form-item label="慢性病史 (Chronic Diseases)">
              <el-input v-model="unifiedForm.chronicDiseases" type="textarea" :rows="3" placeholder="糖尿病、心脏病等..." />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="疫苗接种记录">
                  <el-input v-model="unifiedForm.vaccineNotes" type="textarea" :rows="4" placeholder="近三年接种记录..." />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用药/治疗史">
                  <el-input v-model="unifiedForm.medicationNotes" type="textarea" :rows="4" placeholder="重大手术或长期服药记录..." />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
        </el-row>

        <div class="form-footer">
          <el-button type="primary" size="large" @click="saveEverything" icon="Check">
            {{ petId ? '确认更新全量档案' : '完成登记并录入档案' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Postcard, User, FirstAidKit, Plus, Check } from '@element-plus/icons-vue';
import { userState } from '../store';
import { http } from '../api';

const userId = userState.id; 
const currentUsername = userState.username || '当前用户';
const petId = ref('');
const unifiedForm = ref({
  // Basic
  ownerUserId: userId,
  name: '',
  species: '',
  breed: '',
  gender: 'MALE',
  birthDate: '',
  weightKg: 0,
  // Health
  allergies: '',
  chronicDiseases: '',
  vaccineNotes: '',
  medicationNotes: ''
});

const userOptions = ref<any[]>([]);
const petOptions = ref<any[]>([]);

onMounted(async () => {
  fetchOptions();
});

async function fetchOptions() {
  try {
    const pRes = await http.get(`/api/vet/pets?userId=${userId}`);
    petOptions.value = pRes.data;
  } catch(e) { console.error('Load Error', e); }
}

function resetForm() {
  petId.value = '';
  unifiedForm.value = {
    ownerUserId: userId,
    name: '',
    species: '',
    breed: '',
    gender: 'MALE',
    birthDate: '',
    weightKg: 0,
    allergies: '',
    chronicDiseases: '',
    vaccineNotes: '',
    medicationNotes: ''
  };
}

async function loadAll() {
  if (!petId.value) {
    resetForm();
    return;
  }
  try {
    // 1. Fetch Basic Info
    const pRes = await http.get(`/api/pets/${petId.value}`);
    const basic = pRes.data;
    
    // 2. Fetch Health Info
    const hRes = await http.get(`/api/pets/${petId.value}/health-record`);
    const health = hRes.data || {};

    unifiedForm.value = {
      ...unifiedForm.value,
      ...basic,
      ...health
    };
    ElMessage.success('宠物全量档案已加载');
  } catch (err) {
    ElMessage.error('加载档案失败');
  }
}

async function saveEverything() {
  try {
    let currentPid = petId.value;
    
    // 1. Save Basic Info (Create or Update)
    if (currentPid) {
      await http.put(`/api/pets/${currentPid}`, unifiedForm.value);
    } else {
      const { data } = await http.post('/api/pets', unifiedForm.value);
      currentPid = String(data.petId || data.id);
      petId.value = currentPid;
    }

    // 2. Save Health Info
    await http.put(`/api/pets/${currentPid}/health-record`, unifiedForm.value);
    
    ElMessage.success('全量档案同步成功');
    fetchOptions(); // Refresh list
  } catch (err) {
    ElMessage.error('保存过程中出现错误');
  }
}
</script>

<style scoped>
.pet-manager {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px;
}
.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title-section {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
}
.action-section {
  display: flex;
  gap: 15px;
}
.selector {
  width: 250px;
}
.form-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #6366f1;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}
.vertical-divider {
  width: 1px;
  height: 100%;
  background-color: #f1f5f9;
  margin: 0 auto;
}
.full-width {
  width: 100%;
}
.form-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: center;
}
</style>
