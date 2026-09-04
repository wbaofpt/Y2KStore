<template>
  <div class="y2k-page-container order-history-page">
    <section class="order-history-hero">
      <div class="order-history-hero__content">
        <span class="section-heading__eyebrow">Tài khoản / Đơn hàng</span>
        <h1>Lịch sử đơn hàng</h1>
        <p>Xem trọn vẹn sản phẩm, người nhận, thanh toán và trạng thái của từng đơn hàng tại một nơi.</p>
      </div>
      <router-link to="/shop" class="btn btn-y2k-primary order-history-hero__action">
        <i class="bi bi-arrow-left me-2" aria-hidden="true"></i>Mua sắm tiếp
      </router-link>
    </section>

    <section class="order-history-stats" aria-label="Tóm tắt đơn hàng">
      <component
        :is="stat.filterValue ? 'button' : 'article'"
        v-for="stat in stats"
        :key="stat.label"
        class="order-history-stat"
        :class="{ 'order-history-stat--action': stat.filterValue, active: activeFilter === stat.filterValue }"
        :type="stat.filterValue ? 'button' : undefined"
        :aria-pressed="stat.filterValue ? activeFilter === stat.filterValue : undefined"
        @click="selectStatFilter(stat.filterValue)"
      >
        <span class="order-history-stat__icon"><i :class="stat.icon"></i></span>
        <div>
          <span>{{ stat.label }}</span>
          <strong>{{ stat.value }}</strong>
        </div>
      </component>
    </section>

    <section ref="orderHistoryPanel" class="order-history-panel">
      <div class="order-history-toolbar">
        <div>
          <span class="section-heading__eyebrow">Theo dõi đơn hàng</span>
          <h2>Đơn hàng của bạn</h2>
          <p class="order-history-toolbar__hint">Thông tin chi tiết được hiển thị sẵn để bạn kiểm tra nhanh.</p>
        </div>
        <div class="order-history-filters" role="tablist" aria-label="Lọc đơn hàng">
          <button
            v-for="filter in filters"
            :key="filter.value"
            :id="`order-filter-${filter.value.toLowerCase()}`"
            type="button"
            role="tab"
            class="order-filter-chip"
            :class="{ active: activeFilter === filter.value }"
            :aria-selected="activeFilter === filter.value"
            @click="activeFilter = filter.value"
          >
            {{ filter.label }} <span>{{ filterCount(filter.value) }}</span>
          </button>
        </div>
      </div>

      <div v-if="loading" class="order-history-loading" aria-live="polite">
        <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
        <span>Đang tải lịch sử đơn hàng...</span>
      </div>
      <div v-else-if="loadError" class="order-history-empty order-history-empty--error" role="alert">
        <i class="bi bi-wifi-off" aria-hidden="true"></i>
        <h3>Không tải được lịch sử đơn hàng</h3>
        <p>{{ loadError }}</p>
        <button type="button" class="btn btn-y2k-primary" @click="fetchOrders">
          <i class="bi bi-arrow-clockwise me-2"></i>Thử lại
        </button>
      </div>
      <div v-else-if="!filteredOrders.length" class="order-history-empty">
        <i class="bi bi-receipt-cutoff" aria-hidden="true"></i>
        <h3>{{ orders.length ? 'Chưa có đơn trong bộ lọc này' : 'Bạn chưa có đơn hàng nào' }}</h3>
        <p>{{ orders.length ? 'Chọn trạng thái khác để xem thêm đơn hàng.' : 'Những món đồ đầu tiên của bạn đang chờ ở cửa hàng.' }}</p>
        <router-link to="/shop" class="btn btn-y2k-outline">Khám phá sản phẩm</router-link>
      </div>

      <div v-else class="order-history-list">
        <article v-for="order in filteredOrders" :key="order.id" class="order-history-card">
          <header class="order-history-card__top">
            <div class="order-history-card__identity">
              <span class="order-history-card__eyebrow">Đơn hàng #{{ order.id }}</span>
              <h3>{{ formatDate(order.orderDate) }}</h3>
              <span class="order-history-card__subline">Đặt hàng qua Y2K Store</span>
            </div>
            <div class="order-history-card__badges" aria-label="Trạng thái đơn hàng">
              <span class="status-pill" :class="statusClass(order.status)">
                <i :class="statusIcon(order.status)" aria-hidden="true"></i>{{ statusLabel(order.status) }}
              </span>
              <span v-if="order.status !== 'CANCELLED'" class="status-pill" :class="paymentClass(order.paymentStatus)">
                <i :class="order.paymentStatus === 'PAID' ? 'bi bi-check-circle' : 'bi bi-clock'" aria-hidden="true"></i>{{ paymentLabel(order.paymentStatus) }}
              </span>
            </div>
          </header>

          <div class="order-history-card__content">
            <section class="order-history-section order-history-section--items" :aria-labelledby="`items-title-${order.id}`">
              <div class="order-history-section__head">
                <div>
                  <span class="order-history-section__eyebrow">Sản phẩm trong đơn</span>
                  <h4 :id="`items-title-${order.id}`">{{ orderItemCount(order.id) }} sản phẩm</h4>
                </div>
                <i class="bi bi-bag-check" aria-hidden="true"></i>
              </div>

              <div v-if="itemLoadingById[order.id]" class="order-history-items-loading" aria-live="polite">
                <span class="order-history-skeleton order-history-skeleton--image"></span>
                <span class="order-history-skeleton order-history-skeleton--copy"></span>
              </div>
              <div v-else-if="itemErrorById[order.id]" class="order-history-items-error" role="alert">
                <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
                <span>{{ itemErrorById[order.id] }}</span>
              </div>
              <div v-else-if="!orderItemsById[order.id]?.length" class="order-history-items-error">
                <i class="bi bi-box2" aria-hidden="true"></i>
                <span>Chưa có thông tin sản phẩm trong đơn này.</span>
              </div>
              <div v-else class="order-history-item-list">
                <div v-for="item in orderItemsById[order.id]" :key="item.id" class="order-history-item">
                  <img :src="item.productImage || '/summer_y2k.png'" :alt="item.productName || 'Sản phẩm trong đơn hàng'" loading="lazy">
                  <div class="order-history-item__copy">
                    <strong>{{ item.productName || 'Sản phẩm đã cập nhật' }}</strong>
                    <span>{{ item.variantLabel || 'Biến thể mặc định' }}</span>
                    <small>SL {{ item.quantity }} · {{ formatPrice(item.unitPrice) }} / sản phẩm</small>
                  </div>
                  <strong class="order-history-item__total">{{ formatPrice(item.totalPrice) }}</strong>
                </div>
              </div>
            </section>

            <aside class="order-history-section order-history-section--summary" :aria-labelledby="`summary-title-${order.id}`">
              <div class="order-history-section__head">
                <div>
                  <span class="order-history-section__eyebrow">Thanh toán</span>
                  <h4 :id="`summary-title-${order.id}`">Tóm tắt chi phí</h4>
                </div>
                <i class="bi bi-receipt" aria-hidden="true"></i>
              </div>
              <dl class="order-history-summary">
                <div>
                  <dt>Tạm tính</dt>
                  <dd>{{ formatPrice(orderSubtotal(order)) }}</dd>
                </div>
                <div v-if="hasDiscount(order)">
                  <dt>Giảm giá <span v-if="order.couponCode">({{ order.couponCode }})</span></dt>
                  <dd class="order-history-summary__discount">− {{ formatPrice(orderDiscount(order)) }}</dd>
                </div>
                <div class="order-history-summary__grand">
                  <dt>Tổng thanh toán</dt>
                  <dd>{{ formatPrice(order.totalAmount) }}</dd>
                </div>
              </dl>
              <div v-if="order.status !== 'CANCELLED'" class="order-history-payment-box">
                <div>
                  <span><i class="bi bi-credit-card-2-front" aria-hidden="true"></i>Phương thức</span>
                  <strong>{{ paymentMethodLabel(order.paymentMethod) }}</strong>
                </div>
                <div>
                  <span><i class="bi bi-shield-check" aria-hidden="true"></i>Thanh toán</span>
                  <strong>{{ paymentLabel(order.paymentStatus) }}</strong>
                </div>
              </div>
              <div v-if="canContinuePayment(order)" class="order-history-payment-cta">
                <div>
                  <strong>Đơn này chưa thanh toán</strong>
                  <span>Tiếp tục thanh toán để shop xử lý đơn nhanh hơn.</span>
                </div>
                <button
                  type="button"
                  class="btn btn-y2k-primary"
                  :disabled="continuingPaymentId === order.id"
                  @click="continuePayment(order)"
                >
                  <span v-if="continuingPaymentId === order.id" class="spinner-border spinner-border-sm me-2"></span>
                  Thanh toán
                </button>
              </div>
            </aside>
          </div>

          <div class="order-history-contact-grid">
            <div class="order-history-contact">
              <span class="order-history-contact__icon"><i class="bi bi-person" aria-hidden="true"></i></span>
              <div>
                <span>Người nhận</span>
                <strong>{{ order.fullName || order.userFullName || 'Chưa cập nhật' }}</strong>
                <small>{{ order.phone || order.userPhone || 'Chưa có số điện thoại' }}</small>
              </div>
            </div>
            <div class="order-history-contact order-history-contact--address">
              <span class="order-history-contact__icon"><i class="bi bi-geo-alt" aria-hidden="true"></i></span>
              <div>
                <span>Địa chỉ giao hàng</span>
                <strong>{{ order.addressLabel || 'Chưa cập nhật địa chỉ' }}</strong>
                <small v-if="order.addressId">Mã địa chỉ #{{ order.addressId }}</small>
              </div>
            </div>
            <div v-if="order.couponCode" class="order-history-contact order-history-contact--coupon">
              <span class="order-history-contact__icon"><i class="bi bi-ticket-perforated" aria-hidden="true"></i></span>
              <div>
                <span>Ưu đãi đã dùng</span>
                <strong>{{ order.couponCode }}</strong>
                <small>{{ order.promotionName || (order.promotionDiscountPercent ? `Giảm ${order.promotionDiscountPercent}%` : 'Đã áp dụng vào đơn') }}</small>
              </div>
            </div>
          </div>

          <footer class="order-history-card__footer">
            <span><i class="bi bi-info-circle" aria-hidden="true"></i> Mã đơn #{{ order.id }} · {{ statusDescription(order.status) }}</span>
            <button
              v-if="canDeleteOrder(order)"
              type="button"
              class="order-history-delete"
              :disabled="deletingOrderId === order.id"
              @click="deleteOrder(order)"
            >
              <i :class="deletingOrderId === order.id ? 'bi bi-hourglass-split' : 'bi bi-trash3'" aria-hidden="true"></i>
              {{ deletingOrderId === order.id ? 'Đang xóa...' : 'Xóa khỏi lịch sử' }}
            </button>
            <router-link to="/shop" class="order-history-continue">
              Mua sắm tiếp <i class="bi bi-arrow-up-right" aria-hidden="true"></i>
            </router-link>
          </footer>
        </article>
      </div>
    </section>

    <Teleport to="body">
    <div v-if="continuedPayment" class="order-history-payment-modal" role="dialog" aria-modal="true" aria-labelledby="order-history-payment-title" @click.self="closeContinuedPayment">
      <div class="order-history-payment-modal__panel">
        <button type="button" class="order-history-payment-modal__close" aria-label="Đóng thanh toán" @click="closeContinuedPayment">
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
        <span class="section-heading__eyebrow">Tiếp tục thanh toán</span>
        <h2 id="order-history-payment-title">Đơn #{{ continuedPayment.orderId }} · {{ paymentMethodLabel(continuedPayment.method) }}</h2>
        <p>{{ continuedPayment.message || 'Quét QR hoặc mở link để hoàn tất thanh toán.' }}</p>
        <div class="order-history-payment-modal__steps" aria-label="Các bước thanh toán">
          <span><i class="bi bi-qr-code-scan" aria-hidden="true"></i>Quét mã</span>
          <span><i class="bi bi-card-checklist" aria-hidden="true"></i>Đúng nội dung</span>
          <span><i class="bi bi-check2-circle" aria-hidden="true"></i>Cập nhật đơn</span>
        </div>
        <div class="order-history-payment-modal__body">
          <div v-if="continuedPaymentQrImageUrl" class="order-history-payment-modal__qr">
            <img :src="continuedPaymentQrImageUrl" alt="Mã QR thanh toán">
            <small>Quét mã bằng app ngân hàng.</small>
          </div>
          <div class="order-history-payment-modal__actions">
            <button type="button" class="btn btn-y2k-outline" @click="fetchOrders">
              <i class="bi bi-arrow-clockwise me-2"></i>Kiểm tra lại đơn
            </button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { formatPrice } from '../composables/useFormat'
import { useConfirmStore } from '../stores/confirm'
import { useToastStore } from '../stores/toast'

const orders = ref([])
const loading = ref(false)
const loadError = ref('')
const activeFilter = ref('ALL')
const orderHistoryPanel = ref(null)
const orderItemsById = ref({})
const itemLoadingById = ref({})
const itemErrorById = ref({})
const deletingOrderId = ref(null)
const continuingPaymentId = ref(null)
const continuedPayment = ref(null)
const confirmStore = useConfirmStore()
const toast = useToastStore()

const filters = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'PROCESSING', label: 'Chờ xử lý' },
  { value: 'PREPARING', label: 'Đang chuẩn bị' },
  { value: 'SHIPPED', label: 'Đang vận chuyển' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' }
]

const filteredOrders = computed(() => {
  if (activeFilter.value === 'ALL') return orders.value
  return orders.value.filter((order) => order.status === activeFilter.value)
})

const heroMetrics = computed(() => [
  { label: 'Chờ thanh toán', value: orders.value.filter((order) => canContinuePayment(order)).length },
  { label: 'Đang xử lý', value: orders.value.filter((order) => ['PROCESSING', 'PREPARING', 'SHIPPED'].includes(order.status)).length },
  { label: 'Đã giao', value: orders.value.filter((order) => order.status === 'DELIVERED').length }
])

const continuedPaymentQrImageUrl = computed(() => {
  const qrCode = continuedPayment.value?.qrCode || ''
  if (qrCode.startsWith('http://') || qrCode.startsWith('https://')) return qrCode
  const qrPayload = qrCode
  return qrPayload ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(qrPayload)}` : ''
})

const stats = computed(() => [
  { label: 'Chờ xử lý', value: orders.value.filter((order) => order.status === 'PROCESSING').length, icon: 'bi bi-arrow-repeat', filterValue: 'PROCESSING' },
  { label: 'Đang chuẩn bị', value: orders.value.filter((order) => order.status === 'PREPARING').length, icon: 'bi bi-box-seam', filterValue: 'PREPARING' },
  { label: 'Đang vận chuyển', value: orders.value.filter((order) => order.status === 'SHIPPED').length, icon: 'bi bi-truck', filterValue: 'SHIPPED' },
  { label: 'Đã giao', value: orders.value.filter((order) => order.status === 'DELIVERED').length, icon: 'bi bi-check2-circle', filterValue: 'DELIVERED' },
  { label: 'Đã hủy', value: orders.value.filter((order) => order.status === 'CANCELLED').length, icon: 'bi bi-x-circle', filterValue: 'CANCELLED' }
])

onMounted(fetchOrders)

async function fetchOrders() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await axios.get('/api/orders')
    orders.value = Array.isArray(data) ? data : []
    orderItemsById.value = {}
    itemErrorById.value = {}
    itemLoadingById.value = {}
    loading.value = false
    await Promise.allSettled(orders.value.map((order) => fetchOrderItems(order.id)))
  } catch (error) {
    orders.value = []
    loadError.value = error.response?.data?.message || 'Vui lòng thử lại sau ít phút.'
  } finally {
    loading.value = false
  }
}

async function fetchOrderItems(orderId) {
  itemLoadingById.value = { ...itemLoadingById.value, [orderId]: true }
  try {
    const { data } = await axios.get(`/api/orders/${orderId}/items`)
    orderItemsById.value = { ...orderItemsById.value, [orderId]: Array.isArray(data) ? data : [] }
  } catch {
    itemErrorById.value = { ...itemErrorById.value, [orderId]: 'Không tải được sản phẩm, vui lòng thử lại.' }
  } finally {
    itemLoadingById.value = { ...itemLoadingById.value, [orderId]: false }
  }
}

function filterCount(value) {
  if (value === 'ALL') return orders.value.length
  return orders.value.filter((order) => order.status === value).length
}

function selectStatFilter(filterValue) {
  if (!filterValue) return
  activeFilter.value = filterValue
  requestAnimationFrame(() => orderHistoryPanel.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
}

function orderItems(orderId) {
  return orderItemsById.value[orderId] || []
}

function orderItemCount(orderId) {
  return orderItems(orderId).reduce((total, item) => total + Number(item.quantity || 0), 0)
}

function orderSubtotal(order) {
  return orderItems(order.id).reduce((total, item) => total + Number(item.totalPrice || (Number(item.unitPrice || 0) * Number(item.quantity || 0))), 0)
}

function orderDiscount(order) {
  const subtotal = orderSubtotal(order)
  return Math.max(0, subtotal - Number(order.totalAmount || 0))
}

function hasDiscount(order) {
  return orderDiscount(order) > 0
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }) : 'Đơn hàng mới'
}

function statusLabel(status) {
  return ({ PROCESSING: 'Chờ xử lý', PREPARING: 'Đang chuẩn bị', SHIPPED: 'Đang vận chuyển', DELIVERED: 'Đã giao', CANCELLED: 'Đã hủy' })[status] || status || 'Đang cập nhật'
}

function statusDescription(status) {
  return ({ PROCESSING: 'Đơn đang chờ shop xử lý', PREPARING: 'Shop đang chuẩn bị hàng', SHIPPED: 'Đơn đang trên đường đến bạn', DELIVERED: 'Đơn đã giao thành công', CANCELLED: 'Đơn đã được hủy' })[status] || 'Đang cập nhật trạng thái'
}

function statusIcon(status) {
  return ({ PROCESSING: 'bi bi-hourglass-split', PREPARING: 'bi bi-box-seam', SHIPPED: 'bi bi-truck', DELIVERED: 'bi bi-check-circle', CANCELLED: 'bi bi-x-circle' })[status] || 'bi bi-info-circle'
}

function statusClass(status) {
  return ({ PROCESSING: 'status-pill--pending', PREPARING: 'status-pill--processing', SHIPPED: 'status-pill--shipped', DELIVERED: 'status-pill--delivered', CANCELLED: 'status-pill--cancelled' })[status] || 'status-pill--processing'
}

function paymentLabel(status) {
  return status === 'PAID' ? 'Đã thanh toán' : 'Chưa thanh toán'
}

function paymentClass(status) {
  return status === 'PAID' ? 'status-pill--delivered' : 'status-pill--pending'
}

function paymentMethodLabel(method) {
  const map = { COD: 'Thanh toán khi nhận hàng', BANKING: 'Thanh toán MB Bank', MOMO: 'Phương thức cũ', ZALOPAY: 'Phương thức cũ' }
  return map[String(method || '').toUpperCase()] || method || 'Chưa xác định'
}

function canContinuePayment(order) {
  const method = String(order?.paymentMethod || '').toUpperCase()
  return order?.status === 'PROCESSING' && order?.paymentStatus !== 'PAID' && method === 'BANKING'
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

function canDeleteOrder(order) {
  return ['DELIVERED', 'CANCELLED'].includes(order?.status)
}

async function deleteOrder(order) {
  const confirmed = await confirmStore.open({
    title: 'Xóa đơn khỏi lịch sử?',
    message: `Đơn #${order.id} đã giao hoặc đã hủy sẽ bị xóa khỏi lịch sử tài khoản. Thao tác này không thể hoàn tác.`,
    confirmText: 'Xóa đơn',
    cancelText: 'Giữ lại',
    variant: 'danger'
  })
  if (!confirmed || deletingOrderId.value) return

  deletingOrderId.value = order.id
  try {
    await axios.delete(`/api/orders/${order.id}`)
    orders.value = orders.value.filter((item) => item.id !== order.id)
    const nextItems = { ...orderItemsById.value }
    const nextLoading = { ...itemLoadingById.value }
    const nextErrors = { ...itemErrorById.value }
    delete nextItems[order.id]
    delete nextLoading[order.id]
    delete nextErrors[order.id]
    orderItemsById.value = nextItems
    itemLoadingById.value = nextLoading
    itemErrorById.value = nextErrors
    toast.success(`Đã xóa đơn #${order.id} khỏi lịch sử.`)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa đơn hàng này.')
  } finally {
    deletingOrderId.value = null
  }
}
</script>

<style scoped>
.order-history-page {
  position: relative;
  max-width: 1360px;
  margin: 0 auto;
  padding: 1.4rem 1rem 4rem;
  color: #0f172a;
}

.order-history-page::before {
  content: "";
  position: absolute;
  inset: 2rem auto auto -4rem;
  width: 240px;
  height: 240px;
  border-radius: 999px;
  background: rgba(14, 165, 233, .08);
  pointer-events: none;
  filter: blur(12px);
}

.order-history-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 1rem;
  align-items: stretch;
  padding: 1.35rem;
  border: 1px solid #dbe2ea;
  border-radius: 1.35rem;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, .06);
}

.order-history-hero__content {
  display: grid;
  align-content: start;
  gap: .45rem;
  min-width: 0;
}

.order-history-hero__content h1 {
  margin: .1rem 0 0;
  font-size: clamp(2rem, 4vw, 3.3rem);
  line-height: .98;
  letter-spacing: -.04em;
}

.order-history-hero__content p {
  max-width: 760px;
  color: #475569;
  line-height: 1.7;
}

.order-history-hero__aside {
  display: grid;
  gap: .8rem;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 1.1rem;
  background: #f8fafc;
}

.order-history-hero__snapshot {
  display: grid;
  gap: .2rem;
  padding: 1rem;
  border-radius: .95rem;
  background: #0f172a;
  color: #fff;
}

.order-history-hero__eyebrow {
  color: rgba(226, 232, 240, .72);
  text-transform: uppercase;
  letter-spacing: .12em;
  font-size: .68rem;
  font-weight: 900;
}

.order-history-hero__snapshot strong {
  font-size: 2.2rem;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.order-history-hero__snapshot span:last-child {
  color: rgba(226, 232, 240, .82);
  font-size: .82rem;
}

.order-history-hero__mini-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: .55rem;
}

.order-history-hero__mini-grid div {
  display: grid;
  gap: .15rem;
  padding: .75rem;
  border: 1px solid #e2e8f0;
  border-radius: .9rem;
  background: #fff;
}

.order-history-hero__mini-grid span {
  color: #64748b;
  font-size: .7rem;
}

.order-history-hero__mini-grid strong {
  font-size: 1.2rem;
  font-variant-numeric: tabular-nums;
}

.order-history-hero__action {
  width: 100%;
  justify-content: center;
}

.order-history-stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: .85rem;
  margin-top: 1rem;
}

.order-history-stat {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: .8rem;
  align-items: center;
  min-height: 96px;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 1rem;
  background: #fff;
  box-shadow: 0 10px 22px rgba(15, 23, 42, .04);
}

.order-history-stat--action {
  cursor: pointer;
  transition: transform .18s ease, border-color .18s ease, box-shadow .18s ease;
}

.order-history-stat--action:hover {
  transform: translateY(-1px);
  border-color: #0ea5e9;
  box-shadow: 0 14px 28px rgba(14, 165, 233, .08);
}

.order-history-stat.active {
  border-color: #0ea5e9;
  box-shadow: inset 0 0 0 1px rgba(14, 165, 233, .12);
}

.order-history-stat__icon {
  display: grid;
  place-items: center;
  size: 42px;
  width: 42px;
  height: 42px;
  border-radius: .9rem;
  background: #e0f2fe;
  color: #0284c7;
}

.order-history-stat__icon i {
  font-size: 1.1rem;
}

.order-history-stat span {
  color: #64748b;
  font-size: .76rem;
}

.order-history-stat strong {
  display: block;
  margin-top: .1rem;
  font-size: 1.6rem;
  font-variant-numeric: tabular-nums;
}

.order-history-panel {
  margin-top: 1rem;
  padding: 1.15rem;
  border: 1px solid #dbe2ea;
  border-radius: 1.35rem;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, .05);
}

.order-history-toolbar {
  display: grid;
  gap: 1rem;
  margin-bottom: 1rem;
}

.order-history-toolbar h2 {
  margin: .15rem 0 0;
  font-size: 1.45rem;
  letter-spacing: -.03em;
}

.order-history-toolbar__hint {
  margin-top: .2rem;
  color: #64748b;
}

.order-history-filters {
  display: flex;
  flex-wrap: wrap;
  gap: .6rem;
}

.order-filter-chip {
  display: inline-flex;
  align-items: center;
  gap: .45rem;
  min-height: 42px;
  padding: .55rem .8rem;
  border: 1px solid #dbe2ea;
  border-radius: 999px;
  background: #fff;
  color: #334155;
  white-space: nowrap;
  transition: transform .18s ease, border-color .18s ease, background .18s ease, color .18s ease;
}

.order-filter-chip:hover {
  transform: translateY(-1px);
  border-color: #0ea5e9;
}

.order-filter-chip.active {
  border-color: transparent;
  background: #0f172a;
  color: #fff;
  box-shadow: 0 10px 20px rgba(15, 23, 42, .12);
}

.order-filter-chip span {
  display: inline-grid;
  place-items: center;
  min-width: 22px;
  height: 22px;
  padding: 0 .35rem;
  border-radius: 999px;
  background: rgba(14, 165, 233, .12);
  color: inherit;
  font-size: .72rem;
  font-variant-numeric: tabular-nums;
}

.order-filter-chip.active span {
  background: rgba(255,255,255,.18);
}

.order-history-loading,
.order-history-empty {
  display: grid;
  place-items: center;
  gap: .75rem;
  min-height: 280px;
  padding: 1rem;
  text-align: center;
}

.order-history-empty i {
  color: #0ea5e9;
  font-size: 2rem;
}

.order-history-list {
  display: grid;
  gap: 1rem;
}

.order-history-card {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 1.15rem;
  background: #fff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, .04);
}

.order-history-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.order-history-card__identity {
  min-width: 0;
}

.order-history-card__eyebrow,
.order-history-section__eyebrow,
.order-history-hero__content .section-heading__eyebrow {
  display: inline-flex;
  align-items: center;
  color: #0ea5e9;
  text-transform: uppercase;
  letter-spacing: .12em;
  font-size: .68rem;
  font-weight: 900;
}

.order-history-card__identity h3 {
  margin: .15rem 0 .15rem;
  font-size: 1.25rem;
}

.order-history-card__subline {
  color: #64748b;
  font-size: .8rem;
}

.order-history-card__badges {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: .45rem;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  min-height: 32px;
  padding: .35rem .7rem;
  border-radius: 999px;
  font-size: .72rem;
  font-weight: 900;
}

.order-history-card__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 1rem;
}

.order-history-section {
  min-width: 0;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 1rem;
  background: #f8fafc;
}

.order-history-section__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: .9rem;
}

.order-history-section__head h4 {
  margin: .15rem 0 0;
  font-size: 1.05rem;
}

.order-history-items-loading,
.order-history-items-error {
  display: grid;
  gap: .5rem;
  min-height: 88px;
}

.order-history-item-list {
  display: grid;
  gap: .65rem;
}

.order-history-item {
  display: grid;
  grid-template-columns: 60px minmax(0, 1fr) auto;
  gap: .7rem;
  align-items: center;
  padding: .65rem;
  border: 1px solid #e2e8f0;
  border-radius: .95rem;
  background: #fff;
}

.order-history-item img {
  width: 60px;
  height: 64px;
  border-radius: .75rem;
  object-fit: cover;
  background: #f1f5f9;
}

.order-history-item__copy strong,
.order-history-item__copy span,
.order-history-item__copy small {
  display: block;
}

.order-history-item__copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-history-item__copy span,
.order-history-item__copy small {
  color: #64748b;
  font-size: .76rem;
}

.order-history-item__total {
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.order-history-summary {
  display: grid;
  gap: .55rem;
}

.order-history-summary div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: .75rem .8rem;
  border: 1px solid #e2e8f0;
  border-radius: .85rem;
  background: #fff;
}

.order-history-summary dt,
.order-history-summary dd {
  margin: 0;
}

.order-history-summary dd {
  font-variant-numeric: tabular-nums;
  font-weight: 900;
}

.order-history-summary__grand {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.order-history-payment-box {
  display: grid;
  gap: .65rem;
  margin-top: 1rem;
  padding-top: .9rem;
  border-top: 1px solid #e2e8f0;
}

.order-history-payment-box div {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: .75rem;
  font-size: .76rem;
}

.order-history-payment-box span {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  color: #64748b;
}

.order-history-payment-box strong {
  color: #0f172a;
  text-align: right;
}

.order-history-payment-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1rem;
  padding: 1rem;
  border: 1px solid #bae6fd;
  border-radius: 1rem;
  background: #f0f9ff;
}

.order-history-payment-cta div {
  display: grid;
  gap: .25rem;
}

.order-history-payment-cta strong {
  color: #0f172a;
  font-weight: 900;
}

.order-history-payment-cta span {
  color: #475569;
  font-size: .82rem;
}

.order-history-contact-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: .85rem;
  margin-top: 1rem;
}

.order-history-contact {
  display: flex;
  gap: .75rem;
  padding: .9rem;
  border: 1px solid #e2e8f0;
  border-radius: 1rem;
  background: #fff;
}

.order-history-contact__icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  border-radius: .85rem;
  background: #e0f2fe;
  color: #0284c7;
}

.order-history-contact strong {
  display: block;
  margin-top: .15rem;
}

.order-history-contact small {
  color: #64748b;
}

.order-history-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .75rem;
  margin-top: 1rem;
  padding-top: .9rem;
  border-top: 1px solid #e2e8f0;
  flex-wrap: wrap;
}

.order-history-card__footer > span {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  color: #64748b;
  font-size: .82rem;
}

.order-history-delete,
.order-history-continue {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  min-height: 40px;
  padding: .45rem .8rem;
  border-radius: .85rem;
  border: 1px solid #dbe2ea;
  background: #fff;
}

.order-history-delete {
  color: #b91c1c;
}

.order-history-continue {
  color: #0f172a;
  text-decoration: none;
}

.order-history-payment-modal {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: transparent;
  backdrop-filter: none;
}

.order-history-payment-modal__panel {
  position: relative;
  overflow: hidden;
  width: min(680px, 100%);
  padding: 1.4rem;
  border: 1px solid rgba(125, 211, 252, .7);
  border-radius: 1.25rem;
  background:
    radial-gradient(circle at top right, rgba(8,145,178,.14), transparent 34%),
    linear-gradient(135deg, rgba(255,255,255,.98), rgba(240,249,255,.95));
  box-shadow: 0 24px 80px rgba(15,23,42,.25), inset 0 4px 0 #0891b2;
}

.order-history-payment-modal__close {
  position: absolute;
  top: .8rem;
  right: .8rem;
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: 1px solid #d4d4d8;
  border-radius: .75rem;
  background: #fff;
  color: #18181b;
  cursor: pointer;
}

.order-history-payment-modal__panel h2 {
  margin: .4rem 2.5rem .4rem 0;
  font-size: 1.35rem;
}

.order-history-payment-modal__panel p {
  max-width: 560px;
  color: #1e293b;
  font-weight: 800;
  line-height: 1.65;
}

.order-history-payment-modal__steps {
  display: flex;
  flex-wrap: wrap;
  gap: .5rem;
  margin-top: .8rem;
}

.order-history-payment-modal__steps span {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  min-height: 34px;
  padding: .35rem .65rem;
  border: 1px solid #bae6fd;
  border-radius: 999px;
  background: rgba(255,255,255,.9);
  color: #0f172a;
  font-size: .76rem;
  font-weight: 900;
  white-space: nowrap;
}

.order-history-payment-modal__steps i { color: #0891b2; }

.order-history-payment-modal__body {
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr);
  gap: 1rem;
  align-items: center;
  margin-top: 1rem;
}

.order-history-payment-modal__qr {
  display: grid;
  justify-items: center;
  gap: .5rem;
  padding: .95rem;
  border: 1px solid #bae6fd;
  border-radius: 1rem;
  background: #fff;
  box-shadow: 0 12px 26px rgba(15, 23, 42, .08);
}

.order-history-payment-modal__qr img {
  width: 180px;
  height: 180px;
  object-fit: contain;
  border-radius: .75rem;
}

.order-history-payment-modal__qr small {
  color: #334155;
  text-align: center;
  font-weight: 700;
}

.order-history-payment-modal__actions {
  display: grid;
  gap: .7rem;
}

@media (max-width: 1200px) {
  .order-history-hero,
  .order-history-card__content {
    grid-template-columns: 1fr;
  }

  .order-history-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 920px) {
  .order-history-stats,
  .order-history-contact-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .order-history-payment-cta {
    align-items: stretch;
    flex-direction: column;
  }

  .order-history-payment-modal__body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .order-history-page {
    padding-inline: .85rem;
  }

  .order-history-hero__mini-grid,
  .order-history-stats,
  .order-history-contact-grid {
    grid-template-columns: 1fr;
  }

  .order-history-card__top,
  .order-history-card__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .order-history-card__badges {
    justify-content: flex-start;
  }

  .order-history-item {
    grid-template-columns: 52px minmax(0, 1fr);
  }

  .order-history-item__total {
    grid-column: 2;
  }
}

</style>

<style scoped>
.order-history-page {
  width: min(1400px, calc(100% - 40px));
  max-width: none;
  gap: 1rem;
  padding-top: 1.25rem;
  padding-bottom: 4.5rem;
}

.order-history-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  padding: 1.35rem 0 1.15rem;
  border: 0;
  border-bottom: 1px solid #dbeafe;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.order-history-hero__action {
  flex: 0 0 auto;
  min-height: 46px;
  white-space: nowrap;
  width: auto;
}

.order-history-stats {
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: .8rem;
}

.order-history-stat {
  min-height: 92px;
  padding: .9rem 1rem;
  border-color: #e4e4e7;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(20, 20, 20, .05);
}

.order-history-panel {
  padding: clamp(1rem, 2vw, 1.25rem);
  border-color: #e4e4e7;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(20, 20, 20, .06);
}

.order-history-toolbar {
  align-items: flex-start;
  padding-bottom: 1.1rem;
}

.order-history-card {
  display: grid !important;
  grid-template-columns: minmax(180px, .52fr) minmax(0, 1.38fr) minmax(290px, .74fr);
  grid-template-areas:
    "meta items summary"
    "meta contacts summary"
    "footer footer footer";
  align-items: stretch;
  overflow: hidden;
  border-color: #d9e0e5;
  background: #fff;
  box-shadow: 6px 6px 0 rgba(34, 211, 238, 0.58), 0 18px 42px rgba(24, 24, 27, 0.075);
}

.order-history-card__top {
  grid-area: meta;
  display: grid;
  align-content: space-between;
  min-height: 100%;
  padding: 1.25rem;
  border-right: 1px solid #dfe4e8;
  border-bottom: 0;
  background:
    linear-gradient(180deg, rgba(236, 254, 255, 0.9), rgba(255, 255, 255, 0.96) 52%, rgba(253, 242, 248, 0.86)),
    #fff;
}

.order-history-card__content {
  display: contents;
  padding: 0;
}

.order-history-section--items {
  grid-area: items;
  min-width: 0;
  padding: 1.25rem 1.35rem 1rem;
}

.order-history-section--summary {
  grid-area: summary;
  display: flex;
  flex-direction: column;
  margin: 1rem 1rem 1rem 0;
  padding: 1.15rem;
  border-color: #9fe8f5;
  border-radius: 8px;
  background:
    linear-gradient(180deg, #f6fdff 0%, #ffffff 54%, #fff7fb 100%);
  box-shadow: inset 0 4px 0 #22d3ee;
}

.order-history-section--summary .order-history-section__head {
  padding-bottom: .8rem;
  border-bottom: 1px solid #dbeafe;
}

.order-history-contact-grid {
  grid-area: contacts;
  grid-template-columns: minmax(0, .9fr) minmax(0, 1.45fr) minmax(0, .8fr);
  gap: .7rem;
  padding: 0 1.35rem 1.25rem;
}

.order-history-card__footer {
  grid-area: footer;
  padding: .9rem 1.15rem;
  border-top: 1px solid #dfe4e8;
  background: linear-gradient(90deg, #ffffff, #fbfdff);
}

@media (max-width: 1180px) {
  .order-history-card {
    grid-template-columns: minmax(160px, .5fr) minmax(0, 1fr);
    grid-template-areas:
      "meta summary"
      "items items"
      "contacts contacts"
      "footer footer";
  }

  .order-history-section--summary {
    margin: 1rem 1rem 1rem 0;
  }

  .order-history-section--items {
    padding-top: .2rem;
  }
}
</style>
