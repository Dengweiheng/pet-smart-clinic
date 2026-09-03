import { createRouter, createWebHistory } from 'vue-router';
import { userState } from '../store';
import AuthPage from '../views/AuthPage.vue';
import ConsultationPage from '../views/ConsultationPage.vue';
import PetPage from '../views/PetPage.vue';
import MallPage from '../views/MallPage.vue';
import AdminPage from '../views/AdminPage.vue';
import VetPage from '../views/VetPage.vue';
import DrugDetailPage from '../views/DrugDetailPage.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/auth' },
    { path: '/auth', component: AuthPage },
    { path: '/consultation', component: ConsultationPage, meta: { roles: ['USER'] } },
    { path: '/pets', component: PetPage, meta: { roles: ['USER'] } },
    { path: '/vet', component: VetPage, meta: { roles: ['USER', 'PHARMACIST', 'VET'] } },
    { path: '/mall', component: MallPage, meta: { roles: ['USER'] } },
    { path: '/drugs/:name', component: DrugDetailPage },
    { path: '/admin', component: AdminPage, meta: { roles: ['ADMIN'] } },
  ],
});

router.beforeEach((to, from, next) => {
  const publicPages = ['/auth'];
  const authRequired = !publicPages.includes(to.path);
  
  if (authRequired && !userState.token) {
    return next('/auth');
  }

  if (to.meta.roles) {
    const roles = to.meta.roles as string[];
    if (!roles.includes(userState.role)) {
      return next('/auth'); // 或者跳转到一个 403 页面
    }
  }

  next();
});

export default router;
