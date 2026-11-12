import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import CitiesView from '@/views/CitiesView.vue'
import CollectionsView from '@/views/CollectionsView.vue'
import AlertRulesView from '@/views/AlertRulesView.vue'
import AlertEventsView from '@/views/AlertEventsView.vue'
import SubscriptionsView from '@/views/SubscriptionsView.vue'
import AuthView from '@/views/AuthView.vue'
import { useAuthStore } from '@/stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: AuthView, props: { register: false }, meta: { public: true, title: '登录' } },
    { path: '/register', component: AuthView, props: { register: true }, meta: { public: true, title: '注册' } },
    { path: '/', component: DashboardView, meta: { title: '空气质量看板' } },
    { path: '/subscriptions', component: SubscriptionsView, meta: { title: '城市订阅' } },
    { path: '/alert-events', component: AlertEventsView, meta: { title: '预警事件' } },
    { path: '/cities', component: CitiesView, meta: { title: '城市管理', admin: true } },
    { path: '/collections', component: CollectionsView, meta: { title: '采集管理', admin: true } },
    { path: '/alert-rules', component: AlertRulesView, meta: { title: '预警规则', admin: true } },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.token && !auth.user) await auth.restore()
  if (to.meta.public) return auth.isAuthenticated ? '/' : true
  if (!auth.isAuthenticated) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.admin && !auth.isAdmin) return '/'
  return true
})
