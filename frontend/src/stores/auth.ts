import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import type { AuthenticationResponse, Credentials, CurrentUser } from '@/types/auth'

const TOKEN_KEY = 'weather.access-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(sessionStorage.getItem(TOKEN_KEY))
  const user = ref<CurrentUser | null>(null)
  const loading = ref(false)
  const isAdmin = computed(() => user.value?.roles.includes('ROLE_ADMIN') ?? false)
  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  function persist(authentication: AuthenticationResponse) {
    token.value = authentication.accessToken
    sessionStorage.setItem(TOKEN_KEY, authentication.accessToken)
  }

  async function login(credentials: Credentials) {
    loading.value = true
    try {
      persist(await authApi.login(credentials))
      user.value = await authApi.me()
    } finally { loading.value = false }
  }

  async function register(credentials: Credentials) {
    loading.value = true
    try {
      persist(await authApi.register(credentials))
      user.value = await authApi.me()
    } finally { loading.value = false }
  }

  async function restore() {
    if (!token.value) return
    try { user.value = await authApi.me() } catch { logout() }
  }

  function logout() {
    token.value = null
    user.value = null
    sessionStorage.removeItem(TOKEN_KEY)
  }

  return { token, user, loading, isAdmin, isAuthenticated, login, register, restore, logout }
})
