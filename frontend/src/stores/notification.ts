import { ref } from 'vue'
import { defineStore } from 'pinia'
import { notificationsApi } from '@/api/notifications'

export const useNotificationStore = defineStore('notifications', () => {
  const unreadCount = ref(0)

  async function loadUnreadCount() {
    try {
      unreadCount.value = await notificationsApi.unreadCount()
    } catch {
      unreadCount.value = 0
    }
  }

  return { unreadCount, loadUnreadCount }
})
