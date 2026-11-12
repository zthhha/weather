<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { subscriptionsApi } from '@/api/subscriptions'
import { useCityStore } from '@/stores/city'
import type { Subscription } from '@/types/api'

const cityStore = useCityStore()
const subscriptions = ref<Subscription[]>([])
const loading = ref(false)
const selectedCityId = ref<number | undefined>()
const severity = ref('MEDIUM')
const subscribedIds = computed(() => new Set(subscriptions.value.map((item) => item.cityId)))

async function load() { loading.value = true; try { subscriptions.value = await subscriptionsApi.list() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '无法加载订阅') } finally { loading.value = false } }
async function subscribe() { if (!selectedCityId.value) return; try { await subscriptionsApi.put(selectedCityId.value, { minimumSeverity: severity.value, enabled: true }); await load(); ElMessage.success('城市订阅已保存') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '订阅失败') } }
async function remove(cityId: number) { try { await subscriptionsApi.remove(cityId); await load(); ElMessage.success('已取消订阅') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '取消失败') } }
onMounted(load)
</script>

<template><section><div class="page-actions"><div><h2>城市订阅</h2><p>订阅城市并设置接收站内预警的最低严重程度。</p></div></div><el-card shadow="never" class="subscription-form"><el-select v-model="selectedCityId" placeholder="选择城市"><el-option v-for="city in cityStore.enabledCities.filter((city) => !subscribedIds.has(city.id))" :key="city.id" :label="city.name" :value="city.id" /></el-select><el-select v-model="severity"><el-option value="LOW" label="LOW" /><el-option value="MEDIUM" label="MEDIUM" /><el-option value="HIGH" label="HIGH" /><el-option value="CRITICAL" label="CRITICAL" /></el-select><el-button type="primary" :disabled="!selectedCityId" @click="subscribe">订阅城市</el-button></el-card><el-card shadow="never"><el-table :data="subscriptions" v-loading="loading"><el-table-column prop="cityName" label="城市" /><el-table-column prop="minimumSeverity" label="最低预警等级" /><el-table-column label="状态"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="100"><template #default="scope"><el-button link type="danger" @click="remove(scope.row.cityId)">取消订阅</el-button></template></el-table-column></el-table><el-empty v-if="!loading && !subscriptions.length" description="还没有订阅城市" /></el-card></section></template>
