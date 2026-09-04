<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Coupon control</span>
        <h4 class="admin-page-title mb-0">Quản lý mã giảm giá</h4>
        <p>Quản lý mã coupon, giá trị giảm, hạn dùng và khuyến mãi liên kết.</p>
      </div>
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm mã
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card"><div class="admin-stat-label">Tổng mã</div><div class="admin-stat-value">{{ coupons.length }}</div><div class="admin-stat-meta">coupons</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Hoạt động</div><div class="admin-stat-value">{{ activeCount }}</div><div class="admin-stat-meta">status = 1</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Đã ẩn</div><div class="admin-stat-value">{{ hiddenCount }}</div><div class="admin-stat-meta">status = 0</div></div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm mã coupon..." style="max-width: 280px;">
      <select v-model="statusFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả trạng thái</option>
        <option value="active">Hoạt động</option>
        <option value="hidden">Đã ẩn</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">{{ editing ? 'Sửa mã giảm giá' : 'Thêm mã giảm giá' }}</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm"><i class="bi bi-x-lg"></i></button>
      </div>
      <form class="row g-3" @submit.prevent="save">
        <div class="col-md-4">
          <label class="form-label small fw-bold">Mã coupon</label>
          <input v-model="form.couponCode" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Khuyến mãi</label>
          <select v-model="form.promotionId" class="form-select form-select-sm">
            <option :value="null">Không gắn khuyến mãi</option>
            <option v-for="promotion in promotions" :key="promotion.id" :value="promotion.id">{{ promotion.promotionName }}</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Giảm giá cố định</label>
          <input v-model.number="form.discountValue" type="number" min="0" step="1" class="form-control form-control-sm">
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Ngày hết hạn</label>
          <input v-model="form.expireDate" type="datetime-local" class="form-control form-control-sm">
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <label class="admin-switch">
            <input v-model="form.status" type="checkbox">
            <span></span>
            <strong>Bật</strong>
          </label>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu mã</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách mã giảm giá</strong><span>{{ filteredCoupons.length }} kết quả</span></div>
        <span class="admin-db-chip">coupons</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="item in filteredCoupons" :key="item.id" class="admin-entity-row">
          <div class="admin-row-icon"><i class="bi bi-ticket-perforated"></i></div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              {{ item.couponCode }}
              <span class="badge" :class="item.status === false ? 'bg-secondary' : 'bg-success'">{{ item.status === false ? 'Ẩn' : 'Hoạt động' }}</span>
            </div>
            <div class="admin-row-sub">{{ item.promotionName || 'Không gắn khuyến mãi' }}</div>
          </div>
          <div class="admin-row-meta">
            <span>Giảm: <strong class="text-y2k-sale">{{ formatMoney(item.discountValue) }}</strong></span>
            <span>Hết hạn: <strong>{{ formatDateLabel(item.expireDate) || 'Không giới hạn' }}</strong></span>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click="openForm(item)"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-sm btn-outline-danger" @click="remove(item.id)"><i class="bi bi-trash"></i></button>
          </div>
        </article>
        <div v-if="!filteredCoupons.length" class="admin-empty-state">
          <i class="bi bi-ticket-perforated"></i>
          <strong>Không có mã phù hợp</strong>
          <span>Thử đổi từ khóa hoặc thêm mã mới.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useToastStore } from '../../stores/toast'
import { useConfirmStore } from '../../stores/confirm'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const coupons = ref([])
const promotions = ref([])
const search = ref('')
const statusFilter = ref('')
const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

const activeCount = computed(() => coupons.value.filter((item) => item.status !== false).length)
const hiddenCount = computed(() => coupons.value.filter((item) => item.status === false).length)
const filteredCoupons = computed(() => {
  const q = search.value.trim().toLowerCase()
  return coupons.value.filter((item) => {
    const matchSearch = !q || item.couponCode?.toLowerCase().includes(q) || item.promotionName?.toLowerCase().includes(q)
    const matchStatus = !statusFilter.value ||
      (statusFilter.value === 'active' && item.status !== false) ||
      (statusFilter.value === 'hidden' && item.status === false)
    return matchSearch && matchStatus
  })
})

function emptyForm() {
  return { promotionId: null, couponCode: '', discountValue: 0, expireDate: '', status: true }
}

function toDateTimeLocal(value) {
  if (!value) return ''
  const date = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatDateLabel(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ'
}

onMounted(() => {
  fetchCoupons()
  fetchPromotions()
})

async function fetchCoupons() {
  const { data } = await axios.get('/api/admin/coupons')
  coupons.value = data
}

async function fetchPromotions() {
  const { data } = await axios.get('/api/admin/promotions')
  promotions.value = data
}

function openForm(item = null) {
  editing.value = item
  form.value = item
    ? {
        promotionId: item.promotionId ?? null,
        couponCode: item.couponCode || '',
        discountValue: item.discountValue ?? 0,
        expireDate: toDateTimeLocal(item.expireDate),
        status: item.status ?? true
      }
    : emptyForm()
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
}

function toPayload() {
  return {
    ...form.value,
    promotionId: form.value.promotionId === '' ? null : form.value.promotionId,
    expireDate: form.value.expireDate ? new Date(form.value.expireDate).toISOString() : null
  }
}

async function save() {
  try {
    const payload = toPayload()
    if (editing.value) await axios.put(`/api/admin/coupons/${editing.value.id}`, payload)
    else await axios.post('/api/admin/coupons', payload)
    toast.success('Lưu mã giảm giá thành công!')
    closeForm()
    fetchCoupons()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi lưu mã giảm giá!')
  }
}

async function remove(id) {
  const ok = await confirmStore.open({
    title: 'Xóa coupon',
    message: 'Coupon đã được dùng trong đơn hàng sẽ không thể xóa.',
    confirmText: 'Xóa'
  })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/coupons/${id}`)
    toast.success('Xóa coupon thành công!')
    fetchCoupons()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa coupon!')
  }
}

defineExpose({ refresh: fetchCoupons })
</script>
