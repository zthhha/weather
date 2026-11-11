import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import CitiesView from '@/views/CitiesView.vue'
import CollectionsView from '@/views/CollectionsView.vue'
import AlertRulesView from '@/views/AlertRulesView.vue'
import AlertEventsView from '@/views/AlertEventsView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: DashboardView, meta: { title: '空气质量看板' } },
    { path: '/cities', component: CitiesView, meta: { title: '城市管理' } },
    { path: '/collections', component: CollectionsView, meta: { title: '采集管理' } },
    { path: '/alert-rules', component: AlertRulesView, meta: { title: '预警规则' } },
    { path: '/alert-events', component: AlertEventsView, meta: { title: '预警事件' } },
  ],
})
