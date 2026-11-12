import client, { request } from './client'
import type { Notification } from '@/types/api'

export const notificationsApi = {
  list: (unreadOnly = false) => request<Notification[]>(client.get('/api/v1/me/notifications', { params: { unreadOnly } })),
  unreadCount: () => request<number>(client.get('/api/v1/me/notifications/unread-count')),
  markRead: (id: number) => request<Notification>(client.patch(`/api/v1/me/notifications/${id}/read`)),
}
