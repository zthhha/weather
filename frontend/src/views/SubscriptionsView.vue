<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import TablePagination from '@/components/TablePagination.vue'
import { ElMessage } from 'element-plus'
import { airQualityApi } from '@/api/airQuality'
import { citiesApi } from '@/api/cities'
import { subscriptionsApi } from '@/api/subscriptions'
import type { City, Subscription } from '@/types/api'

const subscriptions = ref<Subscription[]>([])
const aqiByCityId = ref<Record<number, number | null>>({})
const loading = ref(false)
const keyword = ref('')
const results = ref<City[]>([])
const searching = ref(false)
const PAGE_SIZE = 10
const page = ref(1)
const subscribedIds = computed(() => new Set(subscriptions.value.map((item) => item.cityId)))
const pagedSubscriptions = computed(() => subscriptions.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
watch(subscriptions, () => { page.value = 1 })

async function load() {
  loading.value = true
  try {
    subscriptions.value = await subscriptionsApi.list()
    const readings = await Promise.all(subscriptions.value.map(async (subscription) => {
      try { return [subscription.cityId, (await airQualityApi.latest(subscription.cityId)).aqi] as const }
      catch { return [subscription.cityId, null] as const }
    }))
    aqiByCityId.value = Object.fromEntries(readings)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '无法加载订阅')
  } finally {
    loading.value = false
  }
}
async function search() { if (!keyword.value.trim()) return; searching.value = true; try { results.value = await citiesApi.search(keyword.value.trim()) } catch (error) { ElMessage.error(error instanceof Error ? error.message : '城市搜索失败') } finally { searching.value = false } }
async function subscribe(city: City) { try { await subscriptionsApi.put(city.id, { minimumSeverity: 'MEDIUM', enabled: true }); await load(); ElMessage.success('城市订阅已保存') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '订阅失败') } }
async function remove(cityId: number) { try { await subscriptionsApi.remove(cityId); await load(); ElMessage.success('已取消订阅') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '取消失败') } }
onMounted(load)
</script>

<template><section><div class="page-actions"><div><h2>城市订阅</h2><p>搜索预置城市；未收录城市会由系统自动解析后加入并订阅。</p></div></div><el-card shadow="never" class="subscription-form"><el-input v-model="keyword" placeholder="输入城市名称，例如：海口" clearable @keyup.enter="search" /><el-button type="primary" :loading="searching" @click="search">搜索城市</el-button></el-card><el-card v-if="results.length" shadow="never" class="mb-20"><template #header>搜索结果</template><el-table :data="results"><el-table-column prop="name" label="城市" /><el-table-column prop="timezone" label="时区" /><el-table-column label="采集状态"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'warning'">{{ scope.row.enabled ? '采集中' : '尚未启用采集' }}</el-tag></template></el-table-column><el-table-column label="操作" width="120"><template #default="scope"><el-button type="primary" link :disabled="subscribedIds.has(scope.row.id)" @click="subscribe(scope.row)">{{ subscribedIds.has(scope.row.id) ? '已订阅' : '订阅' }}</el-button></template></el-table-column></el-table></el-card><el-card shadow="never"><template #header>我的订阅</template><el-table :data="pagedSubscriptions" v-loading="loading"><el-table-column prop="cityName" label="城市" /><el-table-column label="AQI" width="100"><template #default="scope">{{ aqiByCityId[scope.row.cityId] ?? '—' }}</template></el-table-column><el-table-column label="状态"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="100"><template #default="scope"><el-button link type="danger" @click="remove(scope.row.cityId)">取消订阅</el-button></template></el-table-column></el-table><TablePagination v-model="page" :total="subscriptions.length" :page-size="PAGE_SIZE" /><el-empty v-if="!loading && !subscriptions.length" description="还没有订阅城市" /></el-card></section></template>
