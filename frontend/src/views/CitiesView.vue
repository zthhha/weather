<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { citiesApi } from '@/api/cities'
import { useCityStore } from '@/stores/city'
import type { City, CityRequest } from '@/types/api'

const cityStore = useCityStore()
const visible = ref(false)
const editing = ref<City | null>(null)
const saving = ref(false)
const form = reactive<CityRequest>({ name: '', locationId: '', timezone: 'Asia/Shanghai', enabled: true })

function open(city?: City) {
  editing.value = city ?? null
  Object.assign(form, city ? { name: city.name, locationId: city.locationId, timezone: city.timezone, enabled: city.enabled } : { name: '', locationId: '', timezone: 'Asia/Shanghai', enabled: true })
  visible.value = true
}
async function save() {
  saving.value = true
  try {
    if (editing.value) await citiesApi.update(editing.value.id, form)
    else await citiesApi.create(form)
    await cityStore.load()
    visible.value = false
    ElMessage.success('城市信息已保存')
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}
</script>

<template>
  <section>
    <div class="page-actions"><div><h2>城市管理</h2><p>配置和风天气 Location ID，并控制是否参与定时采集。</p></div><el-button type="primary" @click="open()">新增城市</el-button></div>
    <el-card shadow="never"><el-table :data="cityStore.cities" v-loading="cityStore.loading"><el-table-column prop="name" label="城市" /><el-table-column prop="locationId" label="Location ID" /><el-table-column prop="timezone" label="时区" /><el-table-column label="状态" width="110"><template #default="scope"><el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '已启用' : '已停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="120"><template #default="scope"><el-button link type="primary" @click="open(scope.row)">编辑</el-button></template></el-table-column></el-table><el-empty v-if="!cityStore.loading && !cityStore.cities.length" description="还没有城市，请新增一个城市" /></el-card>
    <el-dialog v-model="visible" :title="editing ? '编辑城市' : '新增城市'" width="480px"><el-form label-position="top"><el-form-item label="城市名称" required><el-input v-model="form.name" /></el-form-item><el-form-item label="和风天气 Location ID" required><el-input v-model="form.locationId" /></el-form-item><el-form-item label="时区" required><el-input v-model="form.timezone" /></el-form-item><el-form-item><el-switch v-model="form.enabled" active-text="启用采集" inactive-text="停用采集" /></el-form-item></el-form><template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog>
  </section>
</template>
