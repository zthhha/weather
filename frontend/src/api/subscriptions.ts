import client, { request } from './client'
import type { Subscription } from '@/types/api'

export const subscriptionsApi = {
  list: () => request<Subscription[]>(client.get('/api/v1/me/subscriptions')),
  put: (cityId: number, payload: { minimumSeverity: string; enabled: boolean }) => request<Subscription>(client.put(`/api/v1/me/subscriptions/${cityId}`, payload)),
  remove: (cityId: number) => client.delete(`/api/v1/me/subscriptions/${cityId}`),
}
