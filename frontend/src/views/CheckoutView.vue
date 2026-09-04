<template>
  <div class="y2k-page-container checkout-layout" :class="{ 'checkout-layout--success': success }">
    <template v-if="success">
      <section class="checkout-success-panel">
        <div class="checkout-success-panel__topline">
          <span class="section-heading__eyebrow">Order confirmed / Y2K Store</span>
          <span class="status-pill status-pill--delivered">Đã tạo đơn</span>
        </div>

        <div class="checkout-success-panel__icon" aria-hidden="true">
          <i class="bi bi-check2"></i>
        </div>
        <h1>Đặt hàng thành công</h1>
        <p class="checkout-success-panel__lead">
          Đơn hàng <strong>#{{ orderConfirmation.orderId }}</strong> đã được ghi nhận. Bạn có thể theo dõi tiến trình giao hàng trong lịch sử đơn hàng.
        </p>

        <div class="checkout-success-meta">
          <div>
            <span>Mã đơn</span>
            <strong>#{{ orderConfirmation.orderId }}</strong>
          </div>
          <div>
            <span>Thanh toán</span>
            <strong>{{ paymentLabel(orderConfirmation.paymentMethod) }}</strong>
          </div>
          <div>
            <span>Trạng thái</span>
            <strong>{{ paymentStatusLabel }}</strong>
          </div>
        </div>

        <div v-if="onlinePaymentPending" class="checkout-online-payment">
          <div class="checkout-online-payment__copy">
            <span class="section-heading__eyebrow">Thanh toán online</span>
            <h2>{{ paymentLabel(orderConfirmation.paymentMethod) }}</h2>
            <p>{{ paymentSession.message || 'Quét QR hoặc mở link để hoàn tất thanh toán.' }}</p>
            <div class="checkout-online-payment__steps" aria-label="Các bước thanh toán">
              <span><i class="bi bi-qr-code-scan" aria-hidden="true"></i>Quét QR</span>
              <span><i class="bi bi-receipt" aria-hidden="true"></i>Đúng nội dung</span>
              <span><i class="bi bi-shield-check" aria-hidden="true"></i>Shop xác nhận</span>
            </div>
            <div class="checkout-online-payment__actions">
              <a v-if="paymentSession.paymentUrl" :href="paymentSession.paymentUrl" target="_blank" rel="noopener" class="btn btn-y2k-primary">
                <i class="bi bi-box-arrow-up-right me-2"></i>Mở trang thanh toán
              </a>
              <a v-if="paymentSession.deeplink" :href="paymentSession.deeplink" class="btn btn-y2k-outline">
                <i class="bi bi-phone me-2"></i>Mở ứng dụng
              </a>
              <button type="button" class="btn btn-y2k-outline" :disabled="paymentChecking" @click="refreshPaymentStatus">
                <span v-if="paymentChecking" class="spinner-border spinner-border-sm me-2"></span>
                Tôi đã thanh toán
              </button>
            </div>
          </div>
          <div v-if="paymentQrImageUrl" class="checkout-online-payment__qr">
            <img :src="paymentQrImageUrl" alt="Mã QR thanh toán">
            <small>Quét mã bằng app ngân hàng hoặc ví điện tử.</small>
          </div>
        </div>

        <div class="checkout-success-panel__actions">
          <router-link
            :to="orderConfirmation.orderId ? `/orders?highlight=${orderConfirmation.orderId}` : '/orders'"
            class="btn btn-y2k-primary"
          >
            <i class="bi bi-receipt me-2"></i>Xem chi tiết đơn
          </router-link>
          <router-link to="/shop" class="btn btn-y2k-outline">
            <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
          </router-link>
        </div>
      </section>

      <aside class="checkout-success-summary">
        <div class="checkout-success-summary__head">
          <div>
            <span class="section-heading__eyebrow">Review</span>
            <h2>Đơn hàng #{{ orderConfirmation.orderId }}</h2>
          </div>
          <span class="checkout-success-summary__count">{{ orderConfirmation.items.length }} sản phẩm</span>
        </div>

        <div class="checkout-confirmation-items">
          <article v-for="item in orderConfirmation.items" :key="item.variantId || item.id" class="checkout-confirmation-item">
            <img :src="item.productImage || '/summer_y2k.png'" :alt="item.productName">
            <div>
              <strong>{{ item.productName }}</strong>
              <span>{{ item.variantLabel || 'Biến thể mặc định' }} · SL {{ item.quantity }}</span>
            </div>
            <strong>{{ formatPrice(item.totalPrice) }}</strong>
          </article>
        </div>

        <div class="summary-stack checkout-confirmation-total">
          <div class="summary-row">
            <span>Tạm tính</span>
            <strong>{{ formatPrice(orderConfirmation.subtotal) }}</strong>
          </div>
          <div class="summary-row">
            <span>Phí vận chuyển</span>
            <strong>{{ formatPrice(orderConfirmation.shippingFee) }}</strong>
          </div>
          <div class="summary-row">
            <span>Giảm giá<span v-if="orderConfirmation.couponCode"> · {{ orderConfirmation.couponCode }}</span></span>
            <strong class="text-success">-{{ formatPrice(orderConfirmation.discountAmount) }}</strong>
          </div>
          <div class="summary-row summary-row--total">
            <span>Tổng thanh toán</span>
            <strong>{{ formatPrice(orderConfirmation.finalAmount) }}</strong>
          </div>
        </div>

        <div class="checkout-confirmation-info">
          <div>
            <i class="bi bi-person" aria-hidden="true"></i>
            <span>
              <small>Người nhận</small>
              <strong>{{ orderConfirmation.customerName }} · {{ orderConfirmation.phone }}</strong>
            </span>
          </div>
          <div>
            <i class="bi bi-geo-alt" aria-hidden="true"></i>
            <span>
              <small>Giao đến</small>
              <strong>{{ orderConfirmation.addressLabel }}</strong>
            </span>
          </div>
          <div>
            <i class="bi bi-credit-card" aria-hidden="true"></i>
            <span>
              <small>Phương thức thanh toán</small>
              <strong>{{ paymentLabel(orderConfirmation.paymentMethod) }}</strong>
            </span>
          </div>
          <div v-if="orderConfirmation.note">
            <i class="bi bi-chat-left-text" aria-hidden="true"></i>
            <span>
              <small>Ghi chú đơn hàng</small>
              <strong>{{ orderConfirmation.note }}</strong>
            </span>
          </div>
        </div>
      </aside>
    </template>

    <template v-else>
    <section class="summary-stack">
      <div class="form-card">
        <div class="section-heading" style="margin-bottom: 0.75rem;">
          <div>
            <span class="section-heading__eyebrow">Checkout</span>
            <h1>Xác nhận đơn hàng</h1>
          </div>
        </div>

        <form class="summary-stack" @submit.prevent="placeOrder">
          <div class="checkout-address-box">
            <div class="d-flex justify-content-between align-items-start gap-3">
              <div>
                <span class="section-heading__eyebrow">Địa chỉ giao hàng</span>
                <h3>Chọn địa chỉ mặc định hoặc địa chỉ khác</h3>
              </div>
              <router-link to="/account" class="btn btn-y2k-outline btn-sm">Quản lý địa chỉ</router-link>
            </div>

            <div v-if="addresses.length" class="checkout-address-select">
              <label class="form-label">Địa chỉ nhận hàng</label>
              <select v-model.number="selectedAddressId" class="y2k-select">
                <option v-for="address in addresses" :key="address.id" :value="address.id">
                  {{ address.isDefault ? 'Mặc định · ' : '' }}{{ formatAddress(address) }}
                </option>
              </select>
              <div v-if="selectedAddress" class="checkout-address-preview">
                <span :class="selectedAddress.isDefault ? 'status-pill status-pill--delivered' : 'status-pill status-pill--pending'">
                  {{ selectedAddress.isDefault ? 'Địa chỉ mặc định' : 'Địa chỉ lưu' }}
                </span>
                <p>{{ formatAddress(selectedAddress) }}</p>
              </div>
            </div>

            <div v-else class="empty-state checkout-empty-address">
              <h4>Chưa có địa chỉ lưu sẵn</h4>
              <p class="muted-copy">Bạn hãy thêm địa chỉ ở trang tài khoản trước khi thanh toán.</p>
              <router-link to="/account" class="btn btn-y2k-primary">Thêm địa chỉ</router-link>
            </div>
          </div>

          <div class="form-grid">
            <div>
              <label class="form-label">Họ và tên</label>
              <input class="form-control" :value="form.fullName" disabled>
            </div>
            <div>
              <label class="form-label">Số điện thoại</label>
              <input class="form-control" :value="form.phone" disabled>
            </div>
          </div>

          <div>
            <label class="form-label">Ghi chú đơn hàng</label>
            <textarea v-model="form.note" class="form-control" rows="4" placeholder="Thời gian giao, lưu ý tòa nhà, ghi chú khác..."></textarea>
          </div>

          <div class="checkout-coupon-card">
            <div class="checkout-coupon-head">
              <div>
                <span class="section-heading__eyebrow">Mã giảm giá</span>
                <h3>Áp coupon cho đơn hàng</h3>
              </div>
              <span v-if="appliedCoupon" class="status-pill status-pill--delivered">
                -{{ couponDiscountLabel }}
              </span>
            </div>

            <div class="checkout-coupon-row">
              <input
                v-model.trim="couponInput"
                class="y2k-input"
                type="text"
                placeholder="Nhập mã giảm giá"
                @keydown.enter.prevent="applyCoupon"
              >
              <button type="button" class="btn btn-y2k-outline" :disabled="couponLoading" @click="applyCoupon">
                <span v-if="couponLoading" class="spinner-border spinner-border-sm me-2"></span>
                Áp mã
              </button>
            </div>
            <div class="checkout-coupon-suggestions" aria-label="Mã giảm giá gợi ý">
              <button
                v-for="coupon in promoHighlights"
                :key="coupon.couponCode"
                type="button"
                @click="useCouponCode(coupon.couponCode)"
              >
                {{ coupon.couponCode }}
              </button>
            </div>
            <p v-if="couponMessage" class="muted-copy" :class="couponMessageTone">{{ couponMessage }}</p>
          </div>

          <div class="summary-stack">
            <div class="quantity-pill">Phương thức thanh toán</div>
            <label class="detail-panel-item checkout-payment-option">
              <input v-model="form.paymentMethod" type="radio" value="COD">
              <div>
                <strong>Thanh toán khi nhận hàng</strong>
                <p>Phù hợp cho đơn nội địa và dễ theo dõi.</p>
              </div>
            </label>
            <label class="detail-panel-item checkout-payment-option">
              <input v-model="form.paymentMethod" type="radio" value="BANKING">
              <div>
                <strong>Thanh toán MB Bank</strong>
                <p>Quét QR MB Bank theo đúng số tiền và nội dung đơn hàng.</p>
              </div>
            </label>
          </div>

          <button class="btn btn-y2k-primary" :disabled="submitting || !cartStore.items.length || !addresses.length">
            <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
            Đặt hàng · {{ formatPrice(finalAmount) }}
          </button>
        </form>
      </div>
    </section>

    <aside class="summary-stack">
      <div class="summary-card">
        <div class="section-heading" style="margin-bottom: 0.75rem;">
          <div>
            <span class="section-heading__eyebrow">Review</span>
            <h3>Giỏ hàng</h3>
          </div>
        </div>

        <div class="checkout-items">
          <article v-for="item in cartStore.items" :key="item.variantId || item.id" class="checkout-line">
            <img :src="item.productImage || '/summer_y2k.png'" :alt="item.productName">
            <div>
              <strong>{{ item.productName }}</strong>
              <p class="muted-copy" style="margin: 0.35rem 0;">
                {{ item.variantLabel || 'Biến thể mặc định' }} x {{ item.quantity }}
              </p>
            </div>
            <strong>{{ formatPrice(item.totalPrice) }}</strong>
          </article>
        </div>

        <div class="summary-stack" style="margin-top: 1rem;">
          <div class="summary-row">
            <span>Tạm tính</span>
            <strong>{{ formatPrice(subtotal) }}</strong>
          </div>
          <div class="summary-row">
            <span>Phí vận chuyển</span>
            <strong>{{ formatPrice(shippingFee) }}</strong>
          </div>
          <div class="summary-row">
            <span>Giảm giá</span>
            <strong class="text-success">-{{ formatPrice(discountAmount) }}</strong>
          </div>
          <div class="summary-row summary-row--total">
            <span>Tổng thanh toán</span>
            <strong>{{ formatPrice(finalAmount) }}</strong>
          </div>
        </div>

        <div class="checkout-summary-note" v-if="selectedAddress">
          <strong>Giao đến</strong>
          <p>{{ formatAddress(selectedAddress) }}</p>
        </div>
      </div>
    </aside>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useNotificationStore } from '../stores/notifications'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../composables/useFormat'
import { promoHighlights } from '../data/promoHighlights'

const authStore = useAuthStore()
const cartStore = useCartStore()
const notificationStore = useNotificationStore()
const toast = useToastStore()

const submitting = ref(false)
const success = ref(false)
const orderConfirmation = ref(null)
const addresses = ref([])
const selectedAddressId = ref(null)
const couponInput = ref('')
const appliedCoupon = ref(null)
const couponLoading = ref(false)
const couponMessage = ref('')
const couponMessageTone = ref('text-muted')
const paymentChecking = ref(false)
const form = ref({
  fullName: '',
  phone: '',
  note: '',
  paymentMethod: 'COD'
})

const selectedAddress = computed(() => addresses.value.find((item) => item.id === selectedAddressId.value) || addresses.value.find((item) => item.isDefault) || addresses.value[0] || null)
const subtotal = computed(() => cartStore.totalAmount)
const shippingFee = computed(() => subtotal.value >= 499000 ? 0 : 30000)
const couponPercent = computed(() => Number(appliedCoupon.value?.promotionDiscountPercent || 0))
const discountAmount = computed(() => {
  const fixed = Number(appliedCoupon.value?.discountValue || 0)
  if (fixed > 0) return Math.min(fixed, subtotal.value)
  return Math.min(subtotal.value * couponPercent.value / 100, subtotal.value)
})
const couponDiscountLabel = computed(() => Number(appliedCoupon.value?.discountValue || 0) > 0
  ? formatPrice(discountAmount.value)
  : `${couponPercent.value}%`)
const finalAmount = computed(() => Math.max(subtotal.value + shippingFee.value - discountAmount.value, 0))
const paymentSession = computed(() => orderConfirmation.value?.paymentSession || {})
const onlinePaymentPending = computed(() => Boolean(paymentSession.value?.paymentUrl || paymentSession.value?.qrCode || paymentSession.value?.deeplink) && orderConfirmation.value?.paymentStatus !== 'PAID')
const paymentQrImageUrl = computed(() => {
  const qrCode = paymentSession.value?.qrCode || ''
  if (qrCode.startsWith('http://') || qrCode.startsWith('https://')) return qrCode
  const qrPayload = qrCode || paymentSession.value?.paymentUrl || ''
  return qrPayload ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(qrPayload)}` : ''
})
const paymentStatusLabel = computed(() => {
  const status = orderConfirmation.value?.paymentStatus
  if (status === 'PAID') return 'Đã thanh toán'
  if (status === 'PENDING') return 'Chờ thanh toán'
  return 'Đang xử lý'
})

onMounted(async () => {
  await Promise.all([authStore.fetchProfile(), cartStore.fetchCart(), fetchAddresses()])
  form.value.fullName = authStore.user?.fullName || ''
  form.value.phone = authStore.user?.phone || ''
})

async function fetchAddresses() {
  try {
    const { data } = await axios.get('/api/auth/addresses')
    addresses.value = data || []
    selectedAddressId.value = addresses.value.find((item) => item.isDefault)?.id || addresses.value[0]?.id || null
  } catch {
    addresses.value = []
    selectedAddressId.value = null
  }
}

async function applyCoupon() {
  const code = couponInput.value.trim()
  if (!code) {
    appliedCoupon.value = null
    couponMessage.value = 'Vui lòng nhập mã giảm giá.'
    couponMessageTone.value = 'text-danger'
    return
  }

  couponLoading.value = true
  couponMessage.value = ''
  try {
    const { data } = await axios.get('/api/coupons/validate', { params: { code } })
    appliedCoupon.value = data?.coupon || null
    couponMessage.value = `Đã áp dụng mã ${data?.coupon?.couponCode || code}.`
    couponMessageTone.value = 'text-success'
    couponInput.value = data?.coupon?.couponCode || code
  } catch (error) {
    appliedCoupon.value = null
    couponMessage.value = error.response?.data?.message || 'Không thể áp dụng mã giảm giá.'
    couponMessageTone.value = 'text-danger'
  } finally {
    couponLoading.value = false
  }
}

function useCouponCode(code) {
  couponInput.value = code
  applyCoupon()
}

async function placeOrder() {
  if (!cartStore.items.length) {
    toast.warning('Giỏ hàng đang trống.')
    return
  }

  if (!selectedAddress.value) {
    toast.warning('Bạn cần chọn một địa chỉ giao hàng.')
    return
  }

  const orderSnapshot = {
    items: cartStore.items.map((item) => ({ ...item })),
    subtotal: subtotal.value,
    shippingFee: shippingFee.value,
    discountAmount: discountAmount.value,
    finalAmount: finalAmount.value,
    addressLabel: formatAddress(selectedAddress.value),
    customerName: form.value.fullName,
    phone: form.value.phone,
    paymentMethod: form.value.paymentMethod,
    couponCode: appliedCoupon.value?.couponCode || couponInput.value.trim() || null,
    note: form.value.note
  }

  submitting.value = true
  try {
    const payload = {
      note: form.value.note,
      paymentMethod: form.value.paymentMethod,
      addressId: selectedAddress.value.id,
      couponCode: appliedCoupon.value?.couponCode || couponInput.value.trim() || null,
      items: cartStore.items.map((item) => ({
        variantId: item.variantId || item.id,
        quantity: item.quantity
      }))
    }

    const { data } = await axios.post('/api/orders', payload)
    orderConfirmation.value = {
      orderId: data.orderId,
      items: orderSnapshot.items,
      subtotal: Number(data.subtotal ?? orderSnapshot.subtotal),
      shippingFee: Number(data.shippingFee ?? orderSnapshot.shippingFee),
      discountAmount: Number(data.discountAmount ?? orderSnapshot.discountAmount),
      finalAmount: Number(data.totalAmount ?? orderSnapshot.finalAmount),
      addressLabel: orderSnapshot.addressLabel,
      customerName: orderSnapshot.customerName,
      phone: orderSnapshot.phone,
      paymentMethod: orderSnapshot.paymentMethod,
      paymentStatus: data.paymentStatus || (orderSnapshot.paymentMethod === 'COD' ? 'UNPAID' : 'PENDING'),
      paymentSession: data.paymentSession || null,
      couponCode: data.couponCode || orderSnapshot.couponCode,
      note: orderSnapshot.note
    }
    notificationStore.pushOrderPlaced({
      orderId: data.orderId,
      paymentMethod: orderSnapshot.paymentMethod,
      paymentStatus: orderConfirmation.value.paymentStatus,
      totalAmount: formatPrice(orderConfirmation.value.finalAmount)
    })
    success.value = true
    await cartStore.clearCart()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể đặt đơn hàng này.')
  } finally {
    submitting.value = false
  }
}

async function refreshPaymentStatus() {
  const orderId = orderConfirmation.value?.orderId
  if (!orderId) return
  paymentChecking.value = true
  try {
    const { data } = await axios.get(`/api/payments/orders/${orderId}/status`)
    orderConfirmation.value = { ...orderConfirmation.value, paymentStatus: data?.status || orderConfirmation.value.paymentStatus }
    if (data?.status === 'PAID') {
      notificationStore.pushPaymentConfirmed({
        orderId,
        totalAmount: formatPrice(orderConfirmation.value.finalAmount)
      })
      toast.success('Đã xác nhận thanh toán thành công.')
    }
    else {
      toast.info('Thanh toán chưa được xác nhận. Vui lòng thử lại sau ít phút.')
    }
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể kiểm tra trạng thái thanh toán.')
  } finally {
    paymentChecking.value = false
  }
}

function formatAddress(address) {
  return [address?.detail, address?.ward, address?.district, address?.province].filter(Boolean).join(', ')
}

function paymentLabel(method) {
  return {
    COD: 'Thanh toán khi nhận hàng',
    BANKING: 'Thanh toán MB Bank',
  }[method] || method || 'Chưa xác định'
}
</script>

<style scoped>
.checkout-online-payment {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 226px;
  gap: 1.2rem;
  margin-top: 1.2rem;
  padding: clamp(1rem, 2vw, 1.25rem);
  border: 1px solid #7dd3fc;
  border-radius: 1.1rem;
  background:
    linear-gradient(90deg, rgba(8,145,178,.12), transparent 38%),
    #f0f9ff;
  box-shadow: 0 18px 42px rgba(14, 116, 144, .12), inset 0 4px 0 #0891b2;
}
.checkout-online-payment::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 6px;
  background: #0891b2;
}
.checkout-online-payment .section-heading__eyebrow {
  color: #0e7490;
}
.checkout-online-payment__copy h2 {
  margin: .35rem 0;
  font-family: var(--font-display);
  color: #0f172a;
  letter-spacing: -.04em;
}
.checkout-online-payment__copy p {
  max-width: 580px;
  margin: 0;
  color: #1e293b;
  font-weight: 800;
  line-height: 1.7;
}
.checkout-online-payment__steps {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: .85rem;
}
.checkout-online-payment__steps span {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  min-height: 34px;
  padding: .35rem .65rem;
  border: 1px solid #bae6fd;
  border-radius: 999px;
  background: rgba(255,255,255,.82);
  color: #0f172a;
  font-size: .76rem;
  font-weight: 900;
  white-space: nowrap;
}
.checkout-online-payment__steps i {
  color: #0891b2;
}
.checkout-online-payment__actions {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
  margin-top: .9rem;
}
.checkout-online-payment__qr {
  display: grid;
  justify-items: center;
  gap: .45rem;
  padding: .85rem;
  border: 1px solid #bae6fd;
  border-radius: 1rem;
  background: #fff;
  text-align: center;
  box-shadow: 0 12px 26px rgba(15, 23, 42, .08);
}
.checkout-online-payment__qr img {
  width: 190px;
  height: 190px;
  object-fit: contain;
  border-radius: .7rem;
}
.checkout-online-payment__qr small {
  max-width: 185px;
  color: #334155;
  font-size: .72rem;
  font-weight: 700;
}
.checkout-coupon-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: .45rem;
}
.checkout-coupon-suggestions button {
  min-height: 34px;
  padding: .3rem .65rem;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #fff;
  color: #1e40af;
  font-size: .74rem;
  font-weight: 900;
  cursor: pointer;
  transition: border-color .2s ease, color .2s ease, transform .2s ease;
}
.checkout-coupon-suggestions button:hover {
  border-color: #7c3aed;
  color: #7c3aed;
  transform: translateY(-1px);
}
@media (max-width: 640px) {
  .checkout-online-payment {
    grid-template-columns: 1fr;
  }
}
</style>
