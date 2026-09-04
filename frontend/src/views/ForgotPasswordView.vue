<template>
  <div class="auth-redesign-page auth-redesign-page--recovery">
    <div class="y2k-page-container auth-redesign auth-redesign--recovery">
      <section class="auth-redesign__panel">
        <router-link to="/login" class="auth-back-link">
          <i class="bi bi-arrow-left"></i>
          Quay lại đăng nhập
        </router-link>

        <div class="auth-heading-block">
          <span class="section-heading__eyebrow">Account recovery</span>
          <h1>{{ mode === 'reset' ? 'Đặt lại mật khẩu' : 'Lấy lại quyền truy cập' }}</h1>
          <p v-if="mode === 'reset'">Tạo mật khẩu mới tối thiểu 6 ký tự cho tài khoản của bạn.</p>
          <p v-else>Nhập email đã đăng ký. Hệ thống sẽ gửi mã xác nhận 6 số để bạn đặt lại mật khẩu.</p>
        </div>

        <div v-if="message" class="auth-alert" :class="success ? 'auth-alert--success' : 'auth-alert--danger'">
          <i :class="success ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
          <span>{{ message }}</span>
        </div>

        <form v-if="mode === 'request'" class="auth-modern-form" @submit.prevent="requestReset">
          <label class="auth-field">
            <span>Email</span>
            <div class="auth-input-wrap">
              <i class="bi bi-envelope"></i>
              <input v-model.trim="email" type="email" autocomplete="email" placeholder="you@example.com" required>
            </div>
          </label>
          <button class="btn btn-y2k-primary auth-submit" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>{{ loading ? 'Đang gửi mã...' : 'Gửi mã xác nhận' }}</span>
            <i class="bi bi-arrow-right"></i>
          </button>
        </form>

        <form v-else class="auth-modern-form" @submit.prevent="resetPassword">
          <label class="auth-field">
            <span>Mã đặt lại</span>
            <div class="auth-input-wrap">
              <i class="bi bi-key"></i>
              <input v-model.trim="token" inputmode="numeric" maxlength="6" pattern="[0-9]{6}" autocomplete="one-time-code" placeholder="Nhập 6 chữ số" required>
            </div>
          </label>
          <label class="auth-field">
            <span>Mật khẩu mới</span>
            <div class="auth-input-wrap">
              <i class="bi bi-lock"></i>
              <input v-model="password" type="password" minlength="6" autocomplete="new-password" required>
            </div>
          </label>
          <button class="btn btn-y2k-primary auth-submit" :disabled="loading || token.length !== 6">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>{{ loading ? 'Đang cập nhật...' : 'Đặt lại mật khẩu' }}</span>
            <i class="bi bi-check2"></i>
          </button>
        </form>

        <router-link to="/register" class="auth-secondary-link">Chưa có tài khoản? Tạo tài khoản</router-link>
      </section>

      <section class="auth-showcase auth-showcase--recovery">
        <div class="auth-showcase__media">
          <img src="/summer_y2k.png" alt="Y2K Store recovery">
        </div>
        <div class="auth-showcase__content">
          <span>Y2K Store</span>
          <h2>Giữ trọn outfit, lịch sử đơn và địa chỉ giao hàng trong một tài khoản.</h2>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const mode = ref(route.query.token ? 'reset' : 'request')
const email = ref('')
const token = ref(route.query.token || '')
const password = ref('')
const loading = ref(false)
const success = ref(false)
const message = ref('')

async function requestReset() {
  loading.value = true
  message.value = ''
  try {
    const data = await authStore.requestPasswordReset(email.value)
    token.value = ''
    mode.value = 'reset'
    success.value = true
    message.value = data.message || 'Mã xác nhận đã được gửi đến email của bạn.'
  } catch (error) {
    success.value = false
    message.value = error.response?.data?.message || 'Không thể gửi mã đặt lại mật khẩu.'
  } finally {
    loading.value = false
  }
}

async function resetPassword() {
  loading.value = true
  message.value = ''
  try {
    await authStore.resetPassword(token.value, password.value)
    success.value = true
    message.value = 'Đặt lại mật khẩu thành công. Đang quay lại đăng nhập...'
    setTimeout(() => router.push('/login'), 900)
  } catch (error) {
    success.value = false
    message.value = error.response?.data?.message || 'Không thể đặt lại mật khẩu.'
  } finally {
    loading.value = false
  }
}
</script>
