<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { notificationsApi } from '@/api/notifications'
import TablePagination from '@/components/TablePagination.vue'
import { severityLabel } from '@/utils/severity'
import { useNotificationStore } from '@/stores/notification'
import type { Notification } from '@/types/api'

const PAGE_SIZE = 10
const notifications = ref<Notification[]>([])
const loading = ref(false)
const unreadOnly = ref(false)
const error = ref('')
const page = ref(1)
const notificationStore = useNotificationStore()
const pagedNotifications = computed(() => notifications.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

async function load() { loading.value = true; error.value = ''; try { notifications.value = await notificationsApi.list(unreadOnly.value); page.value = 1; await notificationStore.loadUnreadCount() } catch (cause) { error.value = cause instanceof Error ? cause.message : '无法加载站内预警' } finally { loading.value = false } }
async function markRead(item: Notification) { if (item.readAt) return; await notificationsApi.markRead(item.id); await load() }
function time(iso: string) { return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso)) }
watch(unreadOnly, () => { page.value = 1 })
onMounted(load)
</script>

<template><section><div class="page-actions"><div><h2>我的站内预警</h2><p>查看订阅城市触发的空气质量预警。</p></div><el-button @click="load">刷新</el-button></div><el-card shadow="never"><div class="filter-row"><el-switch v-model="unreadOnly" active-text="仅未读" @change="load" /></div><el-alert v-if="error" :title="error" type="error" class="mb-20" show-icon /><el-table :data="pagedNotifications" v-loading="loading"><el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.readAt ? 'info' : 'danger'">{{ scope.row.readAt ? '已读' : '未读' }}</el-tag></template></el-table-column><el-table-column prop="cityName" label="城市" /><el-table-column prop="ruleName" label="规则" /><el-table-column prop="severity" label="等级" width="100"><template #default="scope"><el-tag type="danger">{{ severityLabel(scope.row.severity) }}</el-tag></template></el-table-column><el-table-column label="最近触发"><template #default="scope">{{ time(scope.row.lastTriggeredAt) }}</template></el-table-column><el-table-column label="操作" width="100"><template #default="scope"><el-button v-if="!scope.row.readAt" link type="primary" @click="markRead(scope.row)">标为已读</el-button></template></el-table-column></el-table><TablePagination v-model="page" :total="notifications.length" :page-size="PAGE_SIZE" /><el-empty v-if="!loading && !notifications.length" description="暂无站内预警" /></el-card></section></template>
