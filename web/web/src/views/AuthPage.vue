<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <div v-if="userState.token" class="auth-logged-in">
        <div class="auth-header">
          <el-avatar :size="64" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <h2 class="auth-title">尊贵的 {{ userState.username }}</h2>
          <el-tag size="large" type="warning" effect="dark" class="role-tag">{{ userState.role }}</el-tag>
          <p class="auth-subtitle" style="margin-top: 20px;">您已在线，可以开始您的业务处理</p>
        </div>
        <div class="auth-actions">
          <el-button type="danger" size="large" plain @click="handleLogout" class="full-width">
            安全登出系统
          </el-button>
        </div>
      </div>

      <div v-else>
        <div class="auth-header">
          <h1 class="clinic-title">宠物药品智能咨询与销售系统</h1>
          <p class="clinic-subtitle">{{ isRegister ? '创建您的云端档案' : '欢迎回归您的宠物健康家园' }}</p>
        </div>

        <el-tabs v-model="mode" stretch class="auth-tabs">
          <el-tab-pane label="会员登录" name="login" />
          <el-tab-pane label="新户注册" name="register" />
        </el-tabs>

        <el-form :model="form" class="auth-form" @submit.prevent="handleSubmit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入您的账号名称" prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="请输入您的安全密码" prefix-icon="Lock" show-password />
          </el-form-item>
          
          <template v-if="isRegister">
            <el-form-item>
              <el-input v-model="form.phone" placeholder="请输入您的联络电话" prefix-icon="Iphone" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="form.role" placeholder="请选择您的注册身份" style="width: 100%">
                <el-option label="宠物主 (User)" value="USER" />
                <el-option label="执业兽医 (Vet)" value="VET" />
              </el-select>
            </el-form-item>
          </template>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            {{ isRegister ? '立即开启在线诊疗' : '安全进入系统' }}
          </el-button>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userState } from '../store';
import { http } from '../api';

const router = useRouter();
const mode = ref('login');
const loading = ref(false);
const isRegister = computed(() => mode.value === 'register');

const form = ref({
  username: '',
  password: '',
  phone: '',
  role: 'USER'
});

async function handleSubmit() {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请填写完整的账号与密码');
  }
  
  loading.value = true;
  try {
    if (isRegister.value) {
      await http.post('/api/auth/register', form.value);
      ElMessage.success('注册成功！正在为您自动登录');
      mode.value = 'login';
      handleSubmit(); 
      } else {
      const { data } = await http.post('/api/auth/login', {
        username: form.value.username,
        password: form.value.password
      });
      userState.login(String(data.userId), data.username, data.role, data.token);
      ElMessage.success(`欢迎回来，${data.username}`);
      
      const roleMap: Record<string, string> = {
        'ADMIN': '/admin',
        'PHARMACIST': '/vet',
        'VET': '/vet',
        'USER': '/consultation'
      };
      
      const targetPath = roleMap[data.role] || '/';
      console.log('Redirecting to:', targetPath);
      router.push(targetPath);
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败，请重试');
  } finally {
    loading.value = false;
  }
}

function handleLogout() {
  userState.logout();
  ElMessage.success('已安全登出');
}
</script>

<style scoped>
.auth-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF8A65 0%, #FFB74D 100%);
}

.auth-card {
  width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px !important;
  box-shadow: 0 20px 40px rgba(255, 138, 101, 0.25) !important;
  padding: 20px;
  border: none !important;
}

.auth-header { text-align: center; margin-bottom: 30px; }
.auth-title { font-size: 26px; font-weight: 800; color: #2C3E50; margin-bottom: 12px; }
.role-tag { font-weight: 800 !important; font-size: 14px !important; background: #FF8A65 !important; border-color: #FF8A65 !important; }
.clinic-title { font-size: 30px; font-weight: 900; color: #2C3E50; letter-spacing: -1px; margin-bottom: 8px; }
.clinic-subtitle { color: #5D4037; font-size: 15px; font-weight: 600; }

.auth-tabs { margin-bottom: 25px; }
:deep(.el-tabs__item) { font-size: 16px !important; font-weight: 800 !important; color: #5D4037 !important; }
:deep(.is-active) { color: #FF8A65 !important; }
:deep(.el-tabs__active-bar) { background-color: #FF8A65 !important; }

:deep(.el-input__wrapper) { padding: 12px 16px !important; background: #fff !important; border: 1px solid #FFD6A5 !important; }
:deep(.el-input__inner) { font-size: 15px !important; font-weight: 600 !important; color: #2C3E50 !important; }

.submit-btn {
  width: 100%;
  height: 54px !important;
  font-size: 18px !important;
  font-weight: 800 !important;
  margin-top: 20px;
  background: linear-gradient(135deg, #FF8A65 0%, #FFA07A 100%) !important;
  border: none !important;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.submit-btn:hover { opacity: 0.9; }
.full-width { width: 100%; }
</style>