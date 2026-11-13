<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { citiesApi } from '@/api/cities'
import TablePagination from '@/components/TablePagination.vue'
import { useCityStore } from '@/stores/city'
import type { City, CityRequest } from '@/types/api'

const PAGE_SIZE = 10
const cityStore = useCityStore()
const visible = ref(false)
const editing = ref<City | null>(null)
const saving = ref(false)
const keyword = ref('')
const page = ref(1)
const form = reactive<CityRequest>({ name: '', enabled: true })
const filteredCities = computed(() => {
  const query = keyword.value.trim().toLocaleLowerCase()
  return query ? cityStore.cities.filter((city) => city.name.toLocaleLowerCase().includes(query)) : cityStore.cities
})
const pagedCities = computed(() => filteredCities.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
watch([() => cityStore.cities, keyword], () => { page.value = 1 })

function open(city?: City) {
  editing.value = city ?? null
  Object.assign(form, city ? { name: city.name, enabled: city.enabled } : { name: '', enabled: true })
  visible.value = true
}
async function save() {
  saving.value = true
  try {
    if (editing.value) await citiesApi.update(editing.value.id, form)
    else await citiesApi.create(form)
    await cityStore.load()
    visible.value = false
    ElMessage.success('城市信息已保存，系统已自动解析和风天气位置。')
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}
</script>

<template>
  <section>
    <div class="page-actions"><div><h2>城市管理</h2><p>输入城市名称后，系统会自动解析和风天气 Location ID 和时区。</p></div><el-button type="primary" @click="open()">新增城市</el-button></div>
    <el-card shadow="never"><div class="filter-row"><el-input v-model="keyword" clearable placeholder="搜索城市名称" /></div><el-table :data="pagedCities" v-loading="cityStore.loading"><el-table-column prop="name" label="城市" /><el-table-column prop="locationId" label="Location ID（自动解析）" /><el-table-column prop="timezone" label="时区（自动解析）" /><el-table-column label="状态" width="110"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '已启用' : '已停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="120"><template #default="scope"><el-button link type="primary" @click="open(scope.row)">编辑</el-button></template></el-table-column></el-table><TablePagination v-model="page" :total="filteredCities.length" :page-size="PAGE_SIZE" /><el-empty v-if="!cityStore.loading && !filteredCities.length" :description="keyword ? '未找到匹配的城市' : '还没有城市，请新增一个城市'" /></el-card>
    <el-dialog v-model="visible" :title="editing ? '编辑城市' : '新增城市'" width="480px"><el-form label-position="top"><el-form-item label="城市名称" required><el-input v-model="form.name" placeholder="例如：海口" /></el-form-item><el-alert title="保存时将通过和风天气自动查询 Location ID 和时区。" type="info" :closable="false" show-icon /><el-form-item class="form-switch"><el-switch v-model="form.enabled" active-text="启用采集" inactive-text="停用采集" /></el-form-item></el-form><template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" :disabled="!form.name.trim()" @click="save">保存</el-button></template></el-dialog>
  </section>
</template>
