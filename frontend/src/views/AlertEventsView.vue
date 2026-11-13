<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { alertsApi } from '@/api/alerts'
import TablePagination from '@/components/TablePagination.vue'
import { severityLabel } from '@/utils/severity'
import { useCityStore } from '@/stores/city'
import type { AlertEvent, AlertEventStatus } from '@/types/api'

const PAGE_SIZE = 10
const cityStore = useCityStore()
const events = ref<AlertEvent[]>([])
const loading = ref(false)
const cityId = ref<number | undefined>()
const status = ref<AlertEventStatus | undefined>()
const page = ref(1)
const filteredEvents = computed(() => events.value.filter((event) => (!cityId.value || event.cityId === cityId.value) && (!status.value || event.status === status.value)))
const pagedEvents = computed(() => filteredEvents.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
watch([cityId, status], () => { page.value = 1 })
async function load() { loading.value = true; try { events.value = await alertsApi.events(); page.value = 1 } catch (error) { ElMessage.error(error instanceof Error ? error.message : '无法加载预警事件') } finally { loading.value = false } }
function date(value: string | null) { return value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—' }
onMounted(load)
</script>

<template>
  <section><div class="page-actions"><div><h2>预警事件</h2><p>查看系统自动触发、持续更新和自动恢复的空气质量预警。</p></div><el-button @click="load">刷新</el-button></div><el-card shadow="never"><div class="filter-row"><el-select v-model="cityId" clearable placeholder="全部城市"><el-option v-for="city in cityStore.cities" :key="city.id" :value="city.id" :label="city.name" /></el-select><el-select v-model="status" clearable placeholder="全部状态"><el-option value="ACTIVE" label="活动中" /><el-option value="RESOLVED" label="已恢复" /></el-select></div><el-table :data="pagedEvents" v-loading="loading"><el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'">{{ scope.row.status === 'ACTIVE' ? '活动中' : '已恢复' }}</el-tag></template></el-table-column><el-table-column prop="cityName" label="城市" /><el-table-column prop="ruleName" label="规则" /><el-table-column prop="severity" label="等级" width="100"><template #default="scope"><el-tag type="danger">{{ severityLabel(scope.row.severity) }}</el-tag></template></el-table-column><el-table-column label="条件" width="140"><template #default="scope">{{ scope.row.metric }} {{ scope.row.operator }} {{ scope.row.threshold }}</template></el-table-column><el-table-column prop="triggeredValue" label="触发值" width="90" /><el-table-column label="最近触发" min-width="170"><template #default="scope">{{ date(scope.row.lastTriggeredAt) }}</template></el-table-column><el-table-column label="恢复时间" min-width="170"><template #default="scope">{{ date(scope.row.resolvedAt) }}</template></el-table-column></el-table><TablePagination v-model="page" :total="filteredEvents.length" :page-size="PAGE_SIZE" /><el-empty v-if="!loading && !filteredEvents.length" description="暂无符合条件的预警事件" /></el-card></section>
</template>
