<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ register?: boolean }>()
const auth = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const error = ref('')

async function submit() {
  error.value = ''
  try {
    if (props.register) await auth.register(form)
    else await auth.login(form)
    ElMessage.success(props.register ? '注册成功，已自动登录' : '登录成功')
    await router.replace('/')
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '操作失败' }
}
</script>

<template>
  <main class="auth-page"><section class="auth-card"><div class="auth-logo">AQ</div><h1>清风预警</h1><p>{{ register ? '创建账号，订阅你关心的城市空气质量。' : '登录后查看个人看板与站内预警。' }}</p><el-alert v-if="error" :title="error" type="error" show-icon class="auth-error" /><el-form label-position="top" @submit.prevent="submit"><el-form-item label="账号"><el-input v-model="form.username" autocomplete="username" placeholder="3 到 64 个字符" /></el-form-item><el-form-item label="密码"><el-input v-model="form.password" type="password" autocomplete="current-password" placeholder="至少 6 个字符" show-password /></el-form-item><el-button type="primary" native-type="submit" class="auth-submit" :loading="auth.loading" :disabled="form.username.length < 3 || form.password.length < 6">{{ register ? '注册并登录' : '登录' }}</el-button></el-form><router-link v-if="!register" to="/register">没有账号？立即注册</router-link><router-link v-else to="/login">已有账号？前往登录</router-link><el-divider /><small>管理员演示账号：admin / 123456</small></section></main>
</template>
