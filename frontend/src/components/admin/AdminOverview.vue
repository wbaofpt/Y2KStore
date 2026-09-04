<template>
  <div>
    <h4 class="admin-page-title">Tổng quan hệ thống</h4>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-y2k-accent" role="status"></div>
    </div>

    <div v-else>
      <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
          <div class="admin-stat-card admin-stat-card--accent">
            <div class="admin-stat-label">Tổng doanh thu</div>
            <div class="admin-stat-value">{{ formatPrice(totalRevenue) }}</div>
            <div class="admin-stat-meta"><i class="bi bi-wallet2 me-1"></i> Không tính đơn hủy</div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="admin-stat-card admin-stat-card--dark">
            <div class="admin-stat-label">Tổng đơn hàng</div>
            <div class="admin-stat-value">{{ stats.totalOrders }}</div>
            <div class="admin-stat-meta"><i class="bi bi-clock me-1"></i> {{ stats.pendingOrders }} chờ xử lý</div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="admin-stat-card">
            <div class="admin-stat-label">Sản phẩm</div>
            <div class="admin-stat-value">{{ stats.totalProducts }}</div>
            <div class="admin-stat-meta text-y2k-sale"><i class="bi bi-exclamation-triangle me-1"></i> {{ stats.lowStockProducts }} sắp hết hàng</div>
          </div>
        </div>
        <div class="col-sm-6 col-xl-3">
          <div class="admin-stat-card">
            <div class="admin-stat-label">Người dùng</div>
            <div class="admin-stat-value">{{ stats.totalUsers }}</div>
            <div class="admin-stat-meta"><i class="bi bi-grid me-1"></i> {{ stats.totalCategories }} danh mục · {{ stats.totalBanners }} banner</div>
          </div>
        </div>
      </div>

      <div class="row g-3 mb-4">
        <div class="col-6 col-lg-2">
          <div class="admin-panel-card h-100">
            <div class="admin-stat-label">Khuyến mãi</div>
            <div class="admin-stat-value">{{ stats.totalPromotions }}</div>
          </div>
        </div>
        <div class="col-6 col-lg-2">
          <div class="admin-panel-card h-100">
            <div class="admin-stat-label">Mã giảm giá</div>
            <div class="admin-stat-value">{{ stats.totalCoupons }}</div>
          </div>
        </div>
        <div class="col-6 col-lg-2">
          <div class="admin-panel-card h-100">
            <div class="admin-stat-label">Đánh giá</div>
            <div class="admin-stat-value">{{ stats.totalReviews }}</div>
          </div>
        </div>
        <div class="col-6 col-lg-2">
          <div class="admin-panel-card h-100">
            <div class="admin-stat-label">Địa chỉ</div>
            <div class="admin-stat-value">{{ stats.totalAddresses }}</div>
          </div>
        </div>
        <div class="col-6 col-lg-2">
          <div class="admin-panel-card h-100">
            <div class="admin-stat-label">Thanh toán</div>
            <div class="admin-stat-value">{{ stats.totalPayments }}</div>
          </div>
        </div>
      </div>

      <section class="admin-revenue-analytics">
        <div class="admin-chart-card admin-chart-card--wide">
          <div class="admin-analytics-head">
            <div>
              <span class="admin-kicker">Revenue timeline</span>
              <h5>Doanh thu theo thời gian</h5>
              <p class="admin-analytics-copy">Theo dõi doanh thu của các đơn không bị hủy theo từng khoảng.</p>
            </div>
            <div class="admin-period-filter" role="tablist" aria-label="Lọc doanh thu theo thời gian">
              <button
                v-for="option in revenuePeriodOptions"
                :key="option.key"
                type="button"
                role="tab"
                :aria-selected="revenuePeriod === option.key"
                :class="{ active: revenuePeriod === option.key }"
                @click="revenuePeriod = option.key"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <div class="admin-revenue-summary">
            <div>
              <span>{{ revenuePeriodLabel }}</span>
              <strong>{{ formatPrice(revenuePeriodTotal) }}</strong>
            </div>
            <span>{{ revenuePeriodHint }}</span>
          </div>

          <div class="admin-line-chart">
            <svg viewBox="0 0 760 250" role="img" :aria-label="`Biểu đồ doanh thu ${revenuePeriodLabel.toLowerCase()}`">
              <defs>
                <linearGradient id="adminRevenueFill" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="#22d3ee" stop-opacity=".28" />
                  <stop offset="100%" stop-color="#22d3ee" stop-opacity="0" />
                </linearGradient>
              </defs>
              <line v-for="tick in revenueTrend.gridLines" :key="tick.value" x1="18" x2="742" :y1="tick.y" :y2="tick.y" class="admin-line-chart__grid" />
              <polygon :points="revenueTrend.areaPoints" fill="url(#adminRevenueFill)" />
              <polyline :points="revenueTrend.pointString" class="admin-line-chart__line" />
              <circle v-for="point in revenueTrend.points" :key="point.key" :cx="point.x" :cy="point.y" r="4" class="admin-line-chart__point" />
            </svg>
            <div class="admin-line-chart__labels">
              <span v-for="point in revenueTrend.points" :key="`${point.key}-label`">{{ point.label }}</span>
            </div>
          </div>
        </div>

        <div class="admin-chart-card">
          <div class="admin-chart-head">
            <div><span class="admin-kicker">Payment mix</span><h5>Cơ cấu thanh toán</h5></div>
            <i class="bi bi-pie-chart admin-chart-accent"></i>
          </div>
          <div class="admin-donut-layout">
            <div class="admin-donut" :style="{ background: paymentDonutGradient }" role="img" :aria-label="`Cơ cấu thanh toán trong ${revenuePeriodLabel.toLowerCase()}`">
              <div><strong>{{ periodOrders.length }}</strong><span>đơn trong kỳ</span></div>
            </div>
            <div class="admin-donut-legend">
              <div v-for="item in paymentChart" :key="item.key">
                <span><i :style="{ background: item.color }"></i>{{ item.label }}</span>
                <strong>{{ item.value }} · {{ item.percent }}%</strong>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="admin-overview-charts">
        <div class="admin-chart-card admin-chart-card--wide">
          <div class="admin-chart-head"><div><span class="admin-kicker">Order pulse</span><h5>Biểu đồ trạng thái đơn hàng</h5></div><span class="admin-chart-total">{{ overviewOrders.length }} đơn</span></div>
          <div class="admin-status-chart">
            <button v-for="item in statusChart" :key="item.key" type="button" class="admin-status-chart__item" :aria-label="`Xem đơn ${item.label.toLowerCase()}`" @click="$emit('navigate', { tab: 'orders', status: item.key })">
              <div class="admin-status-chart__label"><span><i class="bi" :class="item.icon"></i>{{ item.label }}</span><strong>{{ item.value }}</strong></div>
              <div class="admin-status-chart__track"><span :style="{ width: `${item.percent}%`, background: item.color }"></span></div>
            </button>
          </div>
        </div>
        <div class="admin-chart-card">
          <div class="admin-chart-head"><div><span class="admin-kicker">Product pulse</span><h5>Sản phẩm bán chạy</h5></div><i class="bi bi-bar-chart-line admin-chart-accent"></i></div>
          <div v-if="topProducts.length" class="admin-top-products">
            <div v-for="item in topProducts" :key="item.key" class="admin-top-product">
              <div class="admin-top-product__head"><span>{{ item.name }}</span><strong>{{ item.quantity }} sản phẩm</strong></div>
              <div class="admin-top-product__track"><span :style="{ width: `${item.percent}%` }"></span></div>
              <small>{{ formatPrice(item.revenue) }}</small>
            </div>
          </div>
          <div v-else class="admin-chart-empty">Chưa có dữ liệu sản phẩm trong đơn.</div>
        </div>
      </section>

      <section class="admin-featured-grid" aria-label="Xếp hạng sản phẩm nổi bật">
        <div class="admin-chart-card">
          <div class="admin-chart-head">
            <div><span class="admin-kicker">Featured / best sellers</span><h5>Sản phẩm mua nhiều nhất</h5></div>
            <i class="bi bi-fire admin-chart-accent" aria-hidden="true"></i>
          </div>
          <div v-if="featuredBestSelling.length" class="admin-featured-list">
            <article v-for="(product, index) in featuredBestSelling" :key="product.id" class="admin-featured-item">
              <span class="admin-featured-item__rank">0{{ index + 1 }}</span>
              <img :src="product.image || product.imageUrl || '/summer_y2k.png'" :alt="product.name">
              <div>
                <strong>{{ product.name }}</strong>
                <span>{{ product.soldCount || 0 }} lượt mua · {{ formatPrice(product.price) }}</span>
              </div>
            </article>
          </div>
          <div v-else class="admin-chart-empty">Chưa có dữ liệu bán hàng.</div>
        </div>

        <div class="admin-chart-card">
          <div class="admin-chart-head">
            <div><span class="admin-kicker">Featured / community</span><h5>Sản phẩm được đánh giá cao</h5></div>
            <i class="bi bi-star admin-chart-accent" aria-hidden="true"></i>
          </div>
          <div v-if="featuredTopRated.length" class="admin-featured-list">
            <article v-for="(product, index) in featuredTopRated" :key="product.id" class="admin-featured-item">
              <span class="admin-featured-item__rank">0{{ index + 1 }}</span>
              <img :src="product.image || product.imageUrl || '/summer_y2k.png'" :alt="product.name">
              <div>
                <strong>{{ product.name }}</strong>
                <span><i class="bi bi-star-fill" aria-hidden="true"></i> {{ Number(product.ratingAverage || 0).toFixed(1) }} · {{ product.reviewCount || 0 }} đánh giá</span>
              </div>
            </article>
          </div>
          <div v-else class="admin-chart-empty">Chưa có đánh giá nổi bật.</div>
        </div>
      </section>

      <div class="admin-panel-card">
        <h5 class="fw-bold mb-3">Truy cập nhanh</h5>
        <div class="d-flex flex-wrap gap-2">
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'orders')"><i class="bi bi-cart3 me-1"></i> Xử lý đơn hàng</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'products')"><i class="bi bi-plus-circle me-1"></i> Thêm sản phẩm</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'banners')"><i class="bi bi-images me-1"></i> Quản lý banner</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'promotions')"><i class="bi bi-megaphone me-1"></i> Khuyến mãi</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'coupons')"><i class="bi bi-ticket-perforated me-1"></i> Mã giảm giá</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'reviews')"><i class="bi bi-chat-square-text me-1"></i> Đánh giá</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'users')"><i class="bi bi-people me-1"></i> Quản lý user</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'addresses')"><i class="bi bi-geo-alt me-1"></i> Địa chỉ</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'payments')"><i class="bi bi-credit-card me-1"></i> Thanh toán</button>
          <button class="btn btn-y2k-outline btn-sm" @click="$emit('navigate', 'database')"><i class="bi bi-diagram-3 me-1"></i> Cấu trúc DB</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { formatPrice } from '../../composables/useFormat'

defineEmits(['navigate'])

const loading = ref(false)
const overviewOrders = ref([])
const orderItemsByOrder = ref({})
const featuredBestSelling = ref([])
const featuredTopRated = ref([])
const revenuePeriod = ref('day')
const revenuePeriodOptions = [
  { key: 'day', label: 'Theo ngày', hint: '14 ngày gần nhất', count: 14 },
  { key: 'week', label: 'Theo tuần', hint: '8 tuần gần nhất', count: 8 },
  { key: 'month', label: 'Theo tháng', hint: '12 tháng gần nhất', count: 12 }
]
const stats = ref({
  totalRevenue: 0, totalOrders: 0, totalProducts: 0, totalUsers: 0,
  pendingOrders: 0, lowStockProducts: 0, totalCategories: 0, totalBanners: 0,
  totalPromotions: 0, totalCoupons: 0, totalReviews: 0, totalAddresses: 0, totalPayments: 0
})

const statusChart = computed(() => {
  const definitions = [
    { key: 'PROCESSING', label: 'Chờ xử lý', icon: 'bi-hourglass-split', color: '#f59e0b' },
    { key: 'PREPARING', label: 'Đang chuẩn bị', icon: 'bi-box-seam', color: '#06b6d4' },
    { key: 'SHIPPED', label: 'Đang vận chuyển', icon: 'bi-truck', color: '#2563eb' },
    { key: 'DELIVERED', label: 'Đã hoàn tất', icon: 'bi-check2-circle', color: '#16a34a' },
    { key: 'CANCELLED', label: 'Đã hủy', icon: 'bi-x-circle', color: '#e11d48' }
  ]
  const total = Math.max(overviewOrders.value.length, 1)
  return definitions.map((item) => ({ ...item, value: overviewOrders.value.filter((order) => order.status === item.key).length, percent: Math.max(4, Math.round((overviewOrders.value.filter((order) => order.status === item.key).length / total) * 100)) }))
})

const revenuePeriodOption = computed(() => revenuePeriodOptions.find((option) => option.key === revenuePeriod.value) || revenuePeriodOptions[0])
const revenuePeriodLabel = computed(() => revenuePeriodOption.value.label)
const revenuePeriodHint = computed(() => revenuePeriodOption.value.hint)

function toDate(value) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function startOfPeriod(value, mode) {
  const date = new Date(value)
  date.setHours(0, 0, 0, 0)
  if (mode === 'week') {
    const mondayOffset = (date.getDay() + 6) % 7
    date.setDate(date.getDate() - mondayOffset)
  }
  if (mode === 'month') date.setDate(1)
  return date
}

function shiftPeriod(value, mode, amount) {
  const date = new Date(value)
  if (mode === 'month') date.setMonth(date.getMonth() + amount)
  else date.setDate(date.getDate() + amount * (mode === 'week' ? 7 : 1))
  return date
}

function periodKey(value, mode) {
  const date = startOfPeriod(value, mode)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  if (mode === 'month') return `${date.getFullYear()}-${month}`
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function periodLabel(value, mode) {
  const date = new Date(value)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return mode === 'month' ? `${month}/${date.getFullYear()}` : `${day}/${month}`
}

function formatRangeDate(value) {
  const date = new Date(value)
  return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`
}

const revenueWindow = computed(() => {
  const option = revenuePeriodOption.value
  const current = startOfPeriod(new Date(), revenuePeriod.value)
  const first = shiftPeriod(current, revenuePeriod.value, -(option.count - 1))
  const buckets = Array.from({ length: option.count }, (_, index) => {
    const date = shiftPeriod(first, revenuePeriod.value, index)
    return { key: periodKey(date, revenuePeriod.value), label: periodLabel(date, revenuePeriod.value), date }
  })
  return { buckets, start: first, end: shiftPeriod(current, revenuePeriod.value, 1) }
})

const revenueOrders = computed(() => overviewOrders.value.filter((order) => order.status !== 'CANCELLED' && toDate(order.orderDate)))
const periodOrders = computed(() => revenueOrders.value.filter((order) => {
  const date = toDate(order.orderDate)
  return date >= revenueWindow.value.start && date < revenueWindow.value.end
}))

const revenueSeries = computed(() => {
  const totals = new Map(revenueWindow.value.buckets.map((bucket) => [bucket.key, 0]))
  revenueOrders.value.forEach((order) => {
    const date = toDate(order.orderDate)
    const key = periodKey(date, revenuePeriod.value)
    if (totals.has(key)) totals.set(key, totals.get(key) + Number(order.totalAmount || 0))
  })
  return revenueWindow.value.buckets.map((bucket) => ({ ...bucket, value: totals.get(bucket.key) || 0 }))
})

const revenuePeriodTotal = computed(() => revenueSeries.value.reduce((sum, item) => sum + item.value, 0))
const totalRevenue = computed(() => revenueOrders.value.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0))

const revenueTrend = computed(() => {
  const chart = { left: 22, right: 738, top: 18, bottom: 212 }
  const values = revenueSeries.value.map((item) => item.value)
  const max = Math.max(...values, 1)
  const step = values.length > 1 ? (chart.right - chart.left) / (values.length - 1) : 0
  const points = revenueSeries.value.map((item, index) => ({
    ...item,
    x: chart.left + step * index,
    y: chart.bottom - (item.value / max) * (chart.bottom - chart.top)
  }))
  const pointString = points.map((point) => `${point.x},${point.y}`).join(' ')
  const areaPoints = `${pointString} ${chart.right},${chart.bottom} ${chart.left},${chart.bottom}`
  const gridLines = [0, 0.5, 1].map((ratio) => ({ value: ratio, y: chart.bottom - ratio * (chart.bottom - chart.top) }))
  return { points, pointString, areaPoints, gridLines }
})

const paymentChart = computed(() => {
  const definitions = [
    { key: 'COD', label: 'COD', color: '#0891b2' },
    { key: 'BANKING', label: 'MB Bank', color: '#0f766e' },
    { key: 'ZALOPAY', label: 'ZaloPay', color: '#64748b' },
    { key: 'OTHER', label: 'Khác', color: '#f59e0b' }
  ]
  const values = definitions.map((item) => ({
    ...item,
    value: periodOrders.value.filter((order) => (definitions.some((definition) => definition.key === order.paymentMethod) ? order.paymentMethod : 'OTHER') === item.key).length
  }))
  const total = periodOrders.value.length
  return values.map((item) => ({ ...item, percent: total ? Math.round((item.value / total) * 100) : 0, share: total ? (item.value / total) * 100 : 0 }))
})

const paymentDonutGradient = computed(() => {
  const active = paymentChart.value.filter((item) => item.value > 0)
  if (!active.length) return '#e4e4e7'
  let cursor = 0
  const segments = active.map((item) => {
    const start = cursor
    cursor += item.share
    return `${item.color} ${start}% ${cursor}%`
  })
  return `conic-gradient(${segments.join(', ')})`
})

const topProducts = computed(() => {
  const activeOrderIds = new Set(periodOrders.value.map((order) => String(order.id)))
  const totals = new Map()
  Object.entries(orderItemsByOrder.value).forEach(([orderId, items]) => {
    if (!activeOrderIds.has(String(orderId))) return
    items.forEach((item) => {
      const key = item.productId || item.productName || `item-${item.id}`
      const current = totals.get(key) || { key, name: item.productName || 'Sản phẩm đã xóa', quantity: 0, revenue: 0 }
      const quantity = Number(item.quantity || 0)
      current.quantity += quantity
      current.revenue += quantity * Number(item.unitPrice || 0)
      totals.set(key, current)
    })
  })
  const products = [...totals.values()].sort((a, b) => b.quantity - a.quantity || b.revenue - a.revenue).slice(0, 5)
  const max = Math.max(...products.map((item) => item.quantity), 1)
  return products.map((item) => ({ ...item, percent: Math.max(6, Math.round((item.quantity / max) * 100)) }))
})

onMounted(fetchStats)

async function fetchStats() {
  loading.value = true
  try {
    const [{ data }, ordersResponse, bestSellingResponse, topRatedResponse] = await Promise.all([
      axios.get('/api/admin/stats'),
      axios.get('/api/admin/orders'),
      axios.get('/api/products/featured', { params: { type: 'best-selling', limit: 5 } }),
      axios.get('/api/products/featured', { params: { type: 'top-rated', limit: 5 } })
    ])
    stats.value = data
    overviewOrders.value = ordersResponse.data || []
    featuredBestSelling.value = bestSellingResponse.data || []
    featuredTopRated.value = topRatedResponse.data || []
    const itemEntries = await Promise.all(overviewOrders.value.filter((order) => order.status !== 'CANCELLED').map(async (order) => {
      try {
        const { data: items } = await axios.get(`/api/admin/orders/${order.id}/items`)
        return [String(order.id), items || []]
      } catch {
        return [String(order.id), []]
      }
    }))
    orderItemsByOrder.value = Object.fromEntries(itemEntries)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

defineExpose({ refresh: fetchStats })
</script>

<style scoped>
.admin-overview-charts { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(280px, .65fr); gap: 1rem; margin-bottom: 1rem; }
.admin-featured-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-bottom: 1rem; }
.admin-chart-card { min-width: 0; padding: 1.25rem; border: 1px solid #e2e8f0; border-radius: 18px; background: #fff; box-shadow: 0 8px 24px rgba(15, 23, 42, .05); }
.admin-chart-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; margin-bottom: 1.2rem; }
.admin-chart-head h5 { margin: .35rem 0 0; font-weight: 900; }
.admin-chart-total { padding: .35rem .6rem; border-radius: 999px; background: #ecfeff; color: #0e7490; font-size: .75rem; font-weight: 900; }
.admin-status-chart { display: grid; gap: .8rem; }
.admin-status-chart__item { width: 100%; padding: 0; border: 0; background: transparent; color: inherit; text-align: left; cursor: pointer; }
.admin-status-chart__item:focus-visible { outline: 2px solid #0891b2; outline-offset: 5px; border-radius: 5px; }
.admin-status-chart__label { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: .3rem; color: #52525b; font-size: .78rem; font-weight: 800; }
.admin-status-chart__label span { display: flex; align-items: center; gap: .4rem; }.admin-status-chart__label i { color: #0891b2; }
.admin-status-chart__track { height: 9px; overflow: hidden; border-radius: 999px; background: #f1f5f9; }.admin-status-chart__track span { display: block; height: 100%; min-width: 6px; border-radius: inherit; transition: width .35s ease; }
.admin-chart-accent { color: #0891b2; font-size: 1.5rem; }.admin-chart-revenue { display: block; margin: .4rem 0 1.3rem; font-size: 1.8rem; }
.admin-revenue-bars { display: flex; align-items: end; gap: .8rem; height: 105px; padding: 0 .7rem; border-bottom: 1px solid #e2e8f0; }.admin-revenue-bars span { flex: 1; min-height: 10px; border-radius: 8px 8px 0 0; box-shadow: 0 4px 0 rgba(165, 243, 252, .4); }
.admin-chart-legend { display: flex; flex-wrap: wrap; gap: .75rem; margin-top: .8rem; color: #71717a; font-size: .7rem; font-weight: 800; }.admin-chart-legend span { display: inline-flex; align-items: center; gap: .3rem; }.admin-chart-legend i { width: 8px; height: 8px; border-radius: 50%; }
@media (max-width: 900px) { .admin-overview-charts { grid-template-columns: 1fr; } }

.admin-revenue-analytics { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(280px, .65fr); gap: 1rem; margin-bottom: 1rem; }
.admin-analytics-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; }
.admin-analytics-head h5 { margin: .35rem 0 0; font-weight: 900; }
.admin-analytics-copy { margin: .35rem 0 0; color: #71717a; font-size: .78rem; }
.admin-period-filter { display: flex; flex: 0 0 auto; gap: .2rem; padding: .25rem; border: 1px solid #dbeafe; border-radius: 10px; background: #f8fafc; }
.admin-period-filter button { min-height: 40px; padding: .45rem .7rem; border: 0; border-radius: 7px; background: transparent; color: #52525b; cursor: pointer; font-size: .72rem; font-weight: 900; transition: background .2s ease, color .2s ease, box-shadow .2s ease; }
.admin-period-filter button:hover { color: #0e7490; background: #ecfeff; }
.admin-period-filter button.active { background: #18181b; color: #fff; box-shadow: 3px 3px 0 #22d3ee; }
.admin-revenue-summary { display: flex; align-items: end; justify-content: space-between; gap: 1rem; padding: .85rem 1rem; border: 1px solid #bae6fd; border-radius: 10px; background: #f8fdff; }
.admin-revenue-summary div { display: grid; gap: .2rem; }.admin-revenue-summary div span { color: #0e7490; font-size: .7rem; font-weight: 900; letter-spacing: .05em; text-transform: uppercase; }.admin-revenue-summary strong { color: #18181b; font-size: 1.55rem; }.admin-revenue-summary > span { color: #71717a; font-size: .74rem; }
.admin-line-chart { min-width: 0; margin-top: 1rem; overflow: hidden; }
.admin-line-chart svg { display: block; width: 100%; height: auto; min-height: 190px; overflow: visible; }
.admin-line-chart__grid { stroke: #e4e4e7; stroke-dasharray: 4 5; stroke-width: 1; }
.admin-line-chart__line { fill: none; stroke: #0891b2; stroke-linecap: round; stroke-linejoin: round; stroke-width: 4; }
.admin-line-chart__point { fill: #fff; stroke: #0891b2; stroke-width: 3; }
.admin-line-chart__labels { display: grid; grid-template-columns: repeat(14, minmax(0, 1fr)); gap: .2rem; color: #71717a; font-size: .62rem; text-align: center; }
.admin-donut-layout { display: grid; grid-template-columns: 160px minmax(0, 1fr); align-items: center; gap: 1.1rem; min-height: 220px; }
.admin-donut { display: grid; place-items: center; width: 154px; height: 154px; border-radius: 50%; }
.admin-donut > div { display: grid; place-items: center; width: 94px; height: 94px; border-radius: 50%; background: #fff; text-align: center; }
.admin-donut strong { display: block; color: #18181b; font-size: 1.55rem; line-height: 1; }.admin-donut span { margin-top: .25rem; color: #71717a; font-size: .62rem; font-weight: 800; }
.admin-donut-legend { display: grid; gap: .8rem; }.admin-donut-legend > div { display: flex; align-items: center; justify-content: space-between; gap: .8rem; color: #52525b; font-size: .75rem; }.admin-donut-legend span { display: inline-flex; align-items: center; gap: .4rem; }.admin-donut-legend i { width: 9px; height: 9px; border-radius: 50%; }.admin-donut-legend strong { color: #18181b; font-size: .72rem; white-space: nowrap; }
.admin-top-products { display: grid; gap: .95rem; }.admin-top-product { min-width: 0; }.admin-top-product__head { display: flex; justify-content: space-between; gap: .8rem; color: #52525b; font-size: .74rem; font-weight: 800; }.admin-top-product__head span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.admin-top-product__head strong { color: #18181b; white-space: nowrap; }.admin-top-product__track { height: 8px; margin-top: .35rem; overflow: hidden; border-radius: 999px; background: #f1f5f9; }.admin-top-product__track span { display: block; height: 100%; border-radius: inherit; background: linear-gradient(90deg, #0891b2, #22d3ee); }.admin-top-product small { display: block; margin-top: .25rem; color: #71717a; font-size: .68rem; }
.admin-chart-empty { display: grid; min-height: 180px; place-items: center; color: #71717a; font-size: .78rem; text-align: center; }
.admin-featured-list { display: grid; gap: .75rem; }
.admin-featured-item { display: grid; grid-template-columns: 26px 52px minmax(0, 1fr); gap: .65rem; align-items: center; min-width: 0; padding: .55rem 0; border-bottom: 1px solid #f1f5f9; }
.admin-featured-item:last-child { border-bottom: 0; }
.admin-featured-item__rank { color: #0891b2; font-size: .72rem; font-weight: 900; }
.admin-featured-item img { width: 52px; height: 62px; object-fit: cover; border-radius: 7px; background: #f4f4f5; }
.admin-featured-item div { min-width: 0; }
.admin-featured-item strong, .admin-featured-item span { display: block; overflow-wrap: anywhere; }
.admin-featured-item strong { color: #18181b; font-size: .78rem; line-height: 1.35; }
.admin-featured-item span { margin-top: .25rem; color: #71717a; font-size: .7rem; }
.admin-featured-item span i { color: #f59e0b; }
@media (max-width: 900px) { .admin-revenue-analytics { grid-template-columns: 1fr; }.admin-donut-layout { grid-template-columns: 170px minmax(0, 1fr); } }
@media (max-width: 560px) { .admin-analytics-head { flex-direction: column; }.admin-period-filter { width: 100%; }.admin-period-filter button { flex: 1 1 0; padding-inline: .35rem; font-size: .68rem; }.admin-revenue-summary { align-items: flex-start; flex-direction: column; }.admin-line-chart__labels { font-size: .55rem; }.admin-donut-layout { grid-template-columns: 1fr; justify-items: center; }.admin-donut-legend { width: 100%; } }
@media (max-width: 900px) { .admin-featured-grid { grid-template-columns: 1fr; } }
</style>
