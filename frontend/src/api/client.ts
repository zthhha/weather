import axios, { AxiosError } from 'axios'
import { router } from '@/router'
import type { ApiEnvelope } from '@/types/api'

const TOKEN_KEY = 'weather.access-token'

export class ApiError extends Error {
  readonly status: number | undefined

  constructor(message: string, status?: number) {
    super(message)
    this.status = status
  }
}

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15_000,
})

client.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(undefined, (error) => {
  if (error.response?.status === 401 && !location.pathname.startsWith('/login')) {
    sessionStorage.removeItem(TOKEN_KEY)
    router.replace('/login')
  }
  return Promise.reject(error)
})

export async function request<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  try {
    return (await promise).data.data
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>
    const status = axiosError.response?.status
    const body = axiosError.response?.data as { message?: string; data?: { message?: string } } | undefined
    const message = body?.data?.message
      ?? body?.message
      ?? (status === 404 ? '暂无可用数据' : undefined)
      ?? axiosError.message
      ?? '请求失败，请稍后重试'
    throw new ApiError(message, status)
  }
}

export default client
