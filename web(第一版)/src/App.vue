<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <el-icon :size="32" color="#10b981"><ElementPlus /></el-icon>
        <h2>宠物智能系统</h2>
      </div>
      <el-menu :default-active="activePath" router class="menu">
        <el-menu-item index="/auth">
          <el-icon><User /></el-icon>
          <span>账户中心</span>
        </el-menu-item>
        
        <!-- 根据角色动态显示菜单 -->
        <template v-for="menu in filteredMenu" :key="menu.path">
          <el-menu-item :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>
    <main class="content">
      <div class="page-header" v-if="activePath !== '/auth'">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="user-info-bar">
          <el-tag v-if="userState.username" effect="plain" type="info">
            {{ userState.username }} ({{ userState.role }})
          </el-tag>
        </div>
      </div>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { userState } from './store';

const route = useRoute();
const activePath = computed(() => route.path);

const allMenus = [
  { path: '/consultation', title: '智能咨询', icon: 'ChatDotRound', roles: ['USER'] },
  { path: '/pets', title: '宠物档案', icon: 'Notebook', roles: ['USER'] },
  { path: '/vet', title: '执业药师', icon: 'FirstAidKit', roles: ['USER', 'PHARMACIST', 'VET'] },
  { path: '/mall', title: '药品商城', icon: 'ShoppingCart', roles: ['USER', 'ADMIN'] },
  { path: '/admin', title: '系统管理', icon: 'Setting', roles: ['ADMIN'] },
];

const filteredMenu = computed(() => {
  return allMenus.filter(menu => menu.roles.includes(userState.role));
});

const pageTitle = computed(() => {
  const item = allMenus.find(m => m.path === route.path);
  return item ? item.title : '账户中心';
});
</script>

<style scoped>
.user-info-bar { margin-top: 8px; }
</style>
