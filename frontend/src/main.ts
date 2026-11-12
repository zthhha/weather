import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { LineChart } from 'echarts/charts'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import { router } from './router'
import { useAuthStore } from './stores/auth'

use([CanvasRenderer, GridComponent, TooltipComponent, LineChart])

const app = createApp(App)
const pinia = createPinia()
app.use(pinia).use(router)
app.use(ElementPlus)

const auth = useAuthStore(pinia)
auth.restore().finally(() => {
  app.mount('#app')
})