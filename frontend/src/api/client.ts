import axios, { AxiosError } from 'axios'
import type { ApiEnvelope } from '@/types/api'

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

export async function request<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  try {
    return (await promise).data.data
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>
    const status = axiosError.response?.status
    const message = axiosError.response?.data?.message
      ?? (status === 404 ? '暂无可用数据' : undefined)
      ?? axiosError.message
      ?? '请求失败，请稍后重试'
    throw new ApiError(message, status)
  }
}

export default client
