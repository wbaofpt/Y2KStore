<template>
  <div class="auth-redesign-page">
    <div class="y2k-page-container auth-redesign">
      <section class="auth-redesign__panel">
        <router-link to="/" class="auth-back-link">
          <i class="bi bi-arrow-left"></i>
          Về trang chủ
        </router-link>

        <div class="auth-heading-block">
          <h1>Đăng nhập để tiếp tục mua sắm</h1>
          <p>Quản lý giỏ hàng, theo dõi đơn và lưu địa chỉ giao hàng của bạn trong một nơi.</p>
        </div>

        <div v-if="errorMsg" class="auth-alert auth-alert--danger">
          <i class="bi bi-exclamation-triangle"></i>
          <span>{{ errorMsg }}</span>
        </div>

        <form class="auth-modern-form" @submit.prevent="handleLogin">
          <label class="auth-field">
            <span>Email</span>
            <div class="auth-input-wrap">
              <i class="bi bi-envelope"></i>
              <input
                v-model.trim="email"
                type="email"
                placeholder="user@gmail.com"
                autocomplete="email"
                required
              >
            </div>
          </label>

          <label class="auth-field">
            <span>Mật khẩu</span>
            <div class="auth-input-wrap">
              <i class="bi bi-lock"></i>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Nhập mật khẩu"
                autocomplete="current-password"
                required
              >
              <button type="button" class="auth-password-toggle" @click="showPassword = !showPassword">
                <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
              </button>
            </div>
          </label>

          <div class="auth-form-row">
            <label class="auth-check">
              <input type="checkbox">
              <span>Ghi nhớ đăng nhập</span>
            </label>
            <router-link to="/register">Tạo tài khoản</router-link>
          </div>

          <button class="btn btn-y2k-primary auth-submit" :disabled="submitting">
            <span v-if="submitting" class="spinner-border spinner-border-sm"></span>
            <span>{{ submitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}</span>
            <i class="bi bi-arrow-right"></i>
          </button>
          <router-link to="/forgot-password" class="auth-forgot-link">Quên mật khẩu?</router-link>
        </form>


      </section>

      <section class="auth-showcase">
        <div class="auth-showcase__media">
          <img src="/y2k_banner.png" alt="Y2K Store">
        </div>
        <div class="auth-showcase__content">
          <span>Y2K Store</span>
          <h2>Item mới, đơn hàng rõ ràng, checkout mượt hơn.</h2>
          <div class="auth-benefit-grid">
            <div>
              <i class="bi bi-bag-check"></i>
              <strong>Lưu giỏ hàng</strong>
            </div>
            <div>
              <i class="bi bi-truck"></i>
              <strong>Theo dõi đơn</strong>
            </div>
            <div>
              <i class="bi bi-geo-alt"></i>
              <strong>Lưu địa chỉ</strong>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const submitting = ref(false)
const errorMsg = ref('')
async function handleLogin() {
  submitting.value = true
  errorMsg.value = ''
  const result = await authStore.login(email.value, password.value)
  submitting.value = false

  if (!result.success) {
    errorMsg.value = result.message
    return
  }

  await Promise.all([authStore.fetchProfile(), cartStore.fetchCart()])
  router.push(authStore.isAdmin ? '/admin' : '/account')
}

</script>
