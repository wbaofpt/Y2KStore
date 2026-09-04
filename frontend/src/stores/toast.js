import { defineStore } from 'pinia'

let toastId = 0

export const useToastStore = defineStore('toast', {
  state: () => ({
    toasts: []
  }),
  actions: {
    show({ message, type = 'success', duration = 3800, action = null }) {
      const id = ++toastId
      this.toasts.push({ id, message, type, action })
      if (duration > 0) {
        setTimeout(() => this.remove(id), duration)
      }
      return id
    },
    success(message, options = {}) {
      return this.show({ message, type: 'success', ...options })
    },
    error(message, options = {}) {
      return this.show({ message, type: 'error', duration: 5000, ...options })
    },
    warning(message, options = {}) {
      return this.show({ message, type: 'warning', duration: 4500, ...options })
    },
    info(message, options = {}) {
      return this.show({ message, type: 'info', ...options })
    },
    remove(id) {
      this.toasts = this.toasts.filter(t => t.id !== id)
    }
  }
})
