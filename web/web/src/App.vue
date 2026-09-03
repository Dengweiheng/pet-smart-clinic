<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-container">
        <!-- 品牌 Logo -->
        <div class="brand" @click="router.push('/')">
          <el-icon :size="32" color="#FF8A65"><ElementPlus /></el-icon>
          <h3 class="brand-name">宠物智能系统</h3>
        </div>

        <!-- 自定义导航菜单 -->
        <div class="nav-menu">
          <router-link
            to="/consultation"
            class="nav-item"
            :class="{ active: $route.path === '/consultation' }"
          >
            <el-icon><ChatDotRound /></el-icon>
            <span>AI咨询</span>
          </router-link>
          <router-link
            to="/pets"
            class="nav-item"
            :class="{ active: $route.path === '/pets' }"
          >
            <el-icon><Notebook /></el-icon>
            <span>我的宠物</span>
          </router-link>
          <router-link
            to="/vet"
            class="nav-item"
            :class="{ active: $route.path === '/vet' }"
          >
            <el-icon><FirstAidKit /></el-icon>
            <span>在线问诊</span>
          </router-link>
          <router-link
            to="/mall"
            class="nav-item"
            :class="{ active: $route.path === '/mall' }"
          >
            <el-icon><ShoppingCart /></el-icon>
            <span>药品商城</span>
          </router-link>
        </div>

        <!-- 用户头像 -->
        <div class="user-icon-wrapper">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :size="40" icon="UserFilled" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <div class="dropdown-user-info">
                    <div class="user-name">{{ userState.username || '未登录' }}</div>
                    <div class="user-role">{{ userState.role || '游客' }}</div>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="profile">账户中心</el-dropdown-item>
                <el-dropdown-item v-if="userState.role === 'ADMIN'" command="admin">系统管理</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容：不再显示全局页面标题 -->
    <main class="content" :class="{ 'full-width': route.path === '/consultation' }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userState } from './store';
import {
  ChatDotRound,
  Notebook,
  FirstAidKit,
  ShoppingCart,
  ElementPlus,
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userState.logout();
    ElMessage.success('已安全退出');
    router.push('/auth');
  } else if (command === 'profile') {
    router.push('/auth');
  } else if (command === 'admin') {
    router.push('/admin');
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  background: #FFF8F0;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}
</style>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #FFF8F0;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 138, 101, 0.2);
  box-shadow: 0 4px 12px rgba(255, 138, 101, 0.06);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 74px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  transition: opacity 150ms;
}
.brand:hover { opacity: 0.85; }
.brand-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #FF8A65 0%, #FFA07A 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: -0.01em;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.nav-menu::-webkit-scrollbar { display: none; }

.nav-item {
  display: flex !important;
  align-items: center;
  gap: 8px;
  height: 74px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 500;
  color: #4a5568;
  text-decoration: none;
  border-bottom: 3px solid transparent;
  transition: all 150ms ease;
  white-space: nowrap;
  flex-shrink: 0;
  background: transparent;
}
.nav-item:hover {
  background: rgba(255, 138, 101, 0.08);
  border-bottom-color: rgba(255, 138, 101, 0.4);
}
.nav-item.active {
  border-bottom-color: #FF8A65 !important;
  color: #FF8A65 !important;
  font-weight: 600 !important;
}
.nav-item .el-icon { font-size: 20px; }

.user-icon-wrapper {
  display: flex;
  align-items: center;
  margin-left: 16px;
}
.user-avatar {
  cursor: pointer;
  transition: transform 150ms, box-shadow 150ms;
  border-radius: 50%;
}
.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 138, 101, 0.3);
}
.dropdown-user-info {
  padding: 4px 0;
  min-width: 140px;
}
.user-name {
  font-weight: 700;
  color: #2C3E50;
  margin-bottom: 2px;
  font-size: 15px;
}
.user-role {
  font-size: 13px;
  color: #7f8c8d;
}

.content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 32px;
  box-sizing: border-box;
}
.content.full-width {
  max-width: none;
  padding: 0;
}

.fade-enter-active, .fade-leave-active { transition: opacity 150ms ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .header-container { padding: 0 16px; height: 64px; }
  .brand-name { font-size: 20px; }
  .nav-item { padding: 0 16px; height: 64px; font-size: 14px; }
  .content { padding: 20px 16px; }
  .content.full-width { padding: 0; }
}

@media (max-width: 480px) {
  .app-header { position: relative; }
  .header-container { height: auto; flex-wrap: wrap; padding: 12px 16px; }
  .brand { order: 1; }
  .user-icon-wrapper { order: 2; }
  .nav-menu { order: 3; width: 100%; justify-content: flex-start; margin-top: 8px; }
  .nav-item { height: 48px; padding: 0 16px; font-size: 14px; }
}
</style>