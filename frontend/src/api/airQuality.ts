import client, { request } from './client'
import type { Forecast, Observation } from '@/types/api'

export const airQualityApi = {
  latest: (cityId: number) => request<Observation>(client.get(`/api/v1/air-quality/cities/${cityId}/latest`)),
  history: (cityId: number, from: string, to: string) => request<Observation[]>(client.get(`/api/v1/air-quality/cities/${cityId}/history`, { params: { from, to } })),
  forecasts: (cityId: number) => request<Forecast[]>(client.get(`/api/v1/forecasts/cities/${cityId}`)),
  regenerateForecast: (cityId: number) => request<{ qualityStatus: string; pointsGenerated: number }>(client.post(`/api/v1/forecasts/cities/${cityId}/regenerate`)),
}
