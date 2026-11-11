<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { collectionsApi } from '@/api/collections'
import type { CollectionJobLog } from '@/types/api'

const jobs = ref<CollectionJobLog[]>([])
const loading = ref(false)
const collecting = ref(false)

async function load() { loading.value = true; try { jobs.value = await collectionsApi.list() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '无法加载采集日志') } finally { loading.value = false } }
async function collectNow() {
  try {
    await ElMessageBox.confirm('将向所有启用城市发起空气质量采集。', '立即采集', { type: 'warning' })
    collecting.value = true
    const job = await collectionsApi.collectNow()
    ElMessage.success(`采集完成：成功 ${job.successfulCities}/${job.totalCities}`)
    await load()
  } catch (error) { if (error !== 'cancel') ElMessage.error(error instanceof Error ? error.message : '采集失败') } finally { collecting.value = false }
}
onMounted(load)
</script>

<template>
  <section><div class="page-actions"><div><h2>采集管理</h2><p>手动采集所有启用城市的数据，并检查采集任务结果。</p></div><el-button type="primary" :loading="collecting" @click="collectNow">立即采集</el-button></div><el-card shadow="never"><el-table :data="jobs" v-loading="loading"><el-table-column prop="status" label="状态" width="140"><template #default="scope"><el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : scope.row.status === 'FAILED' ? 'danger' : 'warning'">{{ scope.row.status }}</el-tag></template></el-table-column><el-table-column prop="startedAt" label="开始时间" min-width="180" /><el-table-column prop="finishedAt" label="结束时间" min-width="180" /><el-table-column label="结果" min-width="150"><template #default="scope">{{ scope.row.successfulCities }} 成功 / {{ scope.row.failedCities }} 失败 / 共 {{ scope.row.totalCities }}</template></el-table-column><el-table-column prop="errorSummary" label="错误摘要" min-width="260" /></el-table><el-empty v-if="!loading && !jobs.length" description="暂无采集任务记录" /></el-card></section>
</template>
