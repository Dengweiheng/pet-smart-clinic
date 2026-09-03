<template>
  <div class="consultation-wrapper">
    <!-- 标题区域 -->
    <div class="consultation-header">
      <div class="header-content">
        <h1 class="page-title">您的宠物AI医生</h1>
        <h2 class="page-subtitle">为您的宠物提供专业私人的健康建议</h2>
      </div>
    </div>

    <!-- 左右两栏内容区 -->
    <div class="consultation-main">
      <!-- 左侧：宠物信息表单卡片 -->
      <el-card class="pet-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3 class="header-title">🐾 宠物基本信息</h3>
            <el-tag type="warning" effect="light" round size="large">当前咨询</el-tag>
          </div>
        </template>
        <el-form :model="form" label-position="top" class="pet-form">
          <el-form-item label="加载已有宠物">
            <el-select
              v-model="selectedPetId"
              placeholder="从档案中快速导入"
              class="full-width"
              @change="onPetSelect"
              size="large"
            >
              <el-option
                v-for="p in petOptions"
                :key="p.id"
                :label="p.name"
                :value="String(p.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="宠物昵称">
            <el-input v-model="form.petName" placeholder="例如：布丁" size="large" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="物种">
                <el-input v-model="form.species" placeholder="猫/狗" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品种">
                <el-input v-model="form.breed" placeholder="英短/柯基" size="large" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="年龄">
            <el-input v-model="form.age" placeholder="例如：2岁" size="large" />
          </el-form-item>
          <el-form-item label="症状描述（多轮对话基础）">
            <el-input
              v-model="form.symptoms"
              type="textarea"
              :rows="5"
              placeholder="请详细描述宠物的症状、持续时间及近况..."
              class="symptom-textarea"
            />
          </el-form-item>
          <div class="form-actions">
            <el-button
              type="primary"
              size="large"
              @click="start"
              class="analyze-btn"
              :loading="chatting"
            >
              <el-icon :size="20"><MagicStick /></el-icon>&nbsp; 开始 AI 智能分析
            </el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 右侧：AI 对话区 -->
      <el-card class="chat-card" shadow="hover">
        <template #header>
          <div class="chat-header">
            <div class="chat-header-left">
              <el-icon :size="20" class="pulse-icon"><Message /></el-icon>
              <span>AI 诊断建议</span>
            </div>
            <el-tag v-if="chatting" type="warning" effect="plain" size="small" round>
              思考中
            </el-tag>
          </div>
        </template>
        <div class="chat-content" ref="chatBox">
          <div v-if="!hasChatStarted && !messages.length" class="chat-placeholder">
            <el-empty description="请在左侧填写宠物信息并开始分析" />
          </div>
          <div v-else>
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['chat-bubble', msg.role]"
            >
              <div class="avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><Monitor /></el-icon>
              </div>
              <div class="message-content">
                <div class="role-label">
                  {{ msg.role === 'user' ? '我的提问' : 'AI 专家建议' }}
                </div>
                <div class="text" v-html="formatResult(msg.content)"></div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { streamConsultation, http } from '../api';
import { userState } from '../store';
import { MagicStick, Message, User, Monitor } from '@element-plus/icons-vue';

const chatting = ref(false);
const chatBox = ref<HTMLElement | null>(null);
const currentUserId = userState.id;
const form = ref({
  petName: '',
  species: '',
  breed: '',
  age: '',
  symptoms: '',
  history: [] as any[]
});
const messages = ref<Array<{ role: string; content: string }>>([]);
const petOptions = ref<any[]>([]);
const selectedPetId = ref('');

const hasChatStarted = ref(false);

onMounted(async () => {
  try {
    const { data } = await http.get('/api/vet/pets', { params: { userId: currentUserId } });
    petOptions.value = data;
  } catch (e) {
    console.error('Load Pets Error', e);
  }
});

function onPetSelect(pid: string) {
  const p = petOptions.value.find(x => String(x.id) === pid);
  if (p) {
    form.value.petName = p.name;
    loadPetDetail(pid);
  }
}

async function loadPetDetail(pid: string) {
  try {
    const { data } = await http.get(`/api/pets/${pid}`);
    form.value.petName = data.name;
    form.value.species = data.species;
    form.value.breed = data.breed;
  } catch (e) {}
}

function formatResult(text: string) {
  if (!text) return '';
  let html = text.replace(/\n/g, '<br/>');
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  html = html.replace(urlRegex, (url) => {
    return `<a href="${url}" target="_blank" style="color: #FF8A65; text-decoration: underline;">查看药品详情</a>`;
  });
  return html;
}

async function start() {
  if (!form.value.symptoms) return;

  hasChatStarted.value = true;

  messages.value.push({ role: 'user', content: form.value.symptoms });
  const aiMsg = { role: 'assistant', content: '' };
  messages.value.push(aiMsg);

  chatting.value = true;
  try {
    await streamConsultation(form.value, (chunk: string) => {
      aiMsg.content += chunk;
      scrollToBottom();
    });
  } finally {
    chatting.value = false;
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBox.value) {
      chatBox.value.scrollTop = chatBox.value.scrollHeight;
    }
  });
}
</script>

<style scoped>
.consultation-wrapper {
  min-height: calc(100vh - 74px);
  display: flex;
  flex-direction: column;
  padding: 0;
  margin: 0;
  background: #FFF8F0;
  overflow-x: hidden;
}

.consultation-header {
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background: #FFFFFF;
  border-radius: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  border-bottom: 1px solid rgba(255, 138, 101, 0.2);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 28px 32px;
  text-align: left;
  box-sizing: border-box;
}

.page-title {
  font-size: 30px;
  font-weight: 550;
  color: #2C3E50;
  margin: 0 0 8px 0;
  letter-spacing: -0.01em;
  background: linear-gradient(135deg, #FF8A65 0%, #FFA07A 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.page-subtitle {
  font-size: 20px;
  color: #5D4037;
  margin: 0;
  font-weight: 400;
}

.consultation-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 32px;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.pet-info-card {
  width: 400px;
  flex-shrink: 0;
  transition: all 300ms cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 24px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 20px rgba(255, 138, 101, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  height: fit-content;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-weight: 700;
  font-size: 26px;
  color: #2C3E50;
  margin: 0;
  letter-spacing: -0.01em;
}

.pet-form :deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 16px;
  color: #2C3E50;
  padding-bottom: 6px;
}

.pet-form :deep(.el-input__wrapper),
.pet-form :deep(.el-textarea__inner) {
  border-radius: 18px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 138, 101, 0.2) !important;
  box-shadow: none !important;
  transition: border-color 150ms, box-shadow 150ms;
  font-size: 16px;
  padding: 10px 16px;
}

.pet-form :deep(.el-input__inner),
.pet-form :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 1.5;
}

.pet-form :deep(.el-textarea__inner) {
  min-height: 120px !important;
}

.pet-form :deep(.el-select) {
  width: 100%;
}

.pet-form :deep(.el-input__wrapper:hover),
.pet-form :deep(.el-textarea__inner:hover) {
  border-color: #FFAB91 !important;
  background: rgba(255, 255, 255, 0.85) !important;
}

.pet-form :deep(.el-input__wrapper.is-focus),
.pet-form :deep(.el-textarea__inner:focus) {
  border-color: #FF8A65 !important;
  box-shadow: 0 0 0 3px rgba(255, 138, 101, 0.2) !important;
  background: rgba(255, 255, 255, 0.9) !important;
}

.full-width { width: 100%; }

.form-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 8px;
}

.analyze-btn {
  border-radius: 999px !important;
  background: linear-gradient(135deg, #FF8A65 0%, #FFA07A 100%) !important;
  border: none !important;
  font-weight: 700 !important;
  font-size: 18px !important;
  padding: 16px 24px !important;
  box-shadow: 0 4px 12px rgba(255, 138, 101, 0.3) !important;
  transition: all 150ms;
}

.analyze-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(255, 138, 101, 0.4) !important;
}

.chat-card {
  flex: 1;
  min-width: 500px;
  border-radius: 24px !important;
  background: rgba(255, 255, 255, 0.75) !important;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 20px rgba(255, 138, 101, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 240px);
  min-height: 600px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 18px;
  color: #2C3E50;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: transparent;
  border-radius: 0 0 24px 24px;
}

.chat-placeholder {
  height: 100%;
  font-size: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-bubble {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}

.chat-bubble.user { flex-direction: row-reverse; }

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 241, 235, 0.9);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF8A65;
  flex-shrink: 0;
  font-size: 20px;
}

.chat-bubble.user .avatar {
  background: #FF8A65;
  color: white;
}

.message-content { max-width: 75%; }

.role-label {
  font-size: 14px;
  font-weight: 600;
  color: #7F8C8D;
  margin-bottom: 6px;
}

.text {
  padding: 16px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  line-height: 1.6;
  font-size: 16px;
  color: #2C3E50;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.chat-bubble.user .text {
  background: linear-gradient(135deg, #FF8A65 0%, #FFA07A 100%);
  color: white;
  border: none;
}

.chat-bubble.user .text a {
  color: #FFF5E0 !important;
}

@media (max-width: 1024px) {
  .consultation-main { padding: 20px 16px; }
  .pet-info-card { width: 100%; }
  .chat-card { min-width: auto; height: auto; min-height: 500px; }
}

@media (max-width: 768px) {
  .header-content { padding: 20px 16px; }
  .page-title { font-size: 20px; }
  .page-subtitle { font-size: 16px; }
  .consultation-main { padding: 16px; gap: 16px; }
}
</style>