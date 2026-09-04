import { defineStore } from 'pinia'

function storageKeyFor(user) {
  return `y2k_notifications_${user?.id || user?.email || 'guest'}`
}

function safeRead(storageKey) {
  try {
    const raw = localStorage.getItem(storageKey)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function safeWrite(storageKey, items) {
  localStorage.setItem(storageKey, JSON.stringify(items))
}

function nowIso() {
  return new Date().toISOString()
}

function uniqueKey(prefix) {
  return `${prefix}:${Date.now().toString(36)}:${Math.random().toString(36).slice(2, 8)}`
}

function normalize(item) {
  return {
    id: item.id || uniqueKey(item.key || 'notification'),
    key: item.key || item.readKey || uniqueKey('notification'),
    readKey: item.readKey || item.key || uniqueKey('notification-read'),
    source: item.source || 'custom',
    icon: item.icon || 'bi bi-bell',
    title: item.title || 'Thông báo',
    description: item.description || '',
    tone: item.tone || 'info',
    count: Number(item.count || 0),
    isRead: Boolean(item.isRead),
    createdAt: item.createdAt || nowIso(),
    action: item.action || null
  }
}

function buildLoginItem() {
  return {
    key: 'login',
    readKey: 'login',
    source: 'system',
    icon: 'bi bi-person-lock',
    title: 'Đăng nhập để theo dõi đơn hàng',
    description: 'Bạn sẽ xem được trạng thái đơn hàng, địa chỉ và thông báo cá nhân ở đây.',
    tone: 'info',
    count: 0,
    isRead: true,
    createdAt: nowIso()
  }
}

function buildWelcomeItem(user) {
  return {
    key: 'welcome',
    readKey: `welcome:${user?.id || user?.email || 'guest'}`,
    source: 'system',
    icon: 'bi bi-person-check',
    title: `Xin chào, ${user?.fullName || 'bạn'}`,
    description: 'Tài khoản của bạn đã được đồng bộ.',
    tone: 'success',
    count: 1,
    isRead: false,
    createdAt: nowIso()
  }
}

function buildOrderBuckets(orders = []) {
  const list = Array.isArray(orders) ? orders : []
  const byStatus = (status) => list.filter((order) => order?.status === status).length
  const unpaid = list.filter((order) => (order?.paymentStatus || 'UNPAID') !== 'PAID' && order?.status !== 'CANCELLED').length

  return [
    {
      key: 'orders-processing',
      readKey: 'orders-processing',
      source: 'system',
      icon: 'bi bi-box-seam',
      title: 'Đơn đang xử lý',
      description: `${byStatus('PROCESSING')} đơn đang chờ shop xác nhận.`,
      tone: 'primary',
      count: byStatus('PROCESSING'),
      isRead: false,
      createdAt: nowIso()
    },
    {
      key: 'orders-preparing',
      readKey: 'orders-preparing',
      source: 'system',
      icon: 'bi bi-box',
      title: 'Đơn đang chuẩn bị',
      description: `${byStatus('PREPARING')} đơn đang được đóng gói.`,
      tone: 'primary',
      count: byStatus('PREPARING'),
      isRead: false,
      createdAt: nowIso()
    },
    {
      key: 'orders-shipping',
      readKey: 'orders-shipping',
      source: 'system',
      icon: 'bi bi-truck',
      title: 'Đơn đang vận chuyển',
      description: `${byStatus('SHIPPED')} đơn đang trên đường giao.`,
      tone: 'success',
      count: byStatus('SHIPPED'),
      isRead: false,
      createdAt: nowIso()
    },
    {
      key: 'orders-payment',
      readKey: 'orders-payment',
      source: 'system',
      icon: 'bi bi-credit-card',
      title: 'Đơn chờ thanh toán',
      description: `${unpaid} đơn chưa được thanh toán hoàn tất.`,
      tone: 'danger',
      count: unpaid,
      isRead: false,
      createdAt: nowIso()
    },
    {
      key: 'orders-delivered',
      readKey: 'orders-delivered',
      source: 'system',
      icon: 'bi bi-check-circle',
      title: 'Đơn đã giao',
      description: `${byStatus('DELIVERED')} đơn đã giao thành công.`,
      tone: 'success',
      count: byStatus('DELIVERED'),
      isRead: false,
      createdAt: nowIso()
    },
    {
      key: 'orders-cancelled',
      readKey: 'orders-cancelled',
      source: 'system',
      icon: 'bi bi-x-circle',
      title: 'Đơn đã hủy',
      description: `${byStatus('CANCELLED')} đơn đã bị hủy.`,
      tone: 'danger',
      count: byStatus('CANCELLED'),
      isRead: false,
      createdAt: nowIso()
    }
  ].filter((item) => item.count > 0)
}

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: [],
    storageKey: storageKeyFor(null)
  }),
  getters: {
    unreadCount: (state) => state.items.filter((item) => !item.isRead).length
  },
  actions: {
    syncOrderNotifications({ authenticated, user, orders = [] }) {
      this.storageKey = storageKeyFor(user)
      const persisted = safeRead(this.storageKey).map(normalize)
      const customItems = persisted.filter((item) => item.source !== 'system')
      const readMap = new Map(persisted.map((item) => [item.readKey || item.key, item.isRead]))
      const systemItems = authenticated
        ? [buildWelcomeItem(user), ...buildOrderBuckets(orders)]
        : [buildLoginItem()]

      this.items = [
        ...systemItems.map((item) => ({ ...item, isRead: readMap.get(item.readKey || item.key) ?? item.isRead })),
        ...customItems
      ]
        .map(normalize)
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

      this.persist()
    },

    push(item) {
      const normalized = normalize({
        ...item,
        isRead: false,
        createdAt: item.createdAt || nowIso()
      })
      this.items = [normalized, ...this.items.filter((existing) => (existing.readKey || existing.key) !== normalized.readKey)]
      this.persist()
      return normalized
    },

    pushCartAdded({ productName, quantity = 1 }) {
      return this.push({
        key: uniqueKey('cart-added'),
        readKey: uniqueKey('cart-added-read'),
        icon: 'bi bi-bag-plus',
        title: `Đã thêm ${productName || 'sản phẩm'} vào giỏ`,
        description: quantity > 1
          ? `Số lượng: ${quantity}.`
          : 'Sản phẩm đã được thêm vào giỏ của bạn.',
        tone: 'primary',
        count: quantity,
        action: { label: 'Xem giỏ hàng', to: '/cart' }
      })
    },

    pushOrderPlaced({ orderId, paymentMethod, paymentStatus, totalAmount }) {
      return this.push({
        key: `order-placed-${orderId}`,
        readKey: `order-placed-${orderId}`,
        icon: 'bi bi-receipt',
        title: `Đơn hàng #${orderId} đã được tạo`,
        description: `Thanh toán: ${paymentMethod || 'COD'} · Trạng thái: ${paymentStatus || 'PENDING'} · Tổng: ${totalAmount || ''}`,
        tone: 'success',
        count: 1,
        action: { label: 'Xem đơn hàng', to: '/orders' }
      })
    },

    pushPaymentConfirmed({ orderId, totalAmount }) {
      return this.push({
        key: `payment-confirmed-${orderId}`,
        readKey: `payment-confirmed-${orderId}`,
        icon: 'bi bi-credit-card-check',
        title: `Thanh toán thành công cho đơn #${orderId}`,
        description: `Đơn hàng đã được xác nhận thanh toán. Tổng giá trị: ${totalAmount || ''}.`,
        tone: 'success',
        count: 1,
        action: { label: 'Xem đơn hàng', to: '/orders' }
      })
    },

    markRead(item) {
      const target = this.items.find((existing) => (existing.readKey || existing.key) === (item.readKey || item.key))
      if (!target || target.isRead) return
      target.isRead = true
      this.persist()
    },

    markAllRead() {
      this.items.forEach((item) => {
        item.isRead = true
      })
      this.persist()
    },

    persist() {
      safeWrite(this.storageKey, this.items.map(normalize))
    }
  }
})
