<template>
  <aside class="admin-sidebar-panel">
    <div class="admin-sidebar-header">
      <div class="admin-sidebar-brand">
        <Y2kLogo compact />
        <div>
          <strong>Admin</strong>
          <span>Control room</span>
        </div>
      </div>
      <div class="admin-user-chip">
        <i class="bi bi-person-badge"></i>
        <div>
          <strong>{{ authStore.user?.fullName || 'Admin' }}</strong>
          <span>Quản trị viên</span>
        </div>
      </div>
    </div>

    <nav class="admin-nav">
      <button
        v-for="item in navItems"
        :key="item.key"
        class="admin-nav-link"
        :class="{ active: modelValue === item.key }"
        @click="$emit('update:modelValue', item.key)"
      >
        <i :class="item.icon"></i>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="admin-sidebar-footer">
      <router-link to="/" class="admin-nav-link admin-nav-link--muted">
        <i class="bi bi-house"></i><span>Về cửa hàng</span>
      </router-link>
      <button class="admin-nav-link admin-nav-link--muted w-100" @click="logout">
        <i class="bi bi-box-arrow-right"></i><span>Đăng xuất</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import Y2kLogo from '../Y2kLogo.vue'

defineProps({
  modelValue: { type: String, required: true }
})
defineEmits(['update:modelValue'])

const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { key: 'overview', label: 'Tổng quan', icon: 'bi bi-speedometer2' },
  { key: 'orders', label: 'Đơn hàng', icon: 'bi bi-cart3' },
  { key: 'products', label: 'Sản phẩm', icon: 'bi bi-bag' },
  { key: 'categories', label: 'Danh mục', icon: 'bi bi-grid' },
  { key: 'banners', label: 'Banner', icon: 'bi bi-images' },
  { key: 'promotions', label: 'Khuyến mãi', icon: 'bi bi-megaphone' },
  { key: 'coupons', label: 'Mã giảm giá', icon: 'bi bi-ticket-perforated' },
  { key: 'reviews', label: 'Đánh giá', icon: 'bi bi-chat-square-text' },
  { key: 'addresses', label: 'Địa chỉ', icon: 'bi bi-geo-alt' },
  { key: 'payments', label: 'Thanh toán', icon: 'bi bi-credit-card' },
  { key: 'users', label: 'Người dùng', icon: 'bi bi-people' },
  { key: 'database', label: 'Cấu trúc DB', icon: 'bi bi-diagram-3' }
]

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
