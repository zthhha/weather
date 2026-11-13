<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { airQualityApi } from '@/api/airQuality'
import TablePagination from '@/components/TablePagination.vue'
import type { CityRanking } from '@/types/api'

const PAGE_SIZE = 10
const rows = ref<CityRanking[]>([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pagedRows = computed(() => rows.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

async function load() {
  loading.value = true; error.value = ''
  try { rows.value = await airQualityApi.rankings(100); page.value = 1 }
  catch (cause) { error.value = cause instanceof Error ? cause.message : '无法加载排行' }
  finally { loading.value = false }
}
function time(iso: string) { return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso)) }
onMounted(load)
</script>

<template><section><div class="page-actions"><div><h2>城市 AQI 排行</h2><p>按每个启用城市的最新 AQI 从高到低排列。</p></div><el-button @click="load">刷新</el-button></div><el-alert v-if="error" :title="error" type="error" class="mb-20" show-icon /><el-card shadow="never"><el-table :data="pagedRows" v-loading="loading"><el-table-column type="index" :index="(index: number) => (page - 1) * PAGE_SIZE + index + 1" label="#" width="60" /><el-table-column prop="cityName" label="城市" /><el-table-column prop="aqi" label="AQI" width="110"><template #default="scope"><strong :style="{ color: scope.row.aqiColor }">{{ scope.row.aqi }}</strong></template></el-table-column><el-table-column prop="aqiLevel" label="等级" width="120"><template #default="scope"><el-tag :style="{ backgroundColor: scope.row.aqiColor, borderColor: scope.row.aqiColor, color: '#fff' }">{{ scope.row.aqiLevel }}</el-tag></template></el-table-column><el-table-column label="观测时间"><template #default="scope">{{ time(scope.row.observedAt) }}</template></el-table-column></el-table><TablePagination v-model="page" :total="rows.length" :page-size="PAGE_SIZE" /><el-empty v-if="!loading && !rows.length" description="暂无可排行的城市观测数据" /></el-card></section></template>
