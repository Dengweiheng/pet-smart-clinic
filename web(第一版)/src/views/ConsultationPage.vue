<template>
  <div class="consultation-wrapper">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="pet-info-card">
          <template #header>
            <div class="card-header">
              <span>宠物基本信息</span>
              <el-tag type="success" effect="dark">当前咨询</el-tag>
            </div>
          </template>
          <el-form :model="form" label-position="top">
            <el-form-item label="加载已有宠物">
              <el-select v-model="selectedPetId" placeholder="从档案中快速导入" class="full-width" @change="onPetSelect">
                <el-option v-for="p in petOptions" :key="p.id" :label="p.name" :value="String(p.id)" />
              </el-select>
            </el-form-item>
            <el-form-item label="宠物昵称"><el-input v-model="form.petName" /></el-form-item>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="物种"><el-input v-model="form.species" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="品种"><el-input v-model="form.breed" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="年龄"><el-input v-model="form.age" /></el-form-item>
            <el-form-item label="症状描述 (多轮对话基础)">
              <el-input v-model="form.symptoms" type="textarea" :rows="4" placeholder="请详细描述宠物的症状、持续时间及近况..." />
            </el-form-item>
            <el-button type="primary" size="large" @click="start" class="full-width" :loading="chatting">
              <el-icon><MagicStick /></el-icon>&nbsp; 开始 AI 智能分析
            </el-button>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="chat-card">
          <template #header>
            <div class="chat-header">
              <el-icon :size="20"><Message /></el-icon>
              <span>诊断建议生成中...</span>
            </div>
          </template>
          <div class="chat-content" ref="chatBox">
            <div v-if="!messages.length" class="chat-empty">
              <el-empty description="输入信息并开始咨询以获取 AI 建议" />
            </div>
            <div v-for="(msg, index) in messages" :key="index" :class="['chat-bubble', msg.role]">
              <div class="avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><Monitor /></el-icon>
              </div>
              <div class="message-content">
                <div class="role-label">{{ msg.role === 'user' ? '我的提问' : 'AI 专家建议' }}</div>
                <div class="text" v-html="formatResult(msg.content)"></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { streamConsultation, http } from '../api';
import { userState } from '../store';

const chatting = ref(false);
const chatBox = ref<any>(null);
const currentUsername = userState.username || '当前用户';
const currentUserId = userState.id;
const form = ref({ petName: '', species: '', breed: '', age: '', symptoms: '', history: [] as any[] });
const messages = ref<any[]>([]);
const petOptions = ref<any[]>([]);
const selectedPetId = ref('');

onMounted(async () => {
  try {
    const { data } = await http.get('/api/vet/pets', { params: { userId: currentUserId } });
    petOptions.value = data;
  } catch(e) { console.error('Load Pets Error', e); }
});

function onPetSelect(pid: string) {
  const p = petOptions.value.find(x => String(x.id) === pid);
  if (p) {
    form.value.petName = p.name;
    // 自动填充品种、物种等（如果档案中有）
    // 由于后端简版 pet 列表接口可能只返回 name，我们尝试动态加载详情
    loadPetDetail(pid);
  }
}

async function loadPetDetail(pid: string) {
  try {
    const { data } = await http.get(`/api/pets/${pid}`);
    form.value.petName = data.name;
    form.value.species = data.species;
    form.value.breed = data.breed;
    // 计算年龄逻辑（如果需要，暂填空由用户填）
  } catch(e) {}
}

function formatResult(text: string) {
  if (!text) return '';
  // 1. 处理换行
  let html = text.replace(/\n/g, '<br/>');
  // 2. 处理加粗（AI 常用 **加粗**）
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  // 3. 将 URL 转换为可点击链接
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  html = html.replace(urlRegex, (url) => {
    return `<a href="${url}" target="_blank" style="color: #409eff; text-decoration: underline;">查看药品详情</a>`;
  });
  return html;
}

async function start() {
  if (!form.value.symptoms) return;
  
  messages.value.push({ role: 'user', content: form.value.symptoms });
  const aiMsg = { role: 'assistant', content: '' };
  messages.value.push(aiMsg);
  
  chatting.value = true;
  try {
    await streamConsultation(form.value, (chunk) => {
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
.full-width { width: 100%; }
.chat-card {
  height: calc(100vh - 240px);
  display: flex;
  flex-direction: column;
}
.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
}
.chat-bubble {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.chat-bubble.user { flex-direction: row-reverse; }
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
}
.chat-bubble.assistant .avatar { background: var(--primary-color); color: #fff; }
.message-content {
  max-width: 80%;
}
.chat-bubble.user .message-content { text-align: right; }
.role-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.text {
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  line-height: 1.6;
}
.chat-bubble.user .text {
  background: var(--primary-color);
  color: #fff;
}
.chat-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
