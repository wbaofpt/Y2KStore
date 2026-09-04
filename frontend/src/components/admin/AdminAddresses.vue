<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Address control</span>
        <h4 class="admin-page-title mb-0">Quản lý địa chỉ</h4>
        <p>Kiểm soát địa chỉ giao hàng, địa chỉ mặc định và thông tin người dùng liên kết.</p>
      </div>
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm địa chỉ
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card"><div class="admin-stat-label">Tổng địa chỉ</div><div class="admin-stat-value">{{ addresses.length }}</div><div class="admin-stat-meta">address</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Mặc định</div><div class="admin-stat-value">{{ defaultCount }}</div><div class="admin-stat-meta">is_default = 1</div></div>
      <div class="admin-stat-card"><div class="admin-stat-label">Người dùng</div><div class="admin-stat-value">{{ userCount }}</div><div class="admin-stat-meta">có địa chỉ</div></div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm tên, tỉnh, quận, địa chỉ..." style="max-width: 360px;">
      <select v-model="defaultFilter" class="form-select form-select-sm" style="max-width: 190px;">
        <option value="">Tất cả</option>
        <option value="default">Mặc định</option>
        <option value="normal">Không mặc định</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">{{ editing ? 'Sửa địa chỉ' : 'Thêm địa chỉ' }}</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm"><i class="bi bi-x-lg"></i></button>
      </div>
      <form class="row g-3" @submit.prevent="save">
        <div class="col-md-4">
          <label class="form-label small fw-bold">Người dùng</label>
          <select v-model="form.userId" class="form-select form-select-sm" required>
            <option v-for="user in users" :key="user.id" :value="user.id">{{ user.fullName || user.email }}</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Tỉnh / Thành phố</label>
          <input v-model="form.province" class="form-control form-control-sm">
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Quận / Huyện</label>
          <input v-model="form.district" class="form-control form-control-sm">
        </div>
        <div class="col-md-6">
          <label class="form-label small fw-bold">Phường / Xã</label>
          <input v-model="form.ward" class="form-control form-control-sm">
        </div>
        <div class="col-md-6">
          <label class="form-label small fw-bold">Chi tiết</label>
          <input v-model="form.detail" class="form-control form-control-sm">
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <label class="admin-switch">
            <input v-model="form.isDefault" type="checkbox">
            <span></span>
            <strong>Mặc định</strong>
          </label>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu địa chỉ</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách địa chỉ</strong><span>{{ filteredAddresses.length }} kết quả</span></div>
        <span class="admin-db-chip">address</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="item in filteredAddresses" :key="item.id" class="admin-entity-row">
          <div class="admin-row-icon"><i class="bi bi-geo-alt"></i></div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              {{ item.userFullName || `User #${item.userId || '-'}` }}
              <span class="badge" :class="item.isDefault ? 'bg-success' : 'bg-secondary'">{{ item.isDefault ? 'Mặc định' : 'Phụ' }}</span>
            </div>
            <div class="admin-row-sub">{{ formatAddress(item) || 'Chưa có chi tiết địa chỉ' }}</div>
          </div>
          <div class="admin-row-meta">
            <span>ID #{{ item.id }}</span>
            <span>{{ item.province || 'Chưa có tỉnh/thành' }}</span>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click="openForm(item)"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-sm btn-outline-danger" @click="remove(item.id)"><i class="bi bi-trash"></i></button>
          </div>
        </article>
        <div v-if="!filteredAddresses.length" class="admin-empty-state">
          <i class="bi bi-geo-alt"></i>
          <strong>Không có địa chỉ phù hợp</strong>
          <span>Thử đổi từ khóa hoặc bộ lọc.</span>
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
const addresses = ref([])
const users = ref([])
const search = ref('')
const defaultFilter = ref('')
const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

const defaultCount = computed(() => addresses.value.filter((item) => item.isDefault).length)
const userCount = computed(() => new Set(addresses.value.map((item) => item.userId).filter(Boolean)).size)
const filteredAddresses = computed(() => {
  const q = search.value.trim().toLowerCase()
  return addresses.value.filter((item) => {
    const text = [item.userFullName, item.detail, item.ward, item.district, item.province].filter(Boolean).join(' ').toLowerCase()
    const matchSearch = !q || text.includes(q)
    const matchDefault = !defaultFilter.value ||
      (defaultFilter.value === 'default' && item.isDefault) ||
      (defaultFilter.value === 'normal' && !item.isDefault)
    return matchSearch && matchDefault
  })
})

function emptyForm() {
  return { userId: null, province: '', district: '', ward: '', detail: '', isDefault: false }
}

onMounted(() => {
  fetchAddresses()
  fetchUsers()
})

async function fetchAddresses() {
  const { data } = await axios.get('/api/admin/addresses')
  addresses.value = data
}

async function fetchUsers() {
  const { data } = await axios.get('/api/admin/users')
  users.value = data
}

function openForm(item = null) {
  editing.value = item
  form.value = item
    ? {
        userId: item.userId ?? null,
        province: item.province || '',
        district: item.district || '',
        ward: item.ward || '',
        detail: item.detail || '',
        isDefault: item.isDefault ?? false
      }
    : emptyForm()
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
}

function formatAddress(item) {
  return [item.detail, item.ward, item.district, item.province].filter(Boolean).join(', ')
}

async function save() {
  try {
    if (editing.value) await axios.put(`/api/admin/addresses/${editing.value.id}`, form.value)
    else await axios.post('/api/admin/addresses', form.value)
    toast.success('Lưu địa chỉ thành công!')
    closeForm()
    fetchAddresses()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi lưu địa chỉ!')
  }
}

async function remove(id) {
  const ok = await confirmStore.open({
    title: 'Xóa địa chỉ',
    message: 'Thao tác này sẽ xóa địa chỉ khỏi hệ thống.',
    confirmText: 'Xóa'
  })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/addresses/${id}`)
    toast.success('Xóa địa chỉ thành công!')
    fetchAddresses()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa địa chỉ!')
  }
}

defineExpose({ refresh: fetchAddresses })
</script>
