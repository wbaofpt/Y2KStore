<template>
  <div class="admin-layout">
    <AdminSidebar v-model="activeTab" />

    <div class="admin-main">
      <div class="admin-main-header">
        <div>
          <span class="admin-kicker">Y2K Admin Center</span>
          <h2 class="admin-main-title">{{ currentTitle }}</h2>
          <p class="muted-copy mb-0">Quản lý dữ liệu bán hàng, nội dung, đơn hàng và tài khoản theo database mới.</p>
        </div>
        <div class="admin-main-header__actions">
          <button class="btn btn-y2k-outline btn-sm" :disabled="!canExport || exportLoading" @click="exportCurrent('sql')"><i class="bi bi-filetype-sql me-1"></i>SQL</button>
          <button class="btn btn-y2k-outline btn-sm" :disabled="!canExport || exportLoading" @click="exportCurrent('excel')"><i class="bi bi-file-earmark-spreadsheet me-1"></i>Excel</button>
          <button class="btn btn-y2k-outline" @click="refreshCurrent">
          <i class="bi bi-arrow-clockwise me-2"></i>Làm mới
          </button>
        </div>
      </div>

      <div class="admin-main-body">
        <AdminOverview v-if="activeTab === 'overview'" ref="overviewRef" @navigate="handleNavigate" />
        <AdminOrders v-else-if="activeTab === 'orders'" ref="ordersRef" :initial-status="orderStatusFilter" />
        <AdminProducts v-else-if="activeTab === 'products'" ref="productsRef" />
        <AdminCategories v-else-if="activeTab === 'categories'" ref="categoriesRef" />
        <AdminBanners v-else-if="activeTab === 'banners'" ref="bannersRef" />
        <AdminPromotions v-else-if="activeTab === 'promotions'" ref="promotionsRef" />
        <AdminCoupons v-else-if="activeTab === 'coupons'" ref="couponsRef" />
        <AdminReviews v-else-if="activeTab === 'reviews'" ref="reviewsRef" />
        <AdminAddresses v-else-if="activeTab === 'addresses'" ref="addressesRef" />
        <AdminPayments v-else-if="activeTab === 'payments'" ref="paymentsRef" />
        <AdminUsers v-else-if="activeTab === 'users'" ref="usersRef" />
        <AdminDatabase v-else-if="activeTab === 'database'" ref="databaseRef" @navigate="activeTab = $event" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import axios from 'axios'
import AdminSidebar from '../components/admin/AdminSidebar.vue'
import AdminOverview from '../components/admin/AdminOverview.vue'
import AdminOrders from '../components/admin/AdminOrders.vue'
import AdminProducts from '../components/admin/AdminProducts.vue'
import AdminCategories from '../components/admin/AdminCategories.vue'
import AdminBanners from '../components/admin/AdminBanners.vue'
import AdminPromotions from '../components/admin/AdminPromotions.vue'
import AdminCoupons from '../components/admin/AdminCoupons.vue'
import AdminReviews from '../components/admin/AdminReviews.vue'
import AdminAddresses from '../components/admin/AdminAddresses.vue'
import AdminPayments from '../components/admin/AdminPayments.vue'
import AdminUsers from '../components/admin/AdminUsers.vue'
import AdminDatabase from '../components/admin/AdminDatabase.vue'

const activeTab = ref('overview')
const overviewRef = ref(null)
const ordersRef = ref(null)
const productsRef = ref(null)
const categoriesRef = ref(null)
const bannersRef = ref(null)
const promotionsRef = ref(null)
const couponsRef = ref(null)
const reviewsRef = ref(null)
const addressesRef = ref(null)
const paymentsRef = ref(null)
const usersRef = ref(null)
const databaseRef = ref(null)
const exportLoading = ref(false)
const orderStatusFilter = ref('')

const titles = {
  overview: 'Tổng quan cửa hàng',
  orders: 'Quản lý đơn hàng',
  products: 'Quản lý sản phẩm',
  categories: 'Danh mục',
  banners: 'Banner trang chủ',
  promotions: 'Khuyến mãi',
  coupons: 'Mã giảm giá',
  reviews: 'Đánh giá khách hàng',
  addresses: 'Địa chỉ giao hàng',
  payments: 'Thanh toán',
  users: 'Người dùng',
  database: 'Sơ đồ database'
}

const currentTitle = computed(() => titles[activeTab.value] || 'Admin')
const exportEndpoints = {
  orders: 'orders', products: 'products', categories: 'categories', banners: 'banners',
  promotions: 'promotions', coupons: 'coupons', reviews: 'reviews', addresses: 'addresses',
  payments: 'payments', users: 'users'
}
const canExport = computed(() => Boolean(exportEndpoints[activeTab.value]))

const refMap = {
  overview: overviewRef,
  orders: ordersRef,
  products: productsRef,
  categories: categoriesRef,
  banners: bannersRef,
  promotions: promotionsRef,
  coupons: couponsRef,
  reviews: reviewsRef,
  addresses: addressesRef,
  payments: paymentsRef,
  users: usersRef,
  database: databaseRef
}

function refreshCurrent() {
  refMap[activeTab.value]?.value?.refresh?.()
}

function handleNavigate(payload) {
  if (typeof payload === 'string') {
    activeTab.value = payload
    return
  }

  const nextTab = payload?.tab || 'overview'
  if (nextTab === 'orders') {
    orderStatusFilter.value = payload?.status || ''
  }
  activeTab.value = nextTab
}

function flattenRecord(record) {
  return Object.fromEntries(Object.entries(record || {}).map(([key, value]) => [
    key,
    value !== null && typeof value === 'object' ? JSON.stringify(value) : value
  ]))
}

function sqlValue(value) {
  if (value === null || value === undefined || value === '') return 'NULL'
  if (typeof value === 'number' || typeof value === 'boolean') return value ? '1' : '0'
  return `'${String(value).replace(/\\/g, '\\\\').replace(/'/g, "''")}'`
}

function downloadFile(content, filename, type) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function exportAsSql(rows, tableName) {
  if (!rows.length) return `-- Không có dữ liệu để xuất từ ${tableName}.\n`
  const columns = [...new Set(rows.flatMap((row) => Object.keys(row)))]
  return rows.map((row) => {
    const values = columns.map((column) => sqlValue(row[column])).join(', ')
    return `INSERT INTO \`${tableName}\` (${columns.map((column) => `\`${column}\``).join(', ')}) VALUES (${values});`
  }).join('\n')
}

function exportAsExcel(rows, title) {
  const columns = [...new Set(rows.flatMap((row) => Object.keys(row)))]
  const header = columns.map((column) => `<th>${column}</th>`).join('')
  const body = rows.map((row) => `<tr>${columns.map((column) => `<td>${String(row[column] ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;')}</td>`).join('')}</tr>`).join('')
  return `\ufeff<table><caption>${title}</caption><thead><tr>${header}</tr></thead><tbody>${body}</tbody></table>`
}

async function exportCurrent(format) {
  const endpoint = exportEndpoints[activeTab.value]
  if (!endpoint || exportLoading.value) return
  exportLoading.value = true
  try {
    const { data } = await axios.get(`/api/admin/${endpoint}`)
    const rows = (Array.isArray(data) ? data : [data]).map(flattenRecord)
    const date = new Date().toISOString().slice(0, 10)
    if (format === 'sql') {
      downloadFile(exportAsSql(rows, endpoint === 'users' ? 'user' : endpoint), `y2k-${endpoint}-${date}.sql`, 'application/sql;charset=utf-8')
    } else {
      downloadFile(exportAsExcel(rows, currentTitle.value), `y2k-${endpoint}-${date}.xls`, 'application/vnd.ms-excel;charset=utf-8')
    }
  } finally {
    exportLoading.value = false
  }
}
</script>
