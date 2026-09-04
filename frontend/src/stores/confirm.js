import { defineStore } from 'pinia'

export const useConfirmStore = defineStore('confirm', {
  state: () => ({
    visible: false,
    title: 'Xác nhận',
    message: '',
    confirmText: 'Xác nhận',
    cancelText: 'Hủy',
    variant: 'danger',
    _resolve: null
  }),
  actions: {
    open({ title, message, confirmText, cancelText, variant } = {}) {
      return new Promise((resolve) => {
        this.title = title || 'Xác nhận'
        this.message = message || ''
        this.confirmText = confirmText || 'Xác nhận'
        this.cancelText = cancelText || 'Hủy'
        this.variant = variant || 'danger'
        this.visible = true
        this._resolve = resolve
      })
    },
    accept() {
      this.visible = false
      this._resolve?.(true)
      this._resolve = null
    },
    cancel() {
      this.visible = false
      this._resolve?.(false)
      this._resolve = null
    }
  }
})
