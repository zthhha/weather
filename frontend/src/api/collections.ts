import client, { request } from './client'
import type { CollectionJobLog } from '@/types/api'

export const collectionsApi = {
  collectNow: () => request<CollectionJobLog>(client.post('/api/v1/collections')),
  list: () => request<CollectionJobLog[]>(client.get('/api/v1/collections')),
}
