<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCityStore } from '@/stores/city'

const route = useRoute()
const cityStore = useCityStore()

onMounted(() => cityStore.load())
</script>

<template>
  <el-container class="shell">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        <span class="brand-mark">AQ</span>
        <div><strong>清风预警</strong><small>Air Quality Monitor</small></div>
      </div>
      <el-menu router :default-active="route.path" class="nav" background-color="transparent" text-color="#b9c7de" active-text-color="#ffffff">
        <el-menu-item index="/"><el-icon><DataAnalysis /></el-icon><span>概览看板</span></el-menu-item>
        <el-menu-item index="/cities"><el-icon><Location /></el-icon><span>城市管理</span></el-menu-item>
        <el-menu-item index="/collections"><el-icon><Refresh /></el-icon><span>采集管理</span></el-menu-item>
        <el-menu-item index="/alert-rules"><el-icon><Setting /></el-icon><span>预警规则</span></el-menu-item>
        <el-menu-item index="/alert-events"><el-icon><Bell /></el-icon><span>预警事件</span></el-menu-item>
      </el-menu>
      <div class="sidebar-footer">毕业设计 · AQI Dashboard</div>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div><h1>{{ route.meta.title }}</h1><p>实时数据、趋势预测与风险预警</p></div>
        <el-select v-model="cityStore.selectedId" class="city-select" placeholder="选择城市" :loading="cityStore.loading" @change="cityStore.select">
          <el-option v-for="city in cityStore.enabledCities" :key="city.id" :label="city.name" :value="city.id" />
        </el-select>
      </el-header>
      <el-main class="content"><router-view /></el-main>
    </el-container>
  </el-container>
</template>
