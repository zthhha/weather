<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { useCityStore } from '@/stores/city'
import { useAuthStore } from '@/stores/auth'
import { airQualityApi } from '@/api/airQuality'
import { alertsApi } from '@/api/alerts'
import { collectionsApi } from '@/api/collections'
import { historyRange } from '@/utils/dateRange'
import type { AlertEvent, CollectionJobLog, Forecast, Observation } from '@/types/api'

const cityStore = useCityStore()
const auth = useAuthStore()
const latest = ref<Observation | null>(null)
const history = ref<Observation[]>([])
const forecasts = ref<Forecast[]>([])
const alerts = ref<AlertEvent[]>([])
const lastJob = ref<CollectionJobLog | null>(null)
const loading = ref(false)
const generating = ref(false)
const error = ref<string | null>(null)
const hours = ref(24)
let requestId = 0

const pollutantCards = computed<[string, number | null, string][]>(() => latest.value ? [
  ['PM2.5', latest.value.pm25, 'μg/m³'], ['PM10', latest.value.pm10, 'μg/m³'],
  ['SO₂', latest.value.so2, 'μg/m³'], ['NO₂', latest.value.no2, 'μg/m³'],
  ['CO', latest.value.co, 'mg/m³'], ['O₃', latest.value.o3, 'μg/m³'],
] : [])

const historyOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 24, top: 36, bottom: 42 },
  xAxis: { type: 'category', data: history.value.map((item) => formatTime(item.observedAt)), axisLabel: { color: '#76849b' } },
  yAxis: { type: 'value', name: 'AQI', splitLine: { lineStyle: { color: '#edf1f7' } }, axisLabel: { color: '#76849b' } },
  series: [{ name: 'AQI', type: 'line', smooth: true, symbolSize: 7, lineStyle: { width: 3, color: '#2563eb' }, itemStyle: { color: '#2563eb' }, areaStyle: { color: 'rgba(37,99,235,.12)' }, data: history.value.map((item) => item.aqi) }],
}))

const forecastOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 44, right: 24, top: 36, bottom: 42 },
  xAxis: { type: 'category', data: forecasts.value.map((item) => formatTime(item.forecastAt)), axisLabel: { color: '#76849b' } },
  yAxis: { type: 'value', name: 'AQI', splitLine: { lineStyle: { color: '#edf1f7' } }, axisLabel: { color: '#76849b' } },
  series: [{ name: '预测 AQI', type: 'line', smooth: true, symbolSize: 6, lineStyle: { width: 3, color: '#7c3aed', type: 'dashed' }, itemStyle: { color: '#7c3aed' }, areaStyle: { color: 'rgba(124,58,237,.1)' }, data: forecasts.value.map((item) => item.aqi) }],
}))

function formatTime(iso: string) { return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit', month: 'numeric', day: 'numeric', timeZone: cityStore.selectedCity?.timezone }).format(new Date(iso)) }
function formatFullTime(iso: string) { return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short', timeZone: cityStore.selectedCity?.timezone }).format(new Date(iso)) }

async function loadDashboard() {
  const cityId = cityStore.selectedId
  if (!cityId) return
  const currentRequest = ++requestId
  loading.value = true
  error.value = null
  const range = historyRange(hours.value)
  const results = await Promise.allSettled([
    airQualityApi.latest(cityId), airQualityApi.history(cityId, range.from, range.to),
    airQualityApi.forecasts(cityId), alertsApi.events({ cityId, status: 'ACTIVE' }),
    ...(auth.isAdmin ? [collectionsApi.list()] : []),
  ])
  if (currentRequest !== requestId) return
  const [latestResult, historyResult, forecastResult, alertsResult, jobsResult] = results
  latest.value = latestResult.status === 'fulfilled' ? latestResult.value : null
  history.value = historyResult.status === 'fulfilled' ? historyResult.value : []
  forecasts.value = forecastResult.status === 'fulfilled' ? forecastResult.value : []
  alerts.value = alertsResult.status === 'fulfilled' ? alertsResult.value : []
  lastJob.value = jobsResult?.status === 'fulfilled' ? jobsResult.value[0] ?? null : null
  if (latestResult.status === 'rejected' && (latestResult.reason as { status?: number }).status !== 404) error.value = latestResult.reason?.message ?? '无法加载看板数据'
  loading.value = false
}

async function regenerate() {
  if (!cityStore.selectedId) return
  generating.value = true
  try { await airQualityApi.regenerateForecast(cityStore.selectedId); await loadDashboard() } finally { generating.value = false }
}

watch(() => [cityStore.selectedId, hours.value], loadDashboard, { immediate: true })
const timer = window.setInterval(loadDashboard, 300_000)
onBeforeUnmount(() => window.clearInterval(timer))
</script>

<template>
  <section v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon class="mb-20" />
    <el-empty v-if="!cityStore.selectedCity && !cityStore.loading" description="请先在城市管理中创建并启用城市" />
    <template v-else>
      <div class="toolbar"><el-radio-group v-model="hours"><el-radio-button :value="24">24 小时</el-radio-button><el-radio-button :value="72">72 小时</el-radio-button><el-radio-button :value="168">7 天</el-radio-button></el-radio-group><el-button :icon="Refresh" @click="loadDashboard">刷新数据</el-button></div>
      <el-empty v-if="!latest" description="该城市尚无 AQI 观测数据，请在采集管理中立即采集。" />
      <template v-else>
        <div class="hero-grid">
          <article class="aqi-hero" :style="{ '--aqi-color': latest.aqiColor }"><div><p>当前空气质量 · {{ latest.cityName }}</p><h2>{{ latest.aqi }}</h2><span>{{ latest.aqiLevel }}</span></div><div class="hero-meta"><strong>{{ latest.healthAdvice }}</strong><small>观测于 {{ formatFullTime(latest.observedAt) }} · {{ latest.source }}</small></div></article>
          <article class="job-card"><p>最近采集</p><h3>{{ lastJob?.status ?? '暂无任务' }}</h3><span v-if="lastJob">成功 {{ lastJob.successfulCities }} / {{ lastJob.totalCities }} 个城市</span><span v-else>等待首次采集</span></article>
        </div>
        <div class="pollutant-grid"><article v-for="card in pollutantCards" :key="card[0]" class="metric-card"><span>{{ card[0] }}</span><strong>{{ card[1] ?? '—' }}</strong><small>{{ card[2] }}</small></article></div>
        <div class="chart-grid"><el-card shadow="never"><template #header><div class="card-title">AQI 历史趋势 <small>最多 7 天</small></div></template><v-chart v-if="history.length" class="chart" :option="historyOption" autoresize /><el-empty v-else description="此时间段没有历史数据" /></el-card><el-card shadow="never"><template #header><div class="card-title">未来 24 小时预测 <el-button v-if="auth.isAdmin" size="small" :loading="generating" @click="regenerate">重新生成</el-button></div></template><v-chart v-if="forecasts.length" class="chart" :option="forecastOption" autoresize /><el-empty v-else description="预测尚未生成" /></el-card></div>
        <el-card shadow="never" class="alerts-card"><template #header><div class="card-title">当前活动预警 <el-tag :type="alerts.length ? 'danger' : 'success'">{{ alerts.length }} 条</el-tag></div></template><el-empty v-if="!alerts.length" description="当前没有活动预警" /><el-table v-else :data="alerts"><el-table-column prop="severity" label="等级" width="100"><template #default="scope"><el-tag type="danger">{{ scope.row.severity }}</el-tag></template></el-table-column><el-table-column prop="ruleName" label="规则" /><el-table-column prop="triggeredValue" label="触发值" width="100" /><el-table-column prop="lastTriggeredAt" label="最近触发"><template #default="scope">{{ formatFullTime(scope.row.lastTriggeredAt) }}</template></el-table-column></el-table></el-card>
      </template>
    </template>
  </section>
</template>
