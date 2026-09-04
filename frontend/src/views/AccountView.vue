<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useConfirmStore } from '../stores/confirm'
import { useToastStore } from '../stores/toast'
import { formatDate, formatPrice } from '../composables/useFormat'
import { productPath } from '../composables/useCatalog'

const authStore = useAuthStore()
const confirmStore = useConfirmStore()
const toast = useToastStore()

const profile = reactive({ fullName: '', email: '', phone: '', avatarUrl: '', emailVerified: false })
const emptyAddress = () => ({ id: null, province: '', district: '', ward: '', detail: '', isDefault: true })

const addressForm = reactive(emptyAddress())
const addresses = ref([])
const orders = ref([])
const orderItemsById = ref({})
const selectedOrderId = ref(null)
const activeOrderTab = ref('PROCESSING')
const loadingAccount = ref(false)
const loadingOrders = ref(false)
const loadingAddresses = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const sendingVerification = ref(false)
const verifyingEmail = ref(false)
const verificationSent = ref(false)
const emailVerificationCode = ref('')
const avatarInput = ref(null)
const continuingPaymentId = ref(null)
const continuedPayment = ref(null)

const orderStatusOptions = [
  { key: 'PROCESSING', label: 'Chờ xử lý', icon: 'bi bi-hourglass-split', className: 'account-pill--pending' },
  { key: 'PREPARING', label: 'Đang chuẩn bị', icon: 'bi bi-box-seam', className: 'account-pill--preparing' },
  { key: 'SHIPPED', label: 'Đang vận chuyển', icon: 'bi bi-truck', className: 'account-pill--shipping' },
  { key: 'DELIVERED', label: 'Đã giao', icon: 'bi bi-check-circle', className: 'account-pill--delivered' },
  { key: 'CANCELLED', label: 'Đã hủy', icon: 'bi bi-x-circle', className: 'account-pill--cancelled' }
]

const initials = computed(() => {
  const words = (profile.fullName || profile.email || 'Y2K').trim().split(/\s+/).filter(Boolean)
  return words.slice(-2).map((word) => word[0]).join('').toUpperCase()
})
const profileDisplayName = computed(() => profile.fullName?.trim()?.split(/\s+/).slice(-1)[0] || 'bạn')
const defaultAddress = computed(() => addresses.value.find((address) => address.isDefault) || addresses.value[0] || null)
const remainingAddressSlots = computed(() => Math.max(0, 4 - addresses.value.length))
const paidDeliveredOrders = computed(() => orders.value.filter((order) => order.status === 'DELIVERED' && order.paymentStatus === 'PAID'))
const reviewableProducts = computed(() => {
  const seen = new Set()
  return paidDeliveredOrders.value
    .flatMap((order) => (orderItemsById.value[order.id] || []).map((item) => ({ ...item, orderId: order.id, orderDate: order.orderDate })))
    .filter((item) => {
      const key = item.productId || item.productName
      if (!key || seen.has(key)) return false
      seen.add(key)
      return true
    })
})
const orderTabs = computed(() => [
  ...orderStatusOptions.map((status) => ({
    ...status,
    count: orders.value.filter((order) => order.status === status.key).length
  }))
])
const filteredOrders = computed(() => {
  return orders.value.filter((order) => order.status === activeOrderTab.value)
})
const selectedOrder = computed(() => orders.value.find((order) => order.id === selectedOrderId.value) || filteredOrders.value[0] || null)
const selectedOrderItems = computed(() => selectedOrder.value ? orderItemsById.value[selectedOrder.value.id] || [] : [])
const continuedPaymentQrImageUrl = computed(() => {
  const qrCode = continuedPayment.value?.qrCode || ''
  if (qrCode.startsWith('http://') || qrCode.startsWith('https://')) return qrCode
  const qrPayload = qrCode || continuedPayment.value?.paymentUrl || ''
  return qrPayload ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(qrPayload)}` : ''
})
const accountStats = computed(() => [
  { label: 'Tổng đơn', value: orders.value.length, hint: 'Đơn hàng trong tài khoản', icon: 'bi bi-receipt' },
  { label: 'Đang vận chuyển', value: orders.value.filter((order) => order.status === 'SHIPPED').length, hint: 'Đơn đang vận chuyển', icon: 'bi bi-truck' },
  { label: 'Có thể đánh giá', value: reviewableProducts.value.length, hint: 'Đã giao và đã thanh toán', icon: 'bi bi-star' },
  { label: 'Địa chỉ', value: `${addresses.value.length}/4`, hint: `${remainingAddressSlots.value} chỗ trống`, icon: 'bi bi-geo-alt' }
])

onMounted(refreshAccount)

async function refreshAccount() {
  loadingAccount.value = true
  try {
    await Promise.all([loadProfile(), fetchOrders(), fetchAddresses()])
  } finally {
    loadingAccount.value = false
  }
}

async function loadProfile() {
  const user = await authStore.fetchProfile()
  profile.fullName = user?.fullName || ''
  profile.email = user?.email || ''
  profile.phone = user?.phone || ''
  profile.avatarUrl = user?.avatarUrl || ''
  profile.emailVerified = user?.emailVerified === true
}

async function fetchOrders() {
  loadingOrders.value = true
  try {
    const { data } = await axios.get('/api/orders')
    orders.value = Array.isArray(data) ? data : []
    orderItemsById.value = {}
    await Promise.all(orders.value.map((order) => fetchOrderItems(order.id)))
    selectedOrderId.value = filteredOrders.value[0]?.id || orders.value[0]?.id || null
  } catch {
    orders.value = []
    orderItemsById.value = {}
  } finally {
    loadingOrders.value = false
  }
}

async function fetchOrderItems(orderId) {
  try {
    const { data } = await axios.get(`/api/orders/${orderId}/items`)
    orderItemsById.value = { ...orderItemsById.value, [orderId]: Array.isArray(data) ? data : [] }
  } catch {
    orderItemsById.value = { ...orderItemsById.value, [orderId]: [] }
  }
}

async function fetchAddresses() {
  loadingAddresses.value = true
  try {
    const { data } = await axios.get('/api/auth/addresses')
    addresses.value = Array.isArray(data) ? data : []
    if (!addressForm.id && defaultAddress.value) editAddress(defaultAddress.value)
  } catch {
    addresses.value = []
  } finally {
    loadingAddresses.value = false
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    const user = await authStore.updateProfile({ fullName: profile.fullName, phone: profile.phone, avatarUrl: profile.avatarUrl || null })
    profile.fullName = user?.fullName || profile.fullName
    profile.phone = user?.phone || profile.phone
    profile.avatarUrl = user?.avatarUrl || profile.avatarUrl
    profile.emailVerified = user?.emailVerified === true
    toast.success('Đã cập nhật thông tin tài khoản.')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể cập nhật tài khoản.')
  } finally {
    savingProfile.value = false
  }
}

async function requestEmailVerification() {
  sendingVerification.value = true
  try {
    const { data } = await axios.post('/api/auth/request-email-verification')
    verificationSent.value = true
    emailVerificationCode.value = ''
    toast.success(data?.message || 'Mã xác nhận đã được gửi đến email của bạn.')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể gửi mã xác nhận email.')
  } finally {
    sendingVerification.value = false
  }
}

async function verifyEmail() {
  if (emailVerificationCode.value.length !== 6) return
  verifyingEmail.value = true
  try {
    const { data } = await axios.post('/api/auth/verify-email', { code: emailVerificationCode.value })
    profile.emailVerified = data?.emailVerified === true
    authStore.user = { ...authStore.user, emailVerified: profile.emailVerified }
    authStore.persistSession()
    verificationSent.value = false
    emailVerificationCode.value = ''
    toast.success('Email của bạn đã được xác nhận.')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Mã xác nhận không đúng hoặc đã hết hạn.')
  } finally {
    verifyingEmail.value = false
  }
}

function openAvatarPicker() {
  avatarInput.value?.click()
}

function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    toast.warning('Vui lòng chọn một tệp hình ảnh.')
    event.target.value = ''
    return
  }
  if (file.size > 8 * 1024 * 1024) {
    toast.warning('Ảnh đại diện nên nhỏ hơn 8MB.')
    event.target.value = ''
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    profile.avatarUrl = reader.result || ''
    toast.info('Ảnh đã sẵn sàng. Bấm lưu thông tin để cập nhật.')
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

async function saveAddress() {
  if (!addressForm.id && addresses.value.length >= 4) {
    toast.warning('Mỗi tài khoản chỉ được lưu tối đa 4 địa chỉ.')
    return
  }
  savingAddress.value = true
  try {
    const payload = { ...addressForm }
    if (payload.id) await axios.put(`/api/auth/addresses/${payload.id}`, payload)
    else await axios.post('/api/auth/addresses', payload)
    toast.success('Đã lưu địa chỉ giao hàng.')
    Object.assign(addressForm, emptyAddress())
    await fetchAddresses()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể lưu địa chỉ.')
  } finally {
    savingAddress.value = false
  }
}

async function deleteAddress(id) {
  const accepted = await confirmStore.open({ title: 'Xóa địa chỉ', message: 'Địa chỉ này sẽ bị xóa khỏi tài khoản của bạn.', confirmText: 'Xóa', cancelText: 'Hủy' })
  if (!accepted) return
  try {
    await axios.delete(`/api/auth/addresses/${id}`)
    toast.success('Đã xóa địa chỉ.')
    if (addressForm.id === id) Object.assign(addressForm, emptyAddress())
    await fetchAddresses()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa địa chỉ.')
  }
}

function editAddress(address) {
  Object.assign(addressForm, {
    id: address.id,
    province: address.province || '',
    district: address.district || '',
    ward: address.ward || '',
    detail: address.detail || '',
    isDefault: address.isDefault ?? false
  })
}

function resetAddressForm() {
  Object.assign(addressForm, emptyAddress())
}

function selectTab(tabKey) {
  activeOrderTab.value = tabKey
  selectedOrderId.value = filteredOrders.value[0]?.id || null
}

function statusLabel(status) {
  return orderStatusOptions.find((item) => item.key === status)?.label || status || 'Không rõ'
}
function statusIcon(status) {
  return orderStatusOptions.find((item) => item.key === status)?.icon || 'bi bi-info-circle'
}
function statusClass(status) {
  return orderStatusOptions.find((item) => item.key === status)?.className || 'account-pill--pending'
}
function paymentLabel(status) {
  return status === 'PAID' ? 'Đã thanh toán' : 'Chưa thanh toán'
}
function paymentClass(status) {
  return status === 'PAID' ? 'account-pill--delivered' : 'account-pill--pending'
}
function paymentMethodLabel(method) {
  const map = { COD: 'Thanh toán khi nhận hàng', BANKING: 'Thanh toán MB Bank', MOMO: 'Phương thức cũ', ZALOPAY: 'Phương thức cũ' }
  return map[String(method || '').toUpperCase()] || method || 'COD'
}
function canContinuePayment(order) {
  const method = String(order?.paymentMethod || '').toUpperCase()
  return order?.status === 'PROCESSING' && order?.paymentStatus !== 'PAID' && method && method !== 'COD'
}
async function continuePayment(order) {
  if (!order?.id) return
  continuingPaymentId.value = order.id
  try {
    const { data } = await axios.post(`/api/orders/${order.id}/payment-session`)
    continuedPayment.value = { ...data, orderId: order.id, method: order.paymentMethod }
    toast.success('Đã tạo lại mã thanh toán cho đơn hàng.')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể tạo lại thanh toán.')
  } finally {
    continuingPaymentId.value = null
  }
}
function closeContinuedPayment() {
  continuedPayment.value = null
}
function formatAddress(address) {
  return [address.detail, address.ward, address.district, address.province].filter(Boolean).join(', ')
}
function canReviewOrder(order) {
  return order?.status === 'DELIVERED' && order?.paymentStatus === 'PAID'
}
</script>

<template>
  <main class="account-v2">
        <section class="account-v2__hero">
      <div class="account-v2__identity">
        <div class="account-v2__avatar">
          <img v-if="profile.avatarUrl" :src="profile.avatarUrl" :alt="`Ảnh đại diện của ${profile.fullName || 'bạn'}`">
          <span v-else>{{ initials }}</span>
        </div>
        <div>
          <span class="account-v2__eyebrow">Tài khoản Y2K</span>
          <h1>Xin chào, {{ profileDisplayName }}</h1>
          <p>Theo dõi đơn hàng, cập nhật hồ sơ và đánh giá sản phẩm đã giao thành công.</p>
        </div>
      </div>
      <div class="account-v2__hero-actions">
        <router-link to="/orders" class="btn btn-y2k-primary"><i class="bi bi-receipt me-2" aria-hidden="true"></i>Lịch sử đơn</router-link>
        <button type="button" class="btn btn-y2k-outline" :disabled="loadingAccount" @click="refreshAccount">
          <span v-if="loadingAccount" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-arrow-clockwise me-2" aria-hidden="true"></i>Làm mới
        </button>
      </div>
    </section>

    <section class="account-v2__stats" aria-label="Tổng quan tài khoản">
      <article v-for="stat in accountStats" :key="stat.label" class="account-v2__stat-card">
        <i :class="stat.icon" aria-hidden="true"></i>
        <span>{{ stat.label }}</span>
        <strong>{{ stat.value }}</strong>
        <small>{{ stat.hint }}</small>
      </article>
    </section>

    <section class="account-v2__review-strip" aria-labelledby="account-review-title">
      <div class="account-v2__review-copy">
        <span class="account-v2__eyebrow">Đánh giá</span>
        <h2 id="account-review-title">Sản phẩm chờ bạn đánh giá</h2>
        <p>Chỉ những đơn đã giao và thanh toán xong mới hiện sản phẩm để bạn đánh giá.</p>
      </div>
      <div class="account-v2__review-panel">
        <div class="account-v2__review-meta">
          <span>{{ reviewableProducts.length }} sản phẩm sẵn sàng đánh giá</span>
          <router-link to="/orders">Xem đơn hàng</router-link>
        </div>

        <div v-if="reviewableProducts.length" class="account-v2__review-products">
          <router-link v-for="item in reviewableProducts.slice(0, 1000)" :key="item.productId || item.productName" :to="productPath(item)" class="account-v2__review-product">
            <img :src="item.productImage || '/favicon.svg'" :alt="item.productName">
            <div>
              <strong>{{ item.productName }}</strong>
              <small>Đơn #{{ item.orderId }}</small>
            </div>
          </router-link>
        </div>
        <div v-else class="account-v2__review-empty">
          <i class="bi bi-lock" aria-hidden="true"></i>
          <span>Chưa có sản phẩm đủ điều kiện đánh giá.</span>
        </div>
      </div>
    </section>

    <div class="account-v2__layout">
      <section class="account-v2__orders" aria-labelledby="account-orders-title">
        <div class="account-v2__section-head">
          <div>
            <span class="account-v2__eyebrow">Đơn hàng</span>
            <h2 id="account-orders-title">Theo dõi trạng thái</h2>
          </div>
          <span>{{ filteredOrders.length }} đơn</span>
        </div>

        <div class="account-v2__tabs" role="tablist" aria-label="Lọc đơn hàng">
          <button v-for="tab in orderTabs" :key="tab.key" type="button" role="tab" class="account-v2__tab" :class="{ 'is-active': activeOrderTab === tab.key }" :aria-selected="activeOrderTab === tab.key" @click="selectTab(tab.key)">
            <i :class="tab.icon" aria-hidden="true"></i>
            <span>{{ tab.label }}</span>
            <strong>{{ tab.count }}</strong>
          </button>
        </div>

        <div v-if="loadingOrders" class="account-v2__loading">
          <span class="spinner-border spinner-border-sm"></span>
          Đang tải đơn hàng...
        </div>
        <div v-else-if="!filteredOrders.length" class="account-v2__empty">
          <i class="bi bi-bag" aria-hidden="true"></i>
          <h3>Chưa có đơn trong mục này</h3>
          <p>Khi có đơn phù hợp, hệ thống sẽ hiển thị tại đây.</p>
          <router-link to="/shop" class="btn btn-y2k-primary">Mua sắm ngay</router-link>
        </div>
        <div v-else class="account-v2__order-workspace">
          <div class="account-v2__order-list">
            <button v-for="order in filteredOrders" :key="order.id" type="button" class="account-v2__order-card" :class="{ 'is-active': selectedOrder?.id === order.id }" @click="selectedOrderId = order.id">
              <span class="account-v2__order-icon"><i :class="statusIcon(order.status)" aria-hidden="true"></i></span>
              <span>
                <strong>Đơn #{{ order.id }}</strong>
                <small>{{ formatDate(order.orderDate) }}</small>
              </span>
              <span class="account-v2__order-total">{{ formatPrice(order.totalAmount) }}</span>
            </button>
          </div>

          <article v-if="selectedOrder" class="account-v2__order-detail">
            <header class="account-v2__order-detail-head">
              <div>
                <span class="account-v2__eyebrow">Chi tiết đơn</span>
                <h3>Đơn #{{ selectedOrder.id }}</h3>
                <p>{{ selectedOrder.addressLabel || 'Chưa có địa chỉ giao hàng' }}</p>
              </div>
              <div class="account-v2__badges">
                <span class="account-pill" :class="statusClass(selectedOrder.status)">{{ statusLabel(selectedOrder.status) }}</span>
                <span v-if="selectedOrder.status !== 'CANCELLED'" class="account-pill" :class="paymentClass(selectedOrder.paymentStatus)">{{ paymentLabel(selectedOrder.paymentStatus) }}</span>
              </div>
            </header>

            <div class="account-v2__order-meta">
              <div><small>Người nhận</small><strong>{{ selectedOrder.fullName || profile.fullName || 'Khách hàng' }}</strong></div>
              <div><small>Điện thoại</small><strong>{{ selectedOrder.phone || selectedOrder.userPhone || profile.phone || 'Chưa cập nhật' }}</strong></div>
              <div><small>Thanh toán</small><strong>{{ paymentMethodLabel(selectedOrder.paymentMethod) }}</strong></div>
              <div><small>Tổng tiền</small><strong>{{ formatPrice(selectedOrder.totalAmount) }}</strong></div>
            </div>

            <div v-if="canContinuePayment(selectedOrder)" class="account-v2__payment-cta">
              <div>
                <strong>Đơn hàng này chưa thanh toán</strong>
                <span>Tiếp tục thanh toán để shop xử lý đơn nhanh hơn.</span>
              </div>
              <button type="button" class="btn btn-y2k-primary" :disabled="continuingPaymentId === selectedOrder.id" @click="continuePayment(selectedOrder)">
                <span v-if="continuingPaymentId === selectedOrder.id" class="spinner-border spinner-border-sm me-2"></span>
                Thanh toán
              </button>
            </div>

            <div class="account-v2__items-head">
              <strong>Sản phẩm trong đơn</strong>
              <span>{{ canReviewOrder(selectedOrder) ? 'Có thể đánh giá' : 'Chờ giao và thanh toán' }}</span>
            </div>
            <div class="account-v2__items">
              <router-link v-for="item in selectedOrderItems" :key="item.id" :to="productPath(item)" class="account-v2__item">
                <img :src="item.productImage || '/favicon.svg'" :alt="item.productName">
                <span>
                  <strong>{{ item.productName }}</strong>
                  <small>{{ item.variantLabel || 'Biến thể mặc định' }} x {{ item.quantity }}</small>
                </span>
                <b>{{ canReviewOrder(selectedOrder) ? 'Đánh giá' : formatPrice(item.totalPrice) }}</b>
              </router-link>
            </div>
          </article>
        </div>
      </section>

      <aside class="account-v2__side">
        <form class="account-v2__panel" @submit.prevent="saveProfile">
          <div class="account-v2__section-head account-v2__section-head--compact">
            <div><span class="account-v2__eyebrow">Hồ sơ</span><h2>Thông tin cá nhân</h2></div>
            <button type="button" class="account-v2__icon-btn" aria-label="Chọn ảnh đại diện" title="Chọn ảnh đại diện" @click="openAvatarPicker"><i class="bi bi-camera" aria-hidden="true"></i></button>
          </div>
          <input ref="avatarInput" type="file" class="visually-hidden" accept="image/*" @change="handleAvatarUpload">
          <label class="form-label" for="account-full-name">Họ và tên</label>
          <input id="account-full-name" v-model="profile.fullName" class="form-control" required>
          <label class="form-label" for="account-email">Email</label>
          <input id="account-email" :value="profile.email" class="form-control" disabled>
          <label class="form-label" for="account-phone">Số điện thoại</label>
          <input id="account-phone" v-model="profile.phone" class="form-control" placeholder="Nhập số điện thoại nhận hàng">
          <button class="btn btn-y2k-primary w-100" :disabled="savingProfile"><span v-if="savingProfile" class="spinner-border spinner-border-sm me-2"></span>Lưu thông tin</button>
        </form>

        <section v-if="!profile.emailVerified" class="account-v2__panel account-v2__email-panel" aria-labelledby="account-email-title">
          <span class="account-v2__eyebrow">Bảo mật</span>
          <h2 id="account-email-title">Xác nhận email</h2>
          <p>Xác nhận email để nhận thông báo trạng thái đơn hàng.</p>
          <button v-if="!verificationSent" type="button" class="btn btn-y2k-outline w-100" :disabled="sendingVerification" @click="requestEmailVerification"><span v-if="sendingVerification" class="spinner-border spinner-border-sm me-2"></span>Gửi mã xác nhận</button>
          <div v-else class="account-v2__verify-form">
            <label class="form-label" for="account-email-code">Mã xác nhận 6 số</label>
            <input id="account-email-code" v-model.trim="emailVerificationCode" class="form-control" inputmode="numeric" maxlength="6" autocomplete="one-time-code">
            <button type="button" class="btn btn-y2k-primary w-100" :disabled="verifyingEmail || emailVerificationCode.length !== 6" @click="verifyEmail"><span v-if="verifyingEmail" class="spinner-border spinner-border-sm me-2"></span>Xác nhận email</button>
          </div>
        </section>

        <form class="account-v2__panel" @submit.prevent="saveAddress">
          <div class="account-v2__section-head account-v2__section-head--compact">
            <div><span class="account-v2__eyebrow">Địa chỉ</span><h2>Giao hàng</h2></div>
            <span>{{ addresses.length }}/4</span>
          </div>
          <label class="form-label" for="account-province">Tỉnh/Thành phố</label>
          <input id="account-province" v-model="addressForm.province" class="form-control" placeholder="VD: TP. Hồ Chí Minh" required autocomplete="address-level1">
          <label class="form-label" for="account-district">Quận/Huyện</label>
          <input id="account-district" v-model="addressForm.district" class="form-control" placeholder="VD: Quận 1" required autocomplete="address-level2">
          <label class="form-label" for="account-ward">Phường/Xã</label>
          <input id="account-ward" v-model="addressForm.ward" class="form-control" placeholder="VD: Bến Nghé" required autocomplete="address-level3">
          <label class="form-label" for="account-detail">Địa chỉ cụ thể</label>
          <textarea id="account-detail" v-model="addressForm.detail" class="form-control" rows="3" placeholder="Số nhà, tên đường, ghi chú giao hàng..." required></textarea>
          <label class="account-v2__check"><input v-model="addressForm.isDefault" type="checkbox"><span>Đặt làm địa chỉ mặc định</span></label>
          <div class="account-v2__form-actions">
            <button class="btn btn-y2k-primary" :disabled="savingAddress || (!addressForm.id && addresses.length >= 4)"><span v-if="savingAddress" class="spinner-border spinner-border-sm me-2"></span>{{ addressForm.id ? 'Cập nhật' : 'Thêm địa chỉ' }}</button>
            <button type="button" class="btn btn-y2k-outline" @click="resetAddressForm">Tạo mới</button>
          </div>
        </form>

        <section class="account-v2__panel" aria-labelledby="account-address-list-title">
          <div class="account-v2__section-head account-v2__section-head--compact">
            <div><span class="account-v2__eyebrow">Đã lưu</span><h2 id="account-address-list-title">Sổ địa chỉ</h2></div>
            <span v-if="loadingAddresses" class="spinner-border spinner-border-sm"></span>
          </div>
          <div v-if="!addresses.length" class="account-v2__mini-empty">Chưa có địa chỉ giao hàng.</div>
          <div v-else class="account-v2__address-list">
            <article v-for="address in addresses" :key="address.id" class="account-v2__address">
              <strong>{{ address.isDefault ? 'Mặc định' : 'Địa chỉ' }}</strong>
              <p>{{ formatAddress(address) }}</p>
              <div>
                <button type="button" @click="editAddress(address)">Sửa</button>
                <button type="button" @click="deleteAddress(address.id)">Xóa</button>
              </div>
            </article>
          </div>
        </section>
      </aside>
    </div>

    <Teleport to="body">
    <div v-if="continuedPayment" class="account-payment-modal" role="dialog" aria-modal="true" aria-labelledby="account-payment-title" @click.self="closeContinuedPayment">
      <div class="account-payment-modal__panel">
        <button type="button" class="account-v2__icon-btn account-payment-modal__close" aria-label="Đóng thanh toán" @click="closeContinuedPayment"><i class="bi bi-x-lg" aria-hidden="true"></i></button>
        <span class="account-v2__eyebrow">Tiếp tục thanh toán</span>
        <h2 id="account-payment-title">Đơn #{{ continuedPayment.orderId }} · {{ paymentMethodLabel(continuedPayment.method) }}</h2>
        <p>{{ continuedPayment.message || 'Quét QR hoặc mở link để hoàn tất thanh toán.' }}</p>
        <div class="account-payment-modal__steps" aria-label="Các bước thanh toán">
          <span><i class="bi bi-qr-code-scan" aria-hidden="true"></i>Quét mã</span>
          <span><i class="bi bi-card-checklist" aria-hidden="true"></i>Đúng nội dung</span>
          <span><i class="bi bi-check2-circle" aria-hidden="true"></i>Cập nhật đơn</span>
        </div>
        <div class="account-payment-modal__body">
          <div v-if="continuedPaymentQrImageUrl" class="account-payment-modal__qr">
            <img :src="continuedPaymentQrImageUrl" alt="Mã QR thanh toán">
            <small>Quét mã bằng app ngân hàng.</small>
          </div>
          <div class="account-payment-modal__actions">
            <button type="button" class="btn btn-y2k-outline" @click="refreshAccount"><i class="bi bi-arrow-clockwise me-2"></i>Kiểm tra lại đơn</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>
  </main>
</template>

<style scoped>
.account-v2 { max-width: 1240px; margin: 0 auto; padding: 1.5rem 1rem 4rem; color: #18181b; }
.account-v2__hero { display: flex; align-items: center; justify-content: space-between; gap: 1.5rem; padding: clamp(1.2rem, 3vw, 2rem); border: 1px solid #e4e4e7; border-radius: 1rem; background: linear-gradient(135deg, #ffffff, #ecfeff); }
.account-v2__identity { display: flex; align-items: center; gap: 1rem; min-width: 0; }
.account-v2__avatar { display: grid; place-items: center; width: 86px; height: 86px; flex: 0 0 auto; overflow: hidden; border: 3px solid #fff; border-radius: 1.2rem; background: #18181b; color: #fff; box-shadow: 0 14px 30px rgba(8, 145, 178, .16); font-family: var(--font-display); font-size: 1.35rem; font-weight: 900; }
.account-v2__avatar img { width: 100%; height: 100%; object-fit: cover; }
.account-v2__eyebrow { display: inline-flex; align-items: center; color: #0891b2; text-transform: uppercase; letter-spacing: .12em; font-size: .7rem; font-weight: 900; }
.account-v2 h1, .account-v2 h2, .account-v2 h3 { font-family: var(--font-display); letter-spacing: -.04em; }
.account-v2 h1 { margin: .35rem 0 .45rem; font-size: clamp(2rem, 4vw, 3.7rem); line-height: 1; }
.account-v2 p { margin: 0; color: #52525b; line-height: 1.6; }
.account-v2__hero-actions { display: flex; flex-wrap: wrap; gap: .7rem; justify-content: flex-end; }
.account-v2__stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: .8rem; margin-top: 1rem; }
.account-v2__stat-card { display: grid; gap: .25rem; min-height: 132px; padding: 1rem; border: 1px solid #e4e4e7; border-radius: .85rem; background: #fff; }
.account-v2__stat-card i { color: #0891b2; font-size: 1.25rem; }
.account-v2__stat-card span, .account-v2__stat-card small { color: #71717a; font-size: .78rem; }
.account-v2__stat-card strong { font-size: 1.75rem; line-height: 1; }
.account-v2__review-strip { display: grid; grid-template-columns: minmax(220px, .82fr) minmax(0, 1.18fr); gap: 1rem; margin-top: 1rem; padding: 1rem 1.1rem; border: 1px solid #1f2937; border-radius: 1.1rem; background: #111317; color: #fff; box-shadow: 0 18px 38px rgba(15, 23, 42, .16); }
.account-v2__review-copy { display: grid; align-content: center; gap: .35rem; }
.account-v2__review-strip .account-v2__review-copy p { max-width: 34rem; color: #a1a1aa; }
.account-v2__review-copy h2 { margin: .2rem 0 .15rem; color: #f8fafc; font-size: clamp(1.3rem, 2vw, 1.75rem); line-height: 1.15; }
.account-v2__review-copy .account-v2__eyebrow { color: #38bdf8; }
.account-v2__review-panel { display: grid; gap: .7rem; min-width: 0; padding: .8rem; border: 1px solid rgba(255,255,255,.08); border-radius: .95rem; background: rgba(255,255,255,.04); }
.account-v2__review-meta { display: flex; align-items: center; justify-content: space-between; gap: .75rem; color: #d4d4d8; font-size: .82rem; }
.account-v2__review-meta a { color: #fff; text-decoration: none; font-weight: 800; }
.account-v2__review-meta a:hover { text-decoration: underline; }
.account-v2__review-products { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: .6rem; }
.account-v2__review-product { display: grid; grid-template-columns: 52px minmax(0, 1fr); gap: .65rem; align-items: center; min-width: 0; padding: .5rem; border-radius: .85rem; background: rgba(255,255,255,.08); color: #fff; text-decoration: none; transition: transform .2s ease, background .2s ease; }
.account-v2__review-product:hover { transform: translateY(-2px); background: rgba(255,255,255,.14); }
.account-v2__review-product img { width: 52px; height: 52px; object-fit: cover; border-radius: .7rem; background: #fff; }
.account-v2__review-product div { min-width: 0; display: grid; gap: .15rem; }
.account-v2__review-product strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: .84rem; font-weight: 800; }
.account-v2__review-product small, .account-v2__review-empty { color: #cbd5e1; font-size: .76rem; }
.account-v2__review-empty { display: flex; align-items: center; justify-content: center; gap: .55rem; min-height: 96px; border: 1px dashed rgba(255,255,255,.25); border-radius: .8rem; }
.account-v2__layout { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 1rem; margin-top: 1rem; align-items: start; }
.account-v2__orders, .account-v2__panel { border: 1px solid #e4e4e7; border-radius: 1rem; background: #fff; }
.account-v2__orders { padding: 1rem; min-width: 0; }
.account-v2__section-head { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; }
.account-v2__section-head h2 { margin: .2rem 0 0; font-size: 1.35rem; }
.account-v2__section-head--compact { margin-bottom: .8rem; }
.account-v2__tabs { display: flex; gap: .6rem; overflow-x: auto; padding-bottom: .4rem; }
.account-v2__tab { display: inline-grid; grid-template-columns: auto auto auto; align-items: center; gap: .45rem; min-height: 44px; padding: .55rem .75rem; border: 1px solid #e4e4e7; border-radius: 999px; background: #fafafa; color: #3f3f46; white-space: nowrap; cursor: pointer; transition: border-color .2s ease, background .2s ease, color .2s ease; }
.account-v2__tab.is-active { border-color: #18181b; background: #18181b; color: #fff; }
.account-v2__tab strong { display: grid; place-items: center; min-width: 22px; height: 22px; border-radius: 999px; background: rgba(8,145,178,.14); font-size: .72rem; }
.account-v2__order-workspace { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 1rem; margin-top: .8rem; }
.account-v2__order-list { display: grid; align-content: start; gap: .55rem; max-height: 680px; overflow: auto; }
.account-v2__order-card { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: .65rem; width: 100%; padding: .75rem; border: 1px solid #e4e4e7; border-radius: .8rem; background: #fff; text-align: left; cursor: pointer; transition: border-color .2s ease, background .2s ease; }
.account-v2__order-card.is-active { border-color: #0891b2; background: #ecfeff; }
.account-v2__order-card strong, .account-v2__order-card small { display: block; }
.account-v2__order-card small { color: #71717a; font-size: .73rem; }
.account-v2__order-icon { display: grid; place-items: center; width: 38px; height: 38px; border-radius: .65rem; background: #e0f2fe; color: #0369a1; }
.account-v2__order-total { grid-column: 2; color: #18181b; font-size: .8rem; font-weight: 900; }
.account-v2__order-detail { min-width: 0; padding: 1rem; border-radius: .9rem; background: #fafafa; }
.account-v2__order-detail-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; }
.account-v2__order-detail-head h3 { margin: .2rem 0 .3rem; font-size: 1.6rem; }
.account-v2__badges { display: flex; flex-wrap: wrap; gap: .45rem; justify-content: flex-end; }
.account-pill { display: inline-flex; align-items: center; min-height: 30px; padding: .35rem .65rem; border-radius: 999px; font-size: .72rem; font-weight: 900; }
.account-pill--pending { background: #fef3c7; color: #92400e; }
.account-pill--preparing { background: #dbeafe; color: #1d4ed8; }
.account-pill--shipping { background: #e0f2fe; color: #0369a1; }
.account-pill--delivered { background: #dcfce7; color: #166534; }
.account-pill--cancelled { background: #fee2e2; color: #991b1b; }
.account-v2__order-meta { display: grid; grid-template-columns: repeat(4, 1fr); gap: .6rem; margin: 1rem 0; }
.account-v2__order-meta div { display: grid; gap: .2rem; padding: .7rem; border-radius: .65rem; background: #fff; }
.account-v2__order-meta small { color: #71717a; font-size: .7rem; }
.account-v2__order-meta strong { min-width: 0; overflow-wrap: anywhere; font-size: .82rem; }
.account-v2__payment-cta { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin: -.25rem 0 1rem; padding: .9rem; border: 1px solid #bae6fd; border-radius: .9rem; background: #f0f9ff; box-shadow: inset 4px 0 0 #0891b2; }
.account-v2__payment-cta div { display: grid; gap: .2rem; }
.account-v2__payment-cta strong { color: #0f172a; }
.account-v2__payment-cta span { color: #475569; font-size: .82rem; }
.account-v2__items-head { display: flex; justify-content: space-between; gap: 1rem; margin: .75rem 0; font-size: .86rem; }
.account-v2__items-head span { color: #0891b2; font-weight: 900; }
.account-v2__items { display: grid; gap: .55rem; }
.account-v2__item { display: grid; grid-template-columns: 58px minmax(0, 1fr) auto; align-items: center; gap: .7rem; padding: .55rem; border-radius: .75rem; background: #fff; color: inherit; text-decoration: none; }
.account-v2__item img { width: 58px; height: 64px; object-fit: cover; border-radius: .55rem; background: #f4f4f5; }
.account-v2__item strong, .account-v2__item small { display: block; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.account-v2__item small { color: #71717a; font-size: .75rem; }
.account-v2__item b { color: #0891b2; font-size: .8rem; white-space: nowrap; }
.account-v2__side { display: grid; gap: 1rem; }
.account-v2__panel { display: grid; gap: .7rem; padding: 1rem; }
.account-v2__panel h2 { margin: .2rem 0 0; font-size: 1.25rem; }
.account-v2__icon-btn { display: grid; place-items: center; width: 44px; height: 44px; border: 1px solid #d4d4d8; border-radius: .75rem; background: #fff; color: #18181b; cursor: pointer; transition: border-color .2s ease, color .2s ease; }
.account-v2__icon-btn:hover { border-color: #0891b2; color: #0891b2; }
.account-v2__email-panel { background: #fdf2f8; border-color: #f5d0fe; }
.account-v2__verify-form { display: grid; gap: .65rem; }
.account-v2__check { display: flex; align-items: center; gap: .55rem; min-height: 44px; color: #52525b; cursor: pointer; }
.account-v2__check input { width: 18px; height: 18px; }
.account-v2__form-actions { display: grid; grid-template-columns: 1fr 1fr; gap: .6rem; }
.account-v2__address-list { display: grid; gap: .6rem; }
.account-v2__address { display: grid; gap: .35rem; padding: .75rem; border: 1px solid #e4e4e7; border-radius: .75rem; background: #fafafa; }
.account-v2__address p { font-size: .82rem; }
.account-v2__address div { display: flex; gap: .65rem; }
.account-v2__address button { min-height: 34px; padding: 0; border: 0; background: transparent; color: #0891b2; font-size: .8rem; font-weight: 900; cursor: pointer; }
.account-v2__mini-empty, .account-v2__loading, .account-v2__empty { color: #71717a; }
.account-v2__loading, .account-v2__empty { display: grid; place-items: center; gap: .7rem; min-height: 260px; text-align: center; }
.account-v2__empty i { color: #0891b2; font-size: 2rem; }
.form-label { margin: .15rem 0 -.35rem; color: #3f3f46; font-size: .78rem; font-weight: 800; }
.form-control { min-height: 44px; border-color: #d4d4d8; border-radius: .7rem; }
.form-control:focus { border-color: #0891b2; box-shadow: 0 0 0 .2rem rgba(8,145,178,.12); }
.account-payment-modal { position: fixed; inset: 0; z-index: 3000; display: grid; place-items: center; padding: 1rem; background: transparent; }
.account-payment-modal__panel { position: relative; overflow: hidden; width: min(680px, 100%); padding: 1.4rem; border: 1px solid #7dd3fc; border-radius: 1rem; background: linear-gradient(90deg, rgba(8,145,178,.1), transparent 42%), #fff; box-shadow: 0 24px 80px rgba(15,23,42,.25), inset 0 4px 0 #0891b2; }
.account-payment-modal__close { position: absolute; top: .8rem; right: .8rem; width: 38px; height: 38px; }
.account-payment-modal__panel h2 { margin: .4rem 2.5rem .4rem 0; font-size: 1.35rem; }
.account-payment-modal__panel p { max-width: 560px; color: #1e293b; font-weight: 800; line-height: 1.65; }
.account-payment-modal__steps { display: flex; flex-wrap: wrap; gap: .5rem; margin-top: .8rem; }
.account-payment-modal__steps span { display: inline-flex; align-items: center; gap: .35rem; min-height: 34px; padding: .35rem .65rem; border: 1px solid #bae6fd; border-radius: 999px; background: rgba(255,255,255,.86); color: #0f172a; font-size: .76rem; font-weight: 900; white-space: nowrap; }
.account-payment-modal__steps i { color: #0891b2; }
.account-payment-modal__body { display: grid; grid-template-columns: 210px minmax(0, 1fr); gap: 1rem; align-items: center; margin-top: 1rem; }
.account-payment-modal__qr { display: grid; justify-items: center; gap: .5rem; padding: .9rem; border: 1px solid #bae6fd; border-radius: .9rem; background: #fff; box-shadow: 0 12px 26px rgba(15, 23, 42, .08); }
.account-payment-modal__qr img { width: 180px; height: 180px; object-fit: contain; border-radius: .65rem; }
.account-payment-modal__qr small { color: #334155; text-align: center; font-weight: 700; }
.account-payment-modal__actions { display: grid; gap: .7rem; }
@media (max-width: 1040px) {
  .account-v2__layout { grid-template-columns: 1fr; }
  .account-v2__side { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 820px) {
  .account-v2__hero, .account-v2__order-detail-head { align-items: stretch; flex-direction: column; }
  .account-v2__hero-actions, .account-v2__badges { justify-content: flex-start; }
  .account-v2__stats, .account-v2__review-strip, .account-v2__order-workspace, .account-v2__order-meta, .account-v2__side { grid-template-columns: 1fr; }
  .account-v2__review-products { grid-template-columns: 1fr; }
  .account-v2__review-meta { align-items: flex-start; flex-direction: column; }
  .account-payment-modal__body { grid-template-columns: 1fr; }
}
@media (max-width: 560px) {
  .account-v2 { padding-inline: .85rem; }
  .account-v2__identity { align-items: flex-start; flex-direction: column; }
  .account-v2__avatar { width: 74px; height: 74px; }
  .account-v2__stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .account-v2__item { grid-template-columns: 52px minmax(0, 1fr); }
  .account-v2__item b { grid-column: 2; }
  .account-v2__form-actions { grid-template-columns: 1fr; }
  .account-v2__payment-cta { align-items: stretch; flex-direction: column; }
}
</style>
