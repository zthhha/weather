import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { citiesApi } from '@/api/cities'
import type { City } from '@/types/api'

const STORAGE_KEY = 'weather.selected-city-id'

export const useCityStore = defineStore('city', () => {
  const cities = ref<City[]>([])
  const selectedId = ref<number | null>(Number(localStorage.getItem(STORAGE_KEY)) || null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const enabledCities = computed(() => cities.value.filter((city) => city.enabled))
  const selectedCity = computed(() => cities.value.find((city) => city.id === selectedId.value) ?? null)

  function select(cityId: number | null) {
    selectedId.value = cityId
    if (cityId === null) localStorage.removeItem(STORAGE_KEY)
    else localStorage.setItem(STORAGE_KEY, String(cityId))
  }

  async function load() {
    loading.value = true
    error.value = null
    try {
      cities.value = (await citiesApi.list()).sort((left, right) => left.name.localeCompare(right.name))
      if (!selectedCity.value?.enabled) select(enabledCities.value[0]?.id ?? null)
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '无法加载城市'
    } finally {
      loading.value = false
    }
  }

  return { cities, enabledCities, selectedId, selectedCity, loading, error, select, load }
})
