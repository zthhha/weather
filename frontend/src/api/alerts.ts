import client, { request } from './client'
import type { AlertEvent, AlertRule, AlertRuleRequest } from '@/types/api'

export const alertsApi = {
  rules: (params?: Record<string, unknown>) => request<AlertRule[]>(client.get('/api/v1/alert-rules', { params })),
  createRule: (payload: AlertRuleRequest) => request<AlertRule>(client.post('/api/v1/alert-rules', payload)),
  updateRule: (id: number, payload: AlertRuleRequest) => request<AlertRule>(client.put(`/api/v1/alert-rules/${id}`, payload)),
  events: (params?: Record<string, unknown>) => request<AlertEvent[]>(client.get('/api/v1/alert-events', { params })),
}
