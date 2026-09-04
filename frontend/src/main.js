import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Import Bootstrap CSS & Bundle JS
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
// Import Bootstrap Icons
import 'bootstrap-icons/font/bootstrap-icons.css'

// Import Custom Style (overrides bootstrap)
import './style.css'

import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Initialize JWT token in Axios headers if exists
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')
