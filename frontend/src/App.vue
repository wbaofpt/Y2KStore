<template>
  <div class="app-shell">
    <Transition name="page-loader">
      <div v-if="isPageLoading" class="page-loader" aria-live="polite" aria-label="Đang tải trang">
        <div class="page-loader__mark">Y2K<span></span></div>
        <div class="page-loader__bar"><span></span></div>
        <small>Đang chuẩn bị outfit...</small>
      </div>
    </Transition>
    <template v-if="!isAdminRoute">
      <header class="site-header">
        <div class="y2k-page-container site-header__row">
          <router-link to="/" class="site-header__brand" aria-label="Y2K Store home">
            <Y2kLogo />
          </router-link>

          <form class="header-search" @submit.prevent="handleSearch">
            <i class="bi bi-search"></i>
            <input v-model="searchQuery" type="text" placeholder="Tìm áo, quần, phụ kiện...">
          </form>

          <div class="header-actions">
            <button type="button" class="header-action" @click="handleAccountClick" ref="accountTrigger">
              <span class="header-avatar">
                <img v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" :alt="accountLabel" class="header-avatar__image">
                <span v-else class="header-avatar__fallback">{{ accountInitials }}</span>
              </span>
              <span>{{ accountLabel }}</span>
            </button>
            <button class="header-action header-action--notify" @click="toggleNotifications" ref="notificationTrigger">
              <i class="bi bi-bell"></i>
              <span>Thông báo</span>
              <span v-if="notificationBadgeCount" class="cart-badge">{{ notificationBadgeCount }}</span>
            </button>
            <router-link to="/cart" class="header-action header-action--cart">
              <i class="bi bi-bag"></i>
              <span>Giỏ hàng</span>
              <span v-if="cartStore.totalCount" class="cart-badge">{{ cartStore.totalCount }}</span>
            </router-link>
          </div>
        </div>
      </header>

      <nav class="top-nav" :class="{ 'is-category-open': categoryMenuHovered }">
        <div class="y2k-page-container top-nav__inner">
          <router-link to="/" class="top-nav__link" :class="{ active: route.path === '/' }">Trang chủ</router-link>
          <div class="top-nav__dropdown" :class="{ 'is-closed': categoryMenuClosed }" @mouseenter="openCategoryMenu" @mouseleave="categoryMenuHovered = false">
            <button class="top-nav__link top-nav__dropdown-trigger" :class="{ active: route.path.startsWith('/shop') && (route.query.category || route.query.categoryName) }" type="button">
              Danh mục <i class="bi bi-chevron-down"></i>
            </button>
            <div class="top-nav__dropdown-panel" @click="closeCategoryMenu">
              <div class="top-nav__dropdown-heading"><span>Shop by category</span><router-link to="/shop">Xem tất cả</router-link></div>
              <div class="top-nav__category-grid">
                <router-link to="/shop?categoryName=áo" class="top-nav__category-link"><i class="bi bi-stars"></i><span>Áo</span><small>Basic · Y2K</small></router-link>
                <router-link to="/shop?categoryName=quần" class="top-nav__category-link"><i class="bi bi-bounding-box"></i><span>Quần</span><small>Denim · Cargo</small></router-link>
                <router-link to="/shop?categoryName=váy%20%2F%20đầm" class="top-nav__category-link"><i class="bi bi-flower1"></i><span>Váy / Đầm</span><small>Soft · Party</small></router-link>
                <router-link to="/shop?categoryName=phụ%20kiện" class="top-nav__category-link"><i class="bi bi-gem"></i><span>Phụ kiện</span><small>Mix & match</small></router-link>
                <router-link to="/shop?categoryName=giày" class="top-nav__category-link"><i class="bi bi-heart"></i><span>Giày</span><small>Sneaker · Boots</small></router-link>
                <router-link to="/shop?categoryName=túi%20xách" class="top-nav__category-link"><i class="bi bi-handbag"></i><span>Túi xách</span><small>Mini · Shoulder</small></router-link>
                <router-link to="/shop?categoryName=trang%20sức" class="top-nav__category-link"><i class="bi bi-brightness-high"></i><span>Trang sức</span><small>Shine your way</small></router-link>
                <router-link to="/shop?categoryName=áo%20khoác" class="top-nav__category-link"><i class="bi bi-cloud"></i><span>Áo khoác</span><small>Layer · Outerwear</small></router-link>
              </div>
            </div>
          </div>
          <router-link to="/shop" class="top-nav__link" :class="{ active: route.path.startsWith('/shop') && !route.query.category && !route.query.categoryName && !route.query.sale && !route.query.sort }">Shop</router-link>
          <router-link to="/new-arrivals" class="top-nav__link" :class="{ active: route.path === '/new-arrivals' }">Hàng mới</router-link>
          <router-link to="/about" class="top-nav__link" :class="{ active: route.path.startsWith('/about') }">About Us</router-link>
          <router-link to="/chinh-sach-khuyen-mai" class="top-nav__link top-nav__link" :class="{ active: route.path.startsWith('/chinh-sach-khuyen-mai') }">Khuyến Mãi</router-link>
          <router-link v-if="authStore.isAuthenticated" to="/orders" class="top-nav__link" :class="{ active: route.path.startsWith('/orders') }">Đơn hàng</router-link>
        </div>
      </nav>
    </template>

    <main class="site-main">
      <router-view v-slot="{ Component, route: currentRoute }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="currentRoute.path" />
        </Transition>
      </router-view>
    </main>

    <template v-if="!isAdminRoute">
      <footer class="site-footer">
        <div class="y2k-page-container site-footer__grid">
          <div>
            <Y2kLogo compact />
            <p class="site-footer__copy">
              Storefront Y2K được làm lại gọn hơn, dễ mua hơn và vẫn dùng dữ liệu backend hiện tại.
            </p>
          </div>

          <div>
            <h6 class="site-footer__title">Mua sắm</h6>
            <div class="site-footer__links">
              <router-link to="/shop">Tất cả sản phẩm</router-link>
              <router-link to="/new-arrivals">Hàng mới</router-link>
              <router-link to="/shop?sale=true">Sale</router-link>
            </div>
          </div>

          <div>
            <h6 class="site-footer__title">Tài khoản</h6>
            <div class="site-footer__links">
              <router-link to="/account">Hồ sơ</router-link>
              <router-link to="/orders">Lịch sử đơn hàng</router-link>
              <router-link to="/checkout">Thanh toán</router-link>
              <router-link v-if="authStore.isAdmin" to="/admin">Quản trị</router-link>
            </div>
          </div>

          <div>
            <h6 class="site-footer__title">Hỗ trợ</h6>
            <div class="site-footer__links">
              <span>Email: support@y2kstore.vn</span>
              <span>Hotline: 0123 456 789</span>
              <span>Thanh toán: COD và MB Bank</span>
              <router-link to="/chinh-sach-khuyen-mai">Chính sách khuyến mãi</router-link>
            </div>
          </div>
        </div>
      </footer>
    </template>

    <Teleport to="body">
      <Transition name="float-panel">
        <div
          v-if="showUserMenu"
          class="user-menu-float"
          :style="userMenuStyle"
          @click.stop
          ref="userMenuPanel"
        >
          <div class="user-menu-float__head">
            <strong>{{ authStore.user?.fullName || 'Khách' }}</strong>
            <span>{{ authStore.user?.email || 'Đăng nhập để tiếp tục' }}</span>
          </div>
          <template v-if="authStore.isAuthenticated">
            <router-link to="/account" class="user-menu-float__link" @click="closeUserMenu">Trang tài khoản</router-link>
            <router-link to="/orders" class="user-menu-float__link" @click="closeUserMenu">Lịch sử đơn hàng</router-link>
            <router-link v-if="authStore.isAdmin" to="/admin" class="user-menu-float__link" @click="closeUserMenu">Trang quản trị</router-link>
            <button class="user-menu-float__link user-menu-float__link--button" @click="logout">Đăng xuất</button>
          </template>
          <template v-else>
            <router-link to="/login" class="user-menu-float__link" @click="closeUserMenu">Đăng nhập</router-link>
            <router-link to="/register" class="user-menu-float__link" @click="closeUserMenu">Tạo tài khoản</router-link>
          </template>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="float-panel">
        <div
          v-if="showNotifications"
          class="notification-float"
          :style="notificationMenuStyle"
          @click.stop
          ref="notificationPanel"
        >
          <div class="notification-float__head">
            <div>
              <strong>Thông báo</strong>
              <span>{{ authStore.isAuthenticated ? 'Cập nhật tài khoản & đơn hàng' : 'Cần đăng nhập để theo dõi đơn' }}</span>
            </div>
            <div class="notification-float__head-actions">
              <button
                v-if="authStore.isAuthenticated"
                class="notification-float__mark-read"
                type="button"
                :disabled="!unreadNotificationCount"
                @click="markAllNotificationsRead"
              >
                <i class="bi bi-check2-all"></i>
                Đánh dấu đã đọc
              </button>
              <button class="notification-float__close" type="button" aria-label="Đóng thông báo" @click="closeNotifications">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
          </div>

          <div class="notification-float__body">
            <div v-if="!authStore.isAuthenticated" class="notification-item notification-item--empty">
              <i class="bi bi-person-lock"></i>
              <div>
                <strong>Bạn chưa đăng nhập</strong>
                <p>Đăng nhập để xem trạng thái đơn, địa chỉ và thông báo cá nhân.</p>
              </div>
            </div>

            <template v-else>
              <div
                v-for="item in notificationItems"
                :key="item.readKey || item.key"
                role="button"
                tabindex="0"
                class="notification-item"
                :class="[`notification-item--${item.tone}`, { 'notification-item--read': item.isRead }]"
                @click="markNotificationRead(item)"
                @keydown.enter="markNotificationRead(item)"
              >
                <i :class="item.icon"></i>
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.description }}</p>
                </div>
                <span v-if="item.count" class="notification-item__count">{{ item.count }}</span>
                <i v-if="!item.isRead" class="notification-item__unread-dot bi bi-circle-fill" aria-label="Chưa đọc"></i>
              </div>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>

    <ToastContainer />
    <ConfirmModal />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import { useNotificationStore } from './stores/notifications'
import Y2kLogo from './components/Y2kLogo.vue'
import ToastContainer from './components/ToastContainer.vue'
import ConfirmModal from './components/ConfirmModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const notificationStore = useNotificationStore()

const searchQuery = ref(route.query.search || '')
const showUserMenu = ref(false)
const showNotifications = ref(false)
const accountTrigger = ref(null)
const notificationTrigger = ref(null)
const userMenuPanel = ref(null)
const notificationPanel = ref(null)
const userMenuStyle = ref({ top: '72px', left: '0px' })
const notificationMenuStyle = ref({ top: '72px', left: '0px' })
const notificationItems = computed(() => notificationStore.items)
const categoryMenuClosed = ref(false)
const categoryMenuHovered = ref(false)
const isPageLoading = ref(true)
const loadingDelayMs = ref(450)
let notificationPollTimer

const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const accountLabel = computed(() => {
  if (!authStore.isAuthenticated) return 'Đăng nhập'
  return authStore.user?.fullName || authStore.user?.email || 'Tài khoản'
})
const accountInitials = computed(() => {
  const label = authStore.user?.fullName || authStore.user?.email || 'Y2K'
  return label.split(/\s+/).filter(Boolean).slice(-2).map((part) => part[0]).join('').toUpperCase()
})
const unreadNotificationCount = computed(() => notificationStore.unreadCount)
const notificationBadgeCount = computed(() => unreadNotificationCount.value)

watch(() => route.query.search, (value) => {
  searchQuery.value = value || ''
})

watch(() => route.fullPath, (nextPath, previousPath) => {
  closeUserMenu()
  closeNotifications()
  const isShopFilterNavigation = nextPath.startsWith('/shop') && previousPath?.startsWith('/shop')
  if (!isShopFilterNavigation) showPageLoader()
})

watch(() => authStore.token, async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile()
    await cartStore.fetchCart()
  } else {
    cartStore.reset()
  }
  await loadNotifications()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('click', handleOutsideClick)
  window.addEventListener('scroll', closeFloatingPanels, { passive: true })
  loadPageConfig()
  showPageLoader()
  notificationPollTimer = setInterval(() => {
    if (authStore.isAuthenticated) loadNotifications()
  }, 60000)
})

async function loadPageConfig() {
  try {
    const { data } = await axios.get('/api/config')
    loadingDelayMs.value = Math.max(0, Number(data?.pageLoadingDelayMs ?? 450))
  } catch {
    loadingDelayMs.value = 450
  }
}

let loaderTimer
function showPageLoader() {
  isPageLoading.value = true
  clearTimeout(loaderTimer)
  loaderTimer = setTimeout(() => { isPageLoading.value = false }, loadingDelayMs.value)
}

onUnmounted(() => {
  window.removeEventListener('click', handleOutsideClick)
  window.removeEventListener('scroll', closeFloatingPanels)
  clearInterval(notificationPollTimer)
})

function handleSearch() {
  const query = searchQuery.value.trim()
  router.push(query ? `/shop?search=${encodeURIComponent(query)}` : '/shop')
}

function handleAccountClick(event) {
  event.stopPropagation()
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  showNotifications.value = false
  showUserMenu.value = !showUserMenu.value
  if (showUserMenu.value) {
    positionPanel(accountTrigger.value, userMenuStyle)
  }
}

function toggleNotifications(event) {
  event.stopPropagation()
  showUserMenu.value = false
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    positionPanel(notificationTrigger.value, notificationMenuStyle)
  }
}

function positionPanel(triggerRef, styleRef) {
  const rect = triggerRef?.getBoundingClientRect()
  if (!rect) return

  const menuWidth = styleRef === notificationMenuStyle ? 420 : 240
  const left = Math.min(Math.max(16, rect.right - menuWidth), window.innerWidth - menuWidth - 16)
  styleRef.value = {
    top: `${rect.bottom + 12}px`,
    left: `${left}px`
  }
}

function handleOutsideClick(event) {
  const target = event.target
  if (showUserMenu.value) {
    if (accountTrigger.value?.contains(target) || userMenuPanel.value?.contains(target)) return
    closeUserMenu()
  }
  if (showNotifications.value) {
    if (notificationTrigger.value?.contains(target) || notificationPanel.value?.contains(target)) return
    closeNotifications()
  }
}

function closeUserMenu() {
  showUserMenu.value = false
}

function closeNotifications() {
  showNotifications.value = false
}

function closeCategoryMenu() {
  categoryMenuClosed.value = true
}

function openCategoryMenu() {
  categoryMenuHovered.value = true
  categoryMenuClosed.value = false
}

function closeFloatingPanels() {
  closeUserMenu()
  closeNotifications()
}

async function loadNotifications() {
  if (!authStore.isAuthenticated) {
    notificationStore.syncOrderNotifications({ authenticated: false, user: null, orders: [] })
    return
  }

  try {
    const { data } = await axios.get('/api/orders')
    const orders = Array.isArray(data) ? data : []
    notificationStore.syncOrderNotifications({ authenticated: true, user: authStore.user, orders })
  } catch {
    notificationStore.syncOrderNotifications({ authenticated: true, user: authStore.user, orders: [] })
  }
}

function markNotificationRead(item) {
  notificationStore.markRead(item)
}

function markAllNotificationsRead() {
  notificationStore.markAllRead()
}

function logout() {
  authStore.logout()
  cartStore.reset()
  notificationStore.items = []
  closeUserMenu()
  closeNotifications()
  router.push('/login')
}
</script>
