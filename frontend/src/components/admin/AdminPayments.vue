<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Payment control</span>
        <h4 class="admin-page-title mb-0">Quản lý thanh toán</h4>
        <p>Cập nhật phương thức, số tiền và trạng thái thanh toán của từng đơn hàng.</p>
      </div>
      <button class="btn btn-y2k-outline btn-sm" @click="fetchPayments">
        <i class="bi bi-arrow-clockwise me-1"></i> Tải lại
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card"><div class="admin-stat-label">Tổng thanh toán</div><div class="admin-stat-value">{{ payments.length }}</div><div class="admin-stat-meta">payments</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Đã thanh toán</div><div class="admin-stat-value">{{ paidCount }}</div><div class="admin-stat-meta">PAID</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Chưa thanh toán</div><div class="admin-stat-value">{{ unpaidCount }}</div><div class="admin-stat-meta">UNPAID</div></div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm đơn, khách hàng, phương thức..." style="max-width: 340px;">
      <select v-model="statusFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả trạng thái</option>
        <option value="UNPAID">UNPAID</option>
        <option value="PAID">PAID</option>
        <option value="FAILED">FAILED</option>
        <option value="REFUNDED">REFUNDED</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">Cập nhật thanh toán</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm"><i class="bi bi-x-lg"></i></button>
      </div>
      <form class="row g-3" @submit.prevent="save">
        <div class="col-md-3">
          <label class="form-label small fw-bold">Mã đơn</label>
          <input :value="editing?.orderId || ''" class="form-control form-control-sm" disabled>
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-bold">Phương thức</label>
          <input v-model="form.method" class="form-control form-control-sm">
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-bold">Số tiền</label>
          <input v-model.number="form.amount" type="number" min="0" step="1" class="form-control form-control-sm">
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-bold">Trạng thái</label>
          <select v-model="form.status" class="form-select form-select-sm">
            <option value="UNPAID">UNPAID</option>
            <option value="PAID">PAID</option>
            <option value="FAILED">FAILED</option>
            <option value="REFUNDED">REFUNDED</option>
          </select>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu thanh toán</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách thanh toán</strong><span>{{ filteredPayments.length }} kết quả</span></div>
        <span class="admin-db-chip">payments</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="item in filteredPayments" :key="item.id" class="admin-entity-row">
          <div class="admin-row-icon"><i class="bi bi-credit-card"></i></div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              Đơn #{{ item.orderId }}
              <span class="badge" :class="paymentBadgeClass(item.status)">{{ item.status || 'UNPAID' }}</span>
            </div>
            <div class="admin-row-sub">{{ item.customerName || `User #${item.userId || '-'}` }}</div>
          </div>
          <div class="admin-row-meta">
            <span>Phương thức: <strong>{{ item.method || 'N/A' }}</strong></span>
            <span>Số tiền: <strong class="text-y2k-sale">{{ formatMoney(item.amount) }}</strong></span>
            <span>{{ formatDateLabel(item.paymentDate) || 'Chưa có ngày' }}</span>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click="openForm(item)"><i class="bi bi-pencil"></i></button>
          </div>
        </article>
        <div v-if="!filteredPayments.length" class="admin-empty-state">
          <i class="bi bi-credit-card"></i>
          <strong>Không có thanh toán phù hợp</strong>
          <span>Thử đổi từ khóa hoặc trạng thái.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useToastStore } from '../../stores/toast'

const toast = useToastStore()
const payments = ref([])
const search = ref('')
const statusFilter = ref('')
const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

const paidCount = computed(() => payments.value.filter((item) => item.status === 'PAID').length)
const unpaidCount = computed(() => payments.value.filter((item) => item.status !== 'PAID').length)
const filteredPayments = computed(() => {
  const q = search.value.trim().toLowerCase()
  return payments.value.filter((item) => {
    const text = [item.orderId, item.customerName, item.method, item.status].filter(Boolean).join(' ').toLowerCase()
    const matchSearch = !q || text.includes(q)
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

function emptyForm() {
  return { method: '', amount: 0, status: 'UNPAID' }
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ'
}

function formatDateLabel(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}

function paymentBadgeClass(status) {
  if (status === 'PAID') return 'bg-success'
  if (status === 'FAILED') return 'bg-danger'
  if (status === 'REFUNDED') return 'bg-info text-dark'
  return 'bg-secondary'
}

onMounted(fetchPayments)

async function fetchPayments() {
  const { data } = await axios.get('/api/admin/payments')
  payments.value = data
}

function openForm(item) {
  editing.value = item
  form.value = { method: item.method || '', amount: item.amount ?? 0, status: item.status || 'UNPAID' }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
}

async function save() {
  try {
    await axios.put(`/api/admin/payments/${editing.value.id}`, form.value)
    toast.success('Cập nhật thanh toán thành công!')
    closeForm()
    fetchPayments()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi cập nhật thanh toán!')
  }
}

defineExpose({ refresh: fetchPayments })
</script>
