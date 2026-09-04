<template>
  <div class="auth-redesign-page">
    <div class="y2k-page-container auth-redesign auth-redesign--register">
      <section class="auth-showcase auth-showcase--register">
        <div class="auth-showcase__media">
          <img src="/summer_y2k.png" alt="Summer Y2K">
        </div>
        <div class="auth-showcase__content">
          <span>Thành viên mới</span>
          <h2>Tạo tài khoản để săn item, lưu địa chỉ và theo dõi đơn nhanh hơn.</h2>
          <div class="auth-benefit-list">
            <div><i class="bi bi-check2-circle"></i> Giỏ hàng đồng bộ theo tài khoản</div>
            <div><i class="bi bi-check2-circle"></i> Quản lý trạng thái đơn hàng</div>
            <div><i class="bi bi-check2-circle"></i> Lưu số điện thoại và địa chỉ nhận hàng</div>
          </div>
        </div>
      </section>

      <section class="auth-redesign__panel">
        <router-link to="/login" class="auth-back-link">
          <i class="bi bi-arrow-left"></i>
          Đã có tài khoản
        </router-link>

        <div class="auth-heading-block">
          <h1>Tạo tài khoản Y2K Store</h1>
          <p>Điền thông tin cơ bản để bắt đầu mua sắm và quản lý đơn hàng cá nhân.</p>
        </div>

        <div v-if="errorMsg" class="auth-alert auth-alert--danger">
          <i class="bi bi-exclamation-triangle"></i>
          <span>{{ errorMsg }}</span>
        </div>
        <div v-if="successMsg" class="auth-alert auth-alert--success">
          <i class="bi bi-check-circle"></i>
          <span>{{ successMsg }}</span>
        </div>

        <form class="auth-modern-form" @submit.prevent="handleRegister">
          <div class="auth-field-grid">
            <label class="auth-field">
              <span>Họ và tên</span>
              <div class="auth-input-wrap">
                <i class="bi bi-person"></i>
                <input v-model.trim="form.fullName" placeholder="Nguyễn Văn A" autocomplete="name" required>
              </div>
            </label>

            <label class="auth-field">
              <span>Số điện thoại</span>
              <div class="auth-input-wrap">
                <i class="bi bi-telephone"></i>
                <input v-model.trim="form.phone" placeholder="0909123456" autocomplete="tel" required>
              </div>
            </label>
          </div>

          <label class="auth-field">
            <span>Email</span>
            <div class="register-email-row">
              <div class="auth-input-wrap">
                <i class="bi bi-envelope"></i>
                <input v-model.trim="form.email" type="email" placeholder="you@example.com" autocomplete="email" required>
              </div>
              <button type="button" class="btn btn-y2k-outline register-email-send" :disabled="sendingCode || !form.email" @click="requestVerificationCode">
                <span v-if="sendingCode" class="spinner-border spinner-border-sm"></span>
                <i v-else class="bi bi-send"></i>
                <span>{{ sendingCode ? 'Đang gửi' : 'Gửi mã' }}</span>
              </button>
            </div>
          </label>

          <label v-if="verificationSent" class="auth-field register-verification-field">
            <span>Mã xác nhận email</span>
            <div class="auth-input-wrap">
              <i class="bi bi-shield-check"></i>
              <input v-model.trim="verificationCode" inputmode="numeric" maxlength="6" pattern="[0-9]{6}" autocomplete="one-time-code" placeholder="Nhập mã 6 số" required>
            </div>
            <small>Mã có hiệu lực trong 15 phút. Nhập đúng mã để hoàn tất đăng ký.</small>
          </label>

          <div class="auth-field-grid">
            <label class="auth-field">
              <span>Mật khẩu</span>
              <div class="auth-input-wrap">
                <i class="bi bi-lock"></i>
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  minlength="6"
                  placeholder="Tối thiểu 6 ký tự"
                  autocomplete="new-password"
                  required
                >
                <button type="button" class="auth-password-toggle" @click="showPassword = !showPassword">
                  <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                </button>
              </div>
            </label>

            <label class="auth-field">
              <span>Nhập lại mật khẩu</span>
              <div class="auth-input-wrap">
                <i class="bi bi-shield-lock"></i>
                <input
                  v-model="confirmPassword"
                  :type="showPassword ? 'text' : 'password'"
                  minlength="6"
                  placeholder="Nhập lại mật khẩu"
                  autocomplete="new-password"
                  required
                >
              </div>
            </label>
          </div>

          <label class="auth-check auth-check--terms">
            <input v-model="acceptedTerms" type="checkbox" required>
            <span>Tôi đồng ý tạo tài khoản và nhận thông tin đơn hàng từ Y2K Store.</span>
          </label>

          <button class="btn btn-y2k-primary auth-submit" :disabled="submitting || !verificationSent || verificationCode.length !== 6">
            <span v-if="submitting" class="spinner-border spinner-border-sm"></span>
            <span>{{ submitting ? 'Đang tạo tài khoản...' : 'Tạo tài khoản' }}</span>
            <i class="bi bi-arrow-right"></i>
          </button>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  fullName: '',
  phone: '',
  email: '',
  password: ''
})

const confirmPassword = ref('')
const acceptedTerms = ref(false)
const showPassword = ref(false)
const submitting = ref(false)
const sendingCode = ref(false)
const verificationSent = ref(false)
const verificationCode = ref('')
const errorMsg = ref('')
const successMsg = ref('')

watch(() => form.email, () => {
  if (verificationSent.value) {
    verificationSent.value = false
    verificationCode.value = ''
  }
})

async function requestVerificationCode() {
  errorMsg.value = ''
  successMsg.value = ''
  sendingCode.value = true
  try {
    const data = await authStore.requestRegistrationCode(form.email)
    verificationSent.value = true
    verificationCode.value = ''
    successMsg.value = data?.message || 'Mã xác nhận đã được gửi đến email của bạn.'
  } catch (error) {
    verificationSent.value = false
    errorMsg.value = error.response?.data?.message || 'Không thể gửi mã xác nhận email.'
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  errorMsg.value = ''
  successMsg.value = ''

  if (form.password !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu nhập lại chưa khớp.'
    return
  }

  if (!verificationSent.value || verificationCode.value.length !== 6) {
    errorMsg.value = 'Vui lòng gửi và nhập đúng mã xác nhận email trước khi tạo tài khoản.'
    return
  }

  submitting.value = true
  const result = await authStore.register({ ...form, verificationCode: verificationCode.value })
  submitting.value = false

  if (!result.success) {
    errorMsg.value = result.message
    return
  }

  successMsg.value = 'Tạo tài khoản thành công. Đang chuyển sang trang đăng nhập...'
  setTimeout(() => router.push('/login'), 900)
}
</script>
