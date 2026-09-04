<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Promotion control</span>
        <h4 class="admin-page-title mb-0">Quản lý khuyến mãi</h4>
        <p>Thiết lập phần trăm giảm giá, thời gian hiệu lực và trạng thái của bảng promotions.</p>
      </div>
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm khuyến mãi
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card"><div class="admin-stat-label">Tổng khuyến mãi</div><div class="admin-stat-value">{{ promotions.length }}</div><div class="admin-stat-meta">promotions</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Hoạt động</div><div class="admin-stat-value">{{ activeCount }}</div><div class="admin-stat-meta">status = 1</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Đã ẩn</div><div class="admin-stat-value">{{ hiddenCount }}</div><div class="admin-stat-meta">status = 0</div></div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm khuyến mãi..." style="max-width: 280px;">
      <select v-model="statusFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả trạng thái</option>
        <option value="active">Hoạt động</option>
        <option value="hidden">Đã ẩn</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">{{ editing ? 'Sửa khuyến mãi' : 'Thêm khuyến mãi' }}</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm"><i class="bi bi-x-lg"></i></button>
      </div>
      <form class="row g-3" @submit.prevent="save">
        <div class="col-md-5">
          <label class="form-label small fw-bold">Tên khuyến mãi</label>
          <input v-model="form.promotionName" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-bold">Giảm (%)</label>
          <input v-model.number="form.discountPercent" type="number" min="0" step="0.01" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-2 d-flex align-items-end">
          <label class="admin-switch">
            <input v-model="form.status" type="checkbox">
            <span></span>
            <strong>Bật</strong>
          </label>
        </div>
        <div class="col-md-3"></div>
        <div class="col-md-6">
          <label class="form-label small fw-bold">Ngày bắt đầu</label>
          <input v-model="form.startDate" type="datetime-local" class="form-control form-control-sm">
        </div>
        <div class="col-md-6">
          <label class="form-label small fw-bold">Ngày kết thúc</label>
          <input v-model="form.endDate" type="datetime-local" class="form-control form-control-sm">
        </div>
        <div class="col-12">
          <label class="form-label small fw-bold">Sản phẩm áp dụng</label>
          <select v-model="selectedProductIds" class="form-select form-select-sm" multiple>
            <option v-for="product in products" :key="product.id" :value="product.id">
              {{ product.name }} · {{ formatPrice(product.price) }}
            </option>
          </select>
          <div class="form-text">Chọn các sản phẩm cần gắn khuyến mãi cho chương trình này.</div>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu khuyến mãi</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách khuyến mãi</strong><span>{{ filteredPromotions.length }} kết quả</span></div>
        <span class="admin-db-chip">promotions</span>
      </div>

      <div class="admin-entity-list">
        <article v-for="item in filteredPromotions" :key="item.id" class="admin-entity-row">
          <div class="admin-row-icon"><i class="bi bi-megaphone"></i></div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              {{ item.promotionName }}
              <span class="badge bg-warning text-dark">-{{ item.discountPercent }}%</span>
              <span class="badge" :class="item.status === false ? 'bg-secondary' : 'bg-success'">{{ item.status === false ? 'Ẩn' : 'Hoạt động' }}</span>
            </div>
            <div class="admin-row-sub">ID #{{ item.id }} · dùng để gắn vào sản phẩm hoặc coupon</div>
          </div>
          <div class="admin-row-meta">
            <span>Bắt đầu: <strong>{{ formatDateLabel(item.startDate) || 'Chưa đặt' }}</strong></span>
            <span>Kết thúc: <strong>{{ formatDateLabel(item.endDate) || 'Không giới hạn' }}</strong></span>
            <span>{{ getPromotionProductCount(item.id) }} sản phẩm</span>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click="openForm(item)"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-sm btn-outline-danger" @click="remove(item.id)"><i class="bi bi-trash"></i></button>
          </div>
        </article>
        <div v-if="!filteredPromotions.length" class="admin-empty-state">
          <i class="bi bi-megaphone"></i>
          <strong>Không có khuyến mãi phù hợp</strong>
          <span>Thử đổi bộ lọc hoặc thêm khuyến mãi mới.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { formatPrice } from '../../composables/useFormat'
import { useToastStore } from '../../stores/toast'
import { useConfirmStore } from '../../stores/confirm'
import { normalizeProduct } from '../../composables/useCatalog'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const promotions = ref([])
const products = ref([])
const search = ref('')
const statusFilter = ref('')
const showForm = ref(false)
const editing = ref(null)
const selectedProductIds = ref([])
const form = ref(emptyForm())

const activeCount = computed(() => promotions.value.filter((item) => item.status !== false).length)
const hiddenCount = computed(() => promotions.value.filter((item) => item.status === false).length)
const filteredPromotions = computed(() => {
  const q = search.value.trim().toLowerCase()
  return promotions.value.filter((item) => {
    const matchSearch = !q || item.promotionName?.toLowerCase().includes(q)
    const matchStatus = !statusFilter.value ||
      (statusFilter.value === 'active' && item.status !== false) ||
      (statusFilter.value === 'hidden' && item.status === false)
    return matchSearch && matchStatus
  })
})

function emptyForm() {
  return { promotionName: '', discountPercent: 0, startDate: '', endDate: '', status: true }
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

onMounted(fetchPromotions)
onMounted(fetchProducts)

async function fetchPromotions() {
  const { data } = await axios.get('/api/admin/promotions')
  promotions.value = data
}

async function fetchProducts() {
  const { data } = await axios.get('/api/admin/products')
  products.value = (data || []).map(normalizeProduct)
  if (showForm.value && editing.value?.id) {
    selectedProductIds.value = products.value
      .filter((product) => product.promotionIds?.includes(editing.value.id))
      .map((product) => product.id)
  }
}

function openForm(item = null) {
  editing.value = item
  form.value = item
    ? {
        promotionName: item.promotionName || '',
        discountPercent: item.discountPercent ?? 0,
        startDate: toDateTimeLocal(item.startDate),
        endDate: toDateTimeLocal(item.endDate),
        status: item.status ?? true
      }
    : emptyForm()
  selectedProductIds.value = item?.id
    ? products.value.filter((product) => product.promotionIds?.includes(item.id)).map((product) => product.id)
    : []
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
  selectedProductIds.value = []
}

function toPayload() {
  return {
    ...form.value,
    startDate: form.value.startDate ? new Date(form.value.startDate).toISOString() : null,
    endDate: form.value.endDate ? new Date(form.value.endDate).toISOString() : null
  }
}

async function save() {
  try {
    const payload = toPayload()
    const response = editing.value
      ? await axios.put(`/api/admin/promotions/${editing.value.id}`, payload)
      : await axios.post('/api/admin/promotions', payload)
    const promotionId = response.data?.id || editing.value?.id
    if (promotionId) {
      await axios.put(`/api/admin/promotions/${promotionId}/products`, {
        productIds: selectedProductIds.value
      })
    }
    toast.success('Lưu khuyến mãi thành công!')
    closeForm()
    await Promise.all([fetchPromotions(), fetchProducts()])
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi lưu khuyến mãi!')
  }
}

function getPromotionProductCount(promotionId) {
  return products.value.filter((product) => product.promotionIds?.includes(promotionId)).length
}

async function remove(id) {
  const ok = await confirmStore.open({
    title: 'Xóa khuyến mãi',
    message: 'Khuyến mãi đang gắn với sản phẩm hoặc coupon sẽ không thể xóa.',
    confirmText: 'Xóa'
  })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/promotions/${id}`)
    toast.success('Xóa khuyến mãi thành công!')
    fetchPromotions()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa khuyến mãi!')
  }
}

defineExpose({ refresh: fetchPromotions })
</script>
