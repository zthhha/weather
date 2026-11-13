<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  total: number
  modelValue: number
  pageSize?: number
}>(), { pageSize: 10 })

const emit = defineEmits<{ 'update:modelValue': [page: number] }>()
const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const currentPage = computed(() => Math.min(Math.max(props.modelValue, 1), pageCount.value))

function change(page: number) {
  emit('update:modelValue', page)
}
</script>

<template>
  <div v-if="total" class="table-pagination">
    <span class="pagination-summary">共 {{ total }} 条</span>
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="change"
    />
  </div>
</template>
