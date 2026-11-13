import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { airQualityApi } from '@/api/airQuality'
import { citiesApi } from '@/api/cities'
import { subscriptionsApi } from '@/api/subscriptions'
import type { City } from '@/types/api'

const STORAGE_KEY = 'weather.selected-city-id'

export const useCityStore = defineStore('city', () => {
  const cities = ref<City[]>([])
  const subscribedCities = ref<City[]>([])
  const selectedId = ref<number | null>(Number(localStorage.getItem(STORAGE_KEY)) || null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const enabledCities = computed(() => cities.value.filter((city) => city.enabled))
  const selectedCity = computed(() => subscribedCities.value.find((city) => city.id === selectedId.value) ?? null)

  function select(cityId: number | null) {
    selectedId.value = cityId
    if (cityId === null) localStorage.removeItem(STORAGE_KEY)
    else localStorage.setItem(STORAGE_KEY, String(cityId))
  }

  async function load() {
    loading.value = true
    error.value = null
    try {
      const [allCities, subscriptions] = await Promise.all([citiesApi.list(), subscriptionsApi.list()])
      cities.value = allCities.sort((left, right) => left.name.localeCompare(right.name))
      const subscribedIds = new Set(subscriptions.filter((subscription) => subscription.enabled).map((subscription) => subscription.cityId))
      const subscribed = cities.value.filter((city) => city.enabled && subscribedIds.has(city.id))
      const rankings = await Promise.all(subscribed.map(async (city) => {
        try { return { city, aqi: (await airQualityApi.latest(city.id)).aqi } }
        catch { return { city, aqi: Number.NEGATIVE_INFINITY } }
      }))
      subscribedCities.value = rankings.sort((left, right) => right.aqi - left.aqi).map((entry) => entry.city)
      if (!selectedCity.value) select(subscribedCities.value[0]?.id ?? null)
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '无法加载城市'
    } finally {
      loading.value = false
    }
  }

  return { cities, enabledCities, subscribedCities, selectedId, selectedCity, loading, error, select, load }
})
