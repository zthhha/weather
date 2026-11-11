<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { alertsApi } from '@/api/alerts'
import { useCityStore } from '@/stores/city'
import type { AlertRule, AlertRuleRequest } from '@/types/api'

const cityStore = useCityStore()
const rules = ref<AlertRule[]>([])
const loading = ref(false)
const visible = ref(false)
const saving = ref(false)
const filterCityId = ref<number | undefined>()
const form = reactive<AlertRuleRequest>({ name: '', cityId: null, metric: 'AQI', operator: 'GTE', threshold: 150, severity: 'HIGH', enabled: true })
const filteredRules = computed(() => rules.value.filter((rule) => !filterCityId.value || rule.cityId === filterCityId.value))
async function load() { loading.value = true; try { rules.value = await alertsApi.rules() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '无法加载预警规则') } finally { loading.value = false } }
async function save() { saving.value = true; try { await alertsApi.createRule(form); visible.value = false; await load(); ElMessage.success('预警规则已创建') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '创建失败') } finally { saving.value = false } }
onMounted(load)
</script>

<template>
  <section><div class="page-actions"><div><h2>预警规则</h2><p>为所有城市或指定城市创建 AQI 阈值预警规则。</p></div><el-button type="primary" @click="visible = true">新建规则</el-button></div><el-card shadow="never"><div class="filter-row"><el-select v-model="filterCityId" clearable placeholder="全部城市"><el-option v-for="city in cityStore.cities" :key="city.id" :value="city.id" :label="city.name" /></el-select></div><el-table :data="filteredRules" v-loading="loading"><el-table-column prop="name" label="规则名称" /><el-table-column label="适用范围"><template #default="scope">{{ scope.row.global ? '全局规则' : scope.row.cityName }}</template></el-table-column><el-table-column prop="metric" label="指标" width="100" /><el-table-column label="条件" width="160"><template #default="scope">{{ scope.row.operator }} {{ scope.row.threshold }}</template></el-table-column><el-table-column prop="severity" label="等级" width="110"><template #default="scope"><el-tag type="danger">{{ scope.row.severity }}</el-tag></template></el-table-column><el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column></el-table><el-empty v-if="!loading && !filteredRules.length" description="暂无预警规则" /></el-card>
    <el-dialog v-model="visible" title="新建预警规则" width="500px"><el-form label-position="top"><el-form-item label="规则名称" required><el-input v-model="form.name" /></el-form-item><el-form-item label="适用城市"><el-select v-model="form.cityId" clearable placeholder="留空表示所有城市"><el-option v-for="city in cityStore.cities" :key="city.id" :label="city.name" :value="city.id" /></el-select></el-form-item><el-form-item label="触发条件"><el-row :gutter="10"><el-col :span="7"><el-select v-model="form.metric"><el-option label="AQI" value="AQI" /></el-select></el-col><el-col :span="7"><el-select v-model="form.operator"><el-option label="大于" value="GT" /><el-option label="大于等于" value="GTE" /><el-option label="小于" value="LT" /><el-option label="小于等于" value="LTE" /></el-select></el-col><el-col :span="10"><el-input-number v-model="form.threshold" :min="0" /></el-col></el-row></el-form-item><el-form-item label="严重程度"><el-select v-model="form.severity"><el-option label="LOW" value="LOW" /><el-option label="MEDIUM" value="MEDIUM" /><el-option label="HIGH" value="HIGH" /><el-option label="CRITICAL" value="CRITICAL" /></el-select></el-form-item><el-form-item><el-switch v-model="form.enabled" active-text="启用规则" inactive-text="停用规则" /></el-form-item></el-form><template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" :disabled="!form.name" @click="save">创建</el-button></template></el-dialog>
  </section>
</template>
