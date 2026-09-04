import { defineStore } from 'pinia'
import axios from 'axios'
import { useAuthStore } from './auth'
import { useNotificationStore } from './notifications'
import { useToastStore } from './toast'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),
  getters: {
    totalCount: (state) => state.items.reduce((total, item) => total + Number(item.quantity || 0), 0),
    totalAmount: (state) => state.items.reduce((total, item) => total + (Number(item.unitPrice || 0) * Number(item.quantity || 0)), 0)
  },
  actions: {
    mapItem(dto) {
      return {
        id: dto.id ?? dto.cartItemId ?? dto.variantId,
        cartItemId: dto.id ?? null,
        variantId: dto.variantId ?? null,
        productId: dto.productId ?? null,
        productName: dto.productName ?? 'Sản phẩm',
        productImage: dto.productImage ?? '',
        variantLabel: dto.variantLabel ?? '',
        unitPrice: Number(dto.unitPrice ?? dto.price ?? 0),
        totalPrice: Number(dto.totalPrice ?? (Number(dto.unitPrice ?? dto.price ?? 0) * Number(dto.quantity ?? 0))),
        quantity: Number(dto.quantity ?? 1),
        stock: Number(dto.stock ?? 0)
      }
    },

    reset() {
      this.items = []
    },

    async fetchCart() {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        this.reset()
        return
      }

      this.loading = true
      try {
        const { data } = await axios.get('/api/cart')
        this.items = data.map(item => this.mapItem(item))
      } catch (error) {
        console.error('Fetch cart error:', error)
        if (error.response?.status === 401) {
          this.reset()
        }
      } finally {
        this.loading = false
      }
    },

    async addItem(productOrVariant, quantity = 1) {
      const authStore = useAuthStore()
      const notificationStore = useNotificationStore()
      const toast = useToastStore()

      if (!authStore.isAuthenticated) {
        toast.warning('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!', {
          action: { label: 'Đăng nhập', to: '/login' }
        })
        return false
      }

      const variantId = productOrVariant?.variantId ?? productOrVariant?.id
      const name = productOrVariant?.productName ?? productOrVariant?.name ?? 'Sản phẩm'

      if (!variantId) {
        toast.error('Không tìm thấy biến thể sản phẩm để thêm vào giỏ hàng!')
        return false
      }

      try {
        await axios.post('/api/cart', { variantId, quantity })
        await this.fetchCart()
        toast.success(`Đã thêm "${name}" vào giỏ hàng!`, {
          action: { label: 'Xem giỏ hàng', to: '/cart' }
        })
        notificationStore.pushCartAdded({ productName: name, quantity })
        return true
      } catch (error) {
        toast.error(error.response?.data?.message || 'Không thể thêm vào giỏ hàng!')
        return false
      }
    },

    async removeItem(variantId) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) return

      try {
        await axios.delete(`/api/cart/${variantId}`)
        this.items = this.items.filter(item => item.variantId !== variantId && item.id !== variantId)
      } catch (error) {
        console.error('Remove cart item error:', error)
      }
    },

    async updateQuantity(variantId, quantity) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) return

      if (quantity < 1) return

      try {
        const { data } = await axios.put(`/api/cart/${variantId}`, { quantity })
        const item = this.items.find(i => i.variantId === variantId || i.id === variantId)
        if (item) {
          item.quantity = data.quantity
          item.totalPrice = Number(item.unitPrice || 0) * Number(data.quantity || quantity)
        }
      } catch (error) {
        console.error('Update cart quantity error:', error)
        await this.fetchCart()
      }
    },

    async clearCart() {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        this.reset()
        return
      }

      try {
        await axios.delete('/api/cart')
      } catch (error) {
        console.error('Clear cart error:', error)
      } finally {
        this.reset()
      }
    }
  }
})
