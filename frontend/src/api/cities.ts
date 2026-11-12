import client, { request } from './client'
import type { City, CityRequest } from '@/types/api'

export const citiesApi = {
  list: () => request<City[]>(client.get('/api/v1/cities')),
  create: (payload: CityRequest) => request<City>(client.post('/api/v1/cities', payload)),
  update: (id: number, payload: CityRequest) => request<City>(client.put(`/api/v1/cities/${id}`, payload)),
  search: (keyword: string) => request<City[]>(client.get('/api/v1/city-search', { params: { keyword } })),
  searchAndSubscribe: (keyword: string) => request<City>(client.get('/api/v1/city-search/subscribe', { params: { keyword } })),
}
