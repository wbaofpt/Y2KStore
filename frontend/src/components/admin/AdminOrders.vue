<template>
  <div class="admin-section admin-orders">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Order control</span>
        <h4 class="admin-page-title mb-0">Quản lý đơn hàng</h4>
        <p>Theo dõi trạng thái đơn, thanh toán, mã giảm giá và chi tiết sản phẩm trong từng đơn.</p>
      </div>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card admin-order-stat"><span class="admin-order-stat__icon"><i class="bi bi-box-seam"></i></span><div><div class="admin-stat-label">Tổng đơn</div><div class="admin-stat-value">{{ orders.length }}</div><div class="admin-stat-meta">Tất cả đơn hàng</div></div></div>
      <div class="admin-stat-card admin-order-stat"><span class="admin-order-stat__icon"><i class="bi bi-arrow-repeat"></i></span><div><div class="admin-stat-label">Đang xử lý</div><div class="admin-stat-value">{{ processingCount }}</div><div class="admin-stat-meta">Chờ xử lý, chuẩn bị và giao</div></div></div>
      <div class="admin-stat-card admin-order-stat"><span class="admin-order-stat__icon"><i class="bi bi-check2-circle"></i></span><div><div class="admin-stat-label">Đã hoàn tất</div><div class="admin-stat-value">{{ completedCount }}</div><div class="admin-stat-meta">DELIVERED</div></div></div>
      <div class="admin-stat-card admin-order-stat"><span class="admin-order-stat__icon"><i class="bi bi-wallet2"></i></span><div><div class="admin-stat-label">Doanh thu</div><div class="admin-stat-value">{{ formatPrice(totalAmount) }}</div><div class="admin-stat-meta">Theo danh sách hiện tại</div></div></div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm mã đơn, tên, SĐT, coupon..." style="max-width: 340px;">
      <select v-model="statusFilter" class="form-select form-select-sm" style="max-width: 190px;">
        <option value="">Tất cả trạng thái</option>
        <option v-for="(status, key) in orderStatusMap" :key="key" :value="key">{{ status.label }}</option>
      </select>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách đơn hàng</strong><span>{{ filteredOrders.length }} kết quả</span></div>
        <span class="admin-db-chip">orders</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="order in filteredOrders" :key="order.id" class="admin-entity-row admin-order-row">
          <div class="admin-row-icon"><i class="bi bi-cart3"></i></div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              Đơn #{{ order.id }}
              <span class="badge" :class="statusBadgeClass(order.status)">{{ orderStatusMap[order.status]?.label || order.status }}</span>
              <span v-if="getCouponCode(order)" class="badge bg-warning text-dark">{{ getCouponCode(order) }}</span>
            </div>
            <div class="admin-row-sub">{{ order.fullName || `User #${order.userId ?? '-'}` }} · {{ order.phone || 'Chưa có SĐT' }}</div>
            <div class="admin-order-products"><i class="bi bi-bag-check"></i><span>{{ getOrderItemNames(order.id) || 'Đang tải tên sản phẩm...' }}</span></div>
            <div class="admin-row-note">{{ formatDate(order.orderDate) }}</div>
          </div>
          <div class="admin-row-meta">
            <span>Tổng: <strong class="text-y2k-sale">{{ formatPrice(order.totalAmount) }}</strong></span>
            <span>TT: <strong>{{ order.paymentMethod || 'N/A' }} / {{ order.paymentStatus || 'UNPAID' }}</strong></span>
            <select class="form-select form-select-sm" :value="order.status" @change="updateStatus(order.id, $event.target.value)">
              <option v-for="(status, key) in orderStatusMap" :key="key" :value="key">{{ status.label }}</option>
            </select>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click.stop="viewDetail(order)"><i class="bi bi-eye"></i></button>
            <button class="btn btn-sm btn-outline-dark" title="Xem hóa đơn" @click.stop="viewInvoice(order)"><i class="bi bi-receipt"></i></button>
            <button class="btn btn-sm btn-y2k-primary" title="In hóa đơn" @click.stop="printInvoice(order)"><i class="bi bi-printer"></i></button>
          </div>
        </article>
        <div v-if="!filteredOrders.length" class="admin-empty-state">
          <i class="bi bi-cart3"></i>
          <strong>Không có đơn hàng phù hợp</strong>
          <span>Thử đổi từ khóa hoặc trạng thái.</span>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="modal">
        <div v-if="selectedOrder" class="y2k-modal-backdrop" @click.self="selectedOrder = null">
          <div class="y2k-modal text-start" style="max-width: 640px;">
            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
              <div>
                <span class="admin-kicker">Order detail</span>
                <h5 class="fw-bold mb-1">Chi tiết đơn #{{ selectedOrder.id }}</h5>
                <p class="text-secondary small mb-0">{{ selectedOrder.fullName || `User #${selectedOrder.userId ?? '-'}` }} · {{ selectedOrder.phone || '—' }}</p>
              </div>
              <button class="btn btn-y2k-outline btn-sm" @click="selectedOrder = null">Đóng</button>
            </div>

            <div class="admin-invoice-actions" :class="{ 'admin-invoice-actions--pending': !isPaid(selectedOrder) }">
              <span><i class="bi" :class="isPaid(selectedOrder) ? 'bi-check-circle-fill' : 'bi-clock-history'"></i> {{ isPaid(selectedOrder) ? 'Đơn đã thanh toán — có thể xuất hóa đơn' : 'Đơn chưa thanh toán — vẫn có thể xuất hóa đơn tạm' }}</span>
              <button class="btn btn-y2k-primary btn-sm" @click="printInvoice(selectedOrder)"><i class="bi bi-printer me-1"></i>In hóa đơn</button>
            </div>

            <div class="d-flex gap-2 flex-wrap mb-3">
              <span class="badge bg-light text-dark border">PTTT: {{ selectedOrder.paymentMethod || 'N/A' }}</span>
              <span :class="selectedOrder.paymentStatus === 'PAID' ? 'badge bg-success' : 'badge bg-secondary'">
                {{ selectedOrder.paymentStatus || 'UNPAID' }}
              </span>
              <span v-if="getCouponCode(selectedOrder)" class="badge bg-warning text-dark border">Mã: {{ getCouponCode(selectedOrder) }}</span>
            </div>

            <div v-if="getPromotionName(selectedOrder)" class="small text-secondary mb-3">
              Ưu đãi: <span class="fw-semibold text-dark">{{ getPromotionName(selectedOrder) }}</span>
              <span v-if="getCouponDiscount(selectedOrder)"> · Giảm {{ getCouponDiscount(selectedOrder) }}%</span>
            </div>

            <div v-if="loadingItems" class="text-center py-3"><div class="spinner-border spinner-border-sm"></div></div>
            <div v-else class="admin-mini-list mb-3">
              <div v-for="item in orderItems" :key="item.id" class="admin-mini-list__item">
                <div class="d-flex align-items-center gap-2">
                  <img v-if="item.productImage" :src="item.productImage" class="rounded border" style="width:42px;height:42px;object-fit:cover">
                  <div>
                    <router-link v-if="item.productName" :to="productPath({ name: item.productName })" class="admin-order-product-link">{{ item.productName }}</router-link>
                    <div v-else class="fw-semibold text-secondary">Sản phẩm đã xóa</div>
                    <div v-if="item.variantLabel" class="text-secondary small">{{ item.variantLabel }}</div>
                  </div>
                </div>
                <div class="text-end">
                  <div class="small text-secondary">SL: {{ item.quantity }}</div>
                  <div class="fw-bold">{{ formatPrice(item.unitPrice) }}</div>
                </div>
              </div>
              <div v-if="!orderItems.length" class="text-secondary small">Không có sản phẩm trong đơn.</div>
            </div>

            <div class="d-flex justify-content-between fw-bold border-top pt-2">
              <span>Tổng cộng</span>
              <span class="text-y2k-sale">{{ formatPrice(selectedOrder.totalAmount) }}</span>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="modal">
        <div v-if="invoiceOrder" class="y2k-modal-backdrop" @click.self="invoiceOrder = null">
          <div class="admin-invoice-sheet">
            <div class="admin-invoice-sheet__topbar">
              <span class="admin-kicker">Payment receipt</span>
              <div class="admin-invoice-sheet__actions">
                <button class="btn btn-y2k-primary btn-sm" @click="printInvoice(invoiceOrder)"><i class="bi bi-printer me-1"></i>In hóa đơn</button>
                <button class="btn btn-y2k-outline btn-sm" @click="invoiceOrder = null">Đóng</button>
              </div>
            </div>
            <div class="admin-invoice-paper">
              <div class="admin-invoice-paper__header">
                <div><div class="admin-invoice-paper__brand">Y2K STORE</div><p>DISCOVER YOUR STYLE<br><span>Hóa đơn điện tử · {{ isPaid(invoiceOrder) ? 'Đã thanh toán' : 'Chưa thanh toán' }}</span></p></div>
                <div class="admin-invoice-paper__code"><strong>HÓA ĐƠN #{{ invoiceOrder.id }}</strong><span>{{ formatDate(invoiceOrder.orderDate) }}</span><b :class="{ 'admin-invoice-paper__status--pending': !isPaid(invoiceOrder) }"><i class="bi" :class="isPaid(invoiceOrder) ? 'bi-check-circle-fill' : 'bi-clock-history'"></i> {{ isPaid(invoiceOrder) ? 'ĐÃ THANH TOÁN' : 'CHƯA THANH TOÁN' }}</b></div>
              </div>
              <div class="admin-invoice-paper__info">
                <div><small>KHÁCH HÀNG</small><strong>{{ invoiceOrder.fullName || `User #${invoiceOrder.userId || '-'}` }}</strong><span>{{ invoiceOrder.phone || 'Chưa có số điện thoại' }}</span></div>
                <div><small>THANH TOÁN</small><strong>{{ invoiceOrder.paymentMethod || 'Không xác định' }}</strong><span>Trạng thái: {{ isPaid(invoiceOrder) ? 'Đã thanh toán' : 'Chưa thanh toán' }}</span><span>Mã giảm giá: {{ getCouponCode(invoiceOrder) || 'Không có' }}</span></div>
                <div><small>GIAO HÀNG</small><strong>{{ invoiceOrder.fullName || `User #${invoiceOrder.userId || '-'}` }}</strong><span>{{ invoiceOrder.phone || 'Chưa có số điện thoại' }}</span><span>{{ invoiceOrder.addressLabel || 'Chưa có địa chỉ giao hàng' }}</span></div>
              </div>
              <div v-if="loadingItems" class="admin-invoice-loading"><span class="spinner-border spinner-border-sm"></span> Đang tải chi tiết hóa đơn...</div>
              <table v-else class="admin-invoice-table"><thead><tr><th>Sản phẩm</th><th>Biến thể</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead><tbody><tr v-for="item in orderItems" :key="item.id"><td><strong>{{ item.productName || 'Sản phẩm đã xóa' }}</strong></td><td>{{ item.variantLabel || '—' }}</td><td>{{ item.quantity }}</td><td>{{ formatPrice(item.unitPrice) }}</td><td><strong>{{ formatPrice(Number(item.unitPrice || 0) * Number(item.quantity || 0)) }}</strong></td></tr><tr v-if="!orderItems.length"><td colspan="5" class="text-center">Chưa có sản phẩm trong hóa đơn.</td></tr></tbody></table>
              <div class="admin-invoice-paper__summary"><div><span>Tạm tính</span><strong>{{ formatPrice(invoiceSubtotal) }}</strong></div><div><span>Khuyến mãi / giảm giá</span><strong>{{ getCouponDiscount(invoiceOrder) ? `${getCouponDiscount(invoiceOrder)}%` : 'Đã áp dụng trong tổng đơn' }}</strong></div><div class="admin-invoice-paper__grand"><span>Tổng thanh toán</span><strong>{{ formatPrice(invoiceOrder.totalAmount) }}</strong></div></div>
              <div class="admin-invoice-paper__footer"><i class="bi bi-stars"></i><div><strong>Cảm ơn bạn đã mua sắm tại Y2K Store!</strong><span>Vui lòng giữ hóa đơn để đối chiếu khi cần hỗ trợ đơn hàng.</span></div></div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { formatPrice, formatDate, orderStatusMap } from '../../composables/useFormat'
import { productPath } from '../../composables/useCatalog'
import { useToastStore } from '../../stores/toast'

const toast = useToastStore()
const props = defineProps({
  initialStatus: {
    type: String,
    default: ''
  }
})
const orders = ref([])
const orderItemsByOrder = ref({})
const search = ref('')
const statusFilter = ref(props.initialStatus)
const selectedOrder = ref(null)
const invoiceOrder = ref(null)
const orderItems = ref([])
const loadingItems = ref(false)

const filteredOrders = computed(() => {
  const q = search.value.trim().toLowerCase()
  return orders.value.filter((order) => {
    const matchStatus = !statusFilter.value || order.status === statusFilter.value
    const matchSearch = !q ||
      String(order.id).includes(q) ||
      order.fullName?.toLowerCase().includes(q) ||
      order.phone?.includes(q) ||
      order.couponCode?.toLowerCase().includes(q)
    return matchStatus && matchSearch
  })
})

const processingCount = computed(() => orders.value.filter((order) => ['PROCESSING', 'PREPARING', 'SHIPPED'].includes(order.status)).length)
const completedCount = computed(() => orders.value.filter((order) => order.status === 'DELIVERED').length)
const totalAmount = computed(() => filteredOrders.value.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0))
const invoiceSubtotal = computed(() => orderItems.value.reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0))

onMounted(fetchOrders)

watch(() => props.initialStatus, (value) => {
  statusFilter.value = value || ''
})

async function fetchOrders() {
  try {
    const { data } = await axios.get('/api/admin/orders')
    orders.value = data
    await Promise.all(orders.value.map(async (order) => {
      try {
        const response = await axios.get(`/api/admin/orders/${order.id}/items`)
        orderItemsByOrder.value[order.id] = response.data || []
      } catch {
        orderItemsByOrder.value[order.id] = []
      }
    }))
  } catch (error) {
    console.error(error)
  }
}

function getOrderItemNames(orderId) {
  return (orderItemsByOrder.value[orderId] || []).map((item) => item.productName || 'Sản phẩm đã xóa').join(' · ')
}

async function updateStatus(id, status) {
  try {
    await axios.put(`/api/admin/orders/${id}/status`, { status })
    toast.success('Đã cập nhật trạng thái đơn hàng!')
    fetchOrders()
  } catch (error) {
    toast.error('Không thể cập nhật trạng thái!')
  }
}

async function viewDetail(order) {
  selectedOrder.value = order
  await loadOrderItems(order)
}

async function loadOrderItems(order) {
  loadingItems.value = true
  try {
    const { data } = await axios.get(`/api/admin/orders/${order.id}/items`)
    orderItems.value = data
  } catch (error) {
    orderItems.value = []
  } finally {
    loadingItems.value = false
  }
}

function viewInvoice(order) {
  invoiceOrder.value = order
  loadOrderItems(order)
}

async function printInvoice(order) {
  if (selectedOrder.value?.id !== order.id || !orderItems.value.length) {
    selectedOrder.value = order
    await loadOrderItems(order)
  }
  const lines = orderItems.value.map((item) => {
    const quantity = Number(item.quantity || 0)
    const unitPrice = Number(item.unitPrice || 0)
    return `<tr><td>${escapeHtml(item.productName || 'Sản phẩm')}</td><td>${escapeHtml(item.variantLabel || '')}</td><td class="num">${quantity}</td><td class="num">${formatPrice(unitPrice)}</td><td class="num">${formatPrice(quantity * unitPrice)}</td></tr>`
  }).join('')
  const popup = window.open('', '_blank', 'width=900,height=700')
  if (!popup) {
    toast.error('Trình duyệt đang chặn cửa sổ in hóa đơn.')
    return
  }
  const paymentLabel = isPaid(order) ? 'ĐÃ THANH TOÁN' : 'CHƯA THANH TOÁN'
  popup.document.write(`<!doctype html><html lang="vi"><head><meta charset="utf-8"><title>Hóa đơn #${order.id}</title><style>
    *{box-sizing:border-box}body{margin:0;padding:32px;font-family:Arial,sans-serif;color:#18181b;background:#fff}main{max-width:820px;margin:auto}.top{display:flex;justify-content:space-between;gap:24px;border-bottom:3px solid #111;padding-bottom:20px}.brand{font-size:25px;font-weight:900;letter-spacing:.08em}.muted{color:#71717a;font-size:13px;line-height:1.55}.title{text-align:right}.title h1{margin:0 0 5px;font-size:28px}.meta{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin:24px 0}.box{padding:14px;border:1px solid #e4e4e7;border-radius:10px}.box strong{display:block;margin-bottom:6px}.label{color:#71717a;font-size:12px;text-transform:uppercase;letter-spacing:.08em;margin-bottom:5px}table{width:100%;border-collapse:collapse;margin-top:22px}th,td{padding:11px 8px;border-bottom:1px solid #e4e4e7;text-align:left;font-size:13px}th{background:#f4f4f5;font-size:11px;text-transform:uppercase}.num{text-align:right}.total{margin:22px 0 0 auto;width:300px}.total div{display:flex;justify-content:space-between;padding:7px 0}.grand{border-top:2px solid #111;margin-top:7px;padding-top:12px!important;font-size:18px;font-weight:900}.paid{display:inline-block;margin-top:18px;padding:7px 12px;border:1px solid #16a34a;border-radius:999px;color:#15803d;font-weight:800;font-size:12px}.unpaid{border-color:#f59e0b;color:#b45309}@media print{body{padding:0}main{max-width:none}}
  </style></head><body><main><div class="top"><div><div class="brand">Y2K STORE</div><div class="muted">DISCOVER YOUR STYLE<br>Hóa đơn thanh toán</div></div><div class="title"><h1>HÓA ĐƠN #${order.id}</h1><div class="muted">${escapeHtml(formatDate(order.orderDate))}</div><span class="paid ${isPaid(order) ? '' : 'unpaid'}">${paymentLabel}</span></div></div><div class="meta"><div class="box"><div class="label">Người nhận hàng</div><strong>${escapeHtml(order.fullName || `User #${order.userId || '-'}`)}</strong><div class="muted">${escapeHtml(order.phone || 'Chưa có số điện thoại')}</div><div class="muted">${escapeHtml(order.addressLabel || 'Chưa có địa chỉ giao hàng')}</div></div><div class="box"><div class="label">Thông tin thanh toán</div><strong>${escapeHtml(order.paymentMethod || 'Không xác định')}</strong><div class="muted">Trạng thái: ${paymentLabel}</div><div class="muted">Mã giảm giá: ${escapeHtml(getCouponCode(order) || 'Không có')}</div></div></div><table><thead><tr><th>Sản phẩm</th><th>Biến thể</th><th class="num">SL</th><th class="num">Đơn giá</th><th class="num">Thành tiền</th></tr></thead><tbody>${lines || '<tr><td colspan="5">Không có sản phẩm</td></tr>'}</tbody></table><div class="total"><div><span>Tạm tính</span><strong>${formatPrice(orderItems.value.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0))}</strong></div><div><span>Giảm giá</span><strong>${escapeHtml(getCouponDiscount(order) ? `${getCouponDiscount(order)}%` : 'Theo đơn')}</strong></div><div class="grand"><span>Tổng thanh toán</span><strong>${formatPrice(order.totalAmount)}</strong></div></div><p class="muted" style="margin-top:40px;text-align:center">Cảm ơn bạn đã mua sắm tại Y2K Store.</p></main><script>window.onload=()=>{window.print()}<\/script></body></html>`)
  popup.document.close()
}

function isPaid(order) {
  return String(order?.paymentStatus || '').trim().toUpperCase() === 'PAID'
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character])
}

function statusBadgeClass(status) {
  const map = {
    PROCESSING: 'bg-warning text-dark',
    PREPARING: 'bg-info text-dark',
    SHIPPED: 'bg-primary',
    DELIVERED: 'bg-success',
    CANCELLED: 'bg-danger'
  }
  return map[status] || 'bg-secondary'
}

function getCouponCode(order) {
  return order?.couponCode || ''
}

function getPromotionName(order) {
  return order?.promotionName || ''
}

function getCouponDiscount(order) {
  const value = order?.couponDiscountValue ?? order?.promotionDiscountPercent
  return value !== undefined && value !== null && value !== '' ? value : ''
}

defineExpose({ refresh: fetchOrders })
</script>

<style scoped>
.y2k-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(20, 20, 20, 0.45);
  backdrop-filter: blur(4px);
}

.y2k-modal {
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  width: 100%;
  border: 2px solid var(--y2k-dark);
  box-shadow: 8px 8px 0 var(--y2k-dark);
}

.admin-invoice-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .8rem;
  margin: 0 0 1rem;
  padding: .75rem .9rem;
  border: 1px solid #bbf7d0;
  border-radius: .75rem;
  background: #f0fdf4;
  color: #166534;
  font-size: .78rem;
  font-weight: 700;
}

.admin-invoice-actions i { margin-right: .3rem; }
.admin-invoice-actions--pending { border-color: #fde68a; background: #fffbeb; color: #92400e; }
.admin-row-actions { display: flex; align-items: center; justify-content: flex-end; gap: .5rem; flex-wrap: wrap; }
.admin-row-actions .btn { min-width: 38px; min-height: 38px; display: inline-grid; place-items: center; padding: .45rem .6rem; border-radius: .7rem; }
.admin-orders .admin-mini-stats { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.admin-order-stat { display: flex; align-items: center; gap: .8rem; min-height: 92px; }
.admin-order-stat__icon { display: grid; flex: 0 0 44px; place-items: center; width: 44px; height: 44px; border-radius: 50%; background: #eff8ff; color: #0284c7; font-size: 1.15rem; }
.admin-order-stat .admin-stat-value { margin-top: .2rem; }
.admin-order-row { grid-template-columns: 52px minmax(220px, 1.15fr) minmax(300px, .9fr) auto; gap: 1.35rem; padding: 1.2rem 1.35rem; background: linear-gradient(110deg, #fff 0%, #fff 72%, #f8fbff 100%); }
.admin-order-row:hover { background: linear-gradient(110deg, #fff 0%, #f8fdff 72%, #fdf2f8 100%); }
.admin-order-row .admin-row-icon { width: 52px; height: 52px; border: 1px solid #dbeafe; border-radius: 17px; background: #eff6ff; color: #0284c7; font-size: 1.1rem; }
.admin-order-row .admin-row-title { display: flex; align-items: center; flex-wrap: wrap; gap: .4rem; font-size: .98rem; }
.admin-order-row .admin-row-sub { margin-top: .4rem; color: #52525b; font-weight: 700; }
.admin-order-row { cursor: default; }
.admin-order-row:focus-visible { outline: 3px solid rgba(8, 145, 178, .35); outline-offset: -3px; }
.admin-order-products { display: flex; align-items: center; gap: .35rem; max-width: 620px; margin-top: .35rem; color: #0e7490; font-size: .76rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.admin-order-products i { color: #d946ef; }
.admin-order-product-link { color: #18181b; font-weight: 800; text-decoration: none; }
.admin-order-product-link:hover { color: #0891b2; text-decoration: underline; }
.admin-order-row .admin-row-note { margin-top: .25rem; color: #94a3b8; font-size: .78rem; font-weight: 700; }
.admin-order-row .admin-row-meta { display: grid; grid-template-columns: 1fr 1.35fr; align-items: center; gap: .45rem .9rem; padding: .7rem .85rem; border: 1px solid #e2e8f0; border-radius: .8rem; background: rgba(255,255,255,.8); }
.admin-order-row .admin-row-meta span { color: #64748b; font-size: .76rem; white-space: nowrap; }
.admin-order-row .admin-row-meta span strong { color: #18181b; }
.admin-order-row .admin-row-meta .text-y2k-sale { color: #e11d48; font-size: .95rem; }
.admin-order-row .admin-row-meta .form-select { grid-column: 1 / -1; min-height: 38px; border-color: #dbeafe; border-radius: .6rem; }

.admin-invoice-sheet { width: min(920px, 100%); max-height: calc(100vh - 2rem); overflow: auto; border-radius: 22px; background: #eef5ff; box-shadow: 10px 10px 0 rgba(8, 145, 178, .35); }
.admin-invoice-sheet__topbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem 1.25rem; border-bottom: 1px solid #dbeafe; }
.admin-invoice-sheet__actions { display: flex; gap: .55rem; }
.admin-invoice-paper { margin: 1.25rem; padding: clamp(1.2rem, 3vw, 2.3rem); border: 1px solid #e4e4e7; border-radius: 16px; background: #fff; box-shadow: 0 14px 35px rgba(15, 23, 42, .1); }
.admin-invoice-paper__header { display: flex; justify-content: space-between; gap: 1.5rem; padding-bottom: 1.5rem; border-bottom: 3px solid #18181b; }
.admin-invoice-paper__brand { font-size: clamp(1.5rem, 4vw, 2.1rem); font-weight: 950; letter-spacing: .1em; }
.admin-invoice-paper__header p { margin: .35rem 0 0; color: #71717a; font-size: .7rem; line-height: 1.5; letter-spacing: .13em; font-weight: 800; }
.admin-invoice-paper__header p span { color: #0891b2; letter-spacing: 0; }
.admin-invoice-paper__code { display: grid; justify-items: end; gap: .35rem; text-align: right; }
.admin-invoice-paper__code strong { font-size: 1.25rem; }
.admin-invoice-paper__code span { color: #71717a; font-size: .8rem; }
.admin-invoice-paper__code b { display: inline-flex; align-items: center; gap: .35rem; padding: .35rem .6rem; border-radius: 999px; background: #dcfce7; color: #15803d; font-size: .68rem; letter-spacing: .04em; }
.admin-invoice-paper__code b.admin-invoice-paper__status--pending { background: #fef3c7; color: #b45309; }
.admin-invoice-paper__info { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin: 1.4rem 0; }
.admin-invoice-paper__info > div { display: grid; gap: .25rem; padding: .9rem; border: 1px solid #e4e4e7; border-radius: .75rem; background: #fafafa; }
.admin-invoice-paper__info small { color: #0891b2; font-size: .68rem; font-weight: 900; letter-spacing: .1em; }
.admin-invoice-paper__info span { color: #71717a; font-size: .8rem; }
.admin-invoice-table { width: 100%; border-collapse: collapse; }
.admin-invoice-table th, .admin-invoice-table td { padding: .75rem .55rem; border-bottom: 1px solid #e4e4e7; text-align: left; font-size: .8rem; }
.admin-invoice-table th { background: #f4f4f5; color: #52525b; font-size: .67rem; letter-spacing: .06em; text-transform: uppercase; }
.admin-invoice-table th:nth-child(n+3), .admin-invoice-table td:nth-child(n+3) { text-align: right; }
.admin-invoice-loading { display: flex; justify-content: center; gap: .55rem; padding: 2rem; color: #71717a; }
.admin-invoice-paper__summary { display: grid; gap: .35rem; width: min(330px, 100%); margin: 1.25rem 0 0 auto; }
.admin-invoice-paper__summary > div { display: flex; justify-content: space-between; gap: 1rem; padding: .35rem 0; color: #52525b; font-size: .82rem; }
.admin-invoice-paper__grand { margin-top: .35rem; padding-top: .8rem !important; border-top: 2px solid #18181b; color: #18181b !important; font-size: 1.05rem !important; }
.admin-invoice-paper__grand strong { color: #e11d48; }
.admin-invoice-paper__footer { display: flex; align-items: flex-start; gap: .75rem; margin-top: 2rem; padding: 1rem; border-radius: .8rem; background: linear-gradient(135deg, #ecfeff, #fdf2f8); color: #52525b; }
.admin-invoice-paper__footer i { color: #d946ef; font-size: 1.25rem; }
.admin-invoice-paper__footer div { display: grid; gap: .25rem; }
.admin-invoice-paper__footer span { font-size: .78rem; }

@media (max-width: 640px) {
  .admin-invoice-sheet__topbar, .admin-invoice-paper__header { align-items: flex-start; flex-direction: column; }
  .admin-invoice-paper__code { justify-items: start; text-align: left; }
  .admin-invoice-paper__info { grid-template-columns: 1fr; }
  .admin-invoice-table { min-width: 650px; }
  .admin-invoice-paper { overflow-x: auto; }
}

@media (max-width: 600px) {
  .admin-invoice-actions { align-items: stretch; flex-direction: column; }
}

@media (max-width: 900px) {
  .admin-orders .admin-mini-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .admin-order-row { grid-template-columns: 52px minmax(0, 1fr) auto; }
  .admin-order-row .admin-row-meta { grid-column: 2 / -1; }
}

@media (max-width: 600px) {
  .admin-orders .admin-mini-stats { grid-template-columns: 1fr; }
  .admin-order-row { grid-template-columns: 46px minmax(0, 1fr); gap: .8rem; padding: 1rem; }
  .admin-order-row .admin-row-icon { width: 46px; height: 46px; }
  .admin-order-row .admin-row-meta, .admin-order-row .admin-row-actions { grid-column: 1 / -1; }
  .admin-order-row .admin-row-meta { grid-template-columns: 1fr 1fr; }
  .admin-order-row .admin-row-actions { justify-content: flex-start; }
}
</style>
