<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { alertsApi } from '@/api/alerts'
import TablePagination from '@/components/TablePagination.vue'
import { useCityStore } from '@/stores/city'
import { severityLabel } from '@/utils/severity'
import type { AlertRule, AlertRuleRequest } from '@/types/api'

const PAGE_SIZE = 10
const cityStore = useCityStore()
const rules = ref<AlertRule[]>([])
const loading = ref(false)
const visible = ref(false)
const saving = ref(false)
const editing = ref<AlertRule | null>(null)
const filterCityId = ref<number | undefined>()
const page = ref(1)
const form = reactive<AlertRuleRequest>({ name: '', cityId: null, metric: 'AQI', operator: 'GTE', threshold: 150, severity: 'HIGH', enabled: true })
const filteredRules = computed(() => rules.value.filter((rule) => !filterCityId.value || rule.cityId === filterCityId.value))
const pagedRules = computed(() => filteredRules.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
watch(filterCityId, () => { page.value = 1 })

function open(rule?: AlertRule) {
  editing.value = rule ?? null
  Object.assign(form, rule ? {
    name: rule.name, cityId: rule.cityId, metric: rule.metric, operator: rule.operator,
    threshold: rule.threshold, severity: rule.severity, enabled: rule.enabled,
  } : { name: '', cityId: null, metric: 'AQI', operator: 'GTE', threshold: 150, severity: 'HIGH', enabled: true })
  visible.value = true
}

async function load() { loading.value = true; try { rules.value = await alertsApi.rules(); page.value = 1 } catch (error) { ElMessage.error(error instanceof Error ? error.message : '无法加载预警规则') } finally { loading.value = false } }
async function save() {
  saving.value = true
  try {
    if (editing.value) await alertsApi.updateRule(editing.value.id, form)
    else await alertsApi.createRule(form)
    visible.value = false
    await load()
    ElMessage.success(editing.value ? '预警规则已修改' : '预警规则已创建')
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}
onMounted(load)
</script>

<template>
  <section><div class="page-actions"><div><h2>预警规则</h2><p>为所有城市或指定城市创建 AQI 阈值预警规则。</p></div><el-button type="primary" @click="open()">新建规则</el-button></div><el-card shadow="never"><div class="filter-row"><el-select v-model="filterCityId" filterable clearable placeholder="全部城市"><el-option v-for="city in cityStore.cities" :key="city.id" :value="city.id" :label="city.name" /></el-select></div><el-table :data="pagedRules" v-loading="loading"><el-table-column prop="name" label="规则名称" /><el-table-column label="适用范围"><template #default="scope">{{ scope.row.global ? '全局规则' : scope.row.cityName }}</template></el-table-column><el-table-column prop="metric" label="指标" width="100" /><el-table-column label="条件" width="160"><template #default="scope">{{ scope.row.operator }} {{ scope.row.threshold }}</template></el-table-column><el-table-column prop="severity" label="等级" width="110"><template #default="scope"><el-tag type="danger">{{ severityLabel(scope.row.severity) }}</el-tag></template></el-table-column><el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="100"><template #default="scope"><el-button link type="primary" @click="open(scope.row)">修改</el-button></template></el-table-column></el-table><TablePagination v-model="page" :total="filteredRules.length" :page-size="PAGE_SIZE" /><el-empty v-if="!loading && !filteredRules.length" description="暂无预警规则" /></el-card>
    <el-dialog v-model="visible" :title="editing ? '修改预警规则' : '新建预警规则'" width="500px"><el-form label-position="top"><el-form-item label="规则名称" required><el-input v-model="form.name" /></el-form-item><el-form-item label="适用城市"><el-select v-model="form.cityId" filterable clearable placeholder="输入城市名称搜索；留空表示所有城市"><el-option v-for="city in cityStore.cities" :key="city.id" :label="city.name" :value="city.id" /></el-select></el-form-item><el-form-item label="触发条件"><el-row :gutter="10"><el-col :span="7"><el-select v-model="form.metric"><el-option label="AQI" value="AQI" /></el-select></el-col><el-col :span="7"><el-select v-model="form.operator"><el-option label="大于" value="GT" /><el-option label="大于等于" value="GTE" /><el-option label="小于" value="LT" /><el-option label="小于等于" value="LTE" /></el-select></el-col><el-col :span="10"><el-input-number v-model="form.threshold" :min="0" /></el-col></el-row></el-form-item><el-form-item label="严重程度"><el-select v-model="form.severity"><el-option label="低" value="LOW" /><el-option label="中" value="MEDIUM" /><el-option label="高" value="HIGH" /><el-option label="严重" value="CRITICAL" /></el-select></el-form-item><el-form-item><el-switch v-model="form.enabled" active-text="启用规则" inactive-text="停用规则" /></el-form-item></el-form><template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" :disabled="!form.name" @click="save">{{ editing ? '保存修改' : '创建' }}</el-button></template></el-dialog>
  </section>
</template>
