<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">User control</span>
        <h4 class="admin-page-title mb-0">Quản lý người dùng</h4>
        <p>Quản lý vai trò, trạng thái tài khoản và chỉnh sửa thông tin người dùng trong hệ thống.</p>
      </div>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card">
        <div class="admin-stat-label">Tổng user</div>
        <div class="admin-stat-value">{{ users.length }}</div>
        <div class="admin-stat-meta">user</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Admin</div>
        <div class="admin-stat-value">{{ adminCount }}</div>
        <div class="admin-stat-meta">ROLE_ADMIN</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Hoạt động</div>
        <div class="admin-stat-value">{{ activeCount }}</div>
        <div class="admin-stat-meta">status = 1</div>
      </div>
    </div>

    <div class="admin-toolbar">
      <input v-model="search" type="text" class="form-control form-control-sm" placeholder="Tìm email, tên, số điện thoại..." style="max-width: 340px;">
      <select v-model="roleFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả vai trò</option>
        <option value="ROLE_USER">ROLE_USER</option>
        <option value="ROLE_ADMIN">ROLE_ADMIN</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">Sửa người dùng</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <form class="row g-3" @submit.prevent="saveUser">
        <div class="col-md-4">
          <label class="form-label small fw-bold">Họ tên</label>
          <input v-model="form.fullName" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Email</label>
          <input v-model="form.email" type="email" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Số điện thoại</label>
          <input v-model="form.phone" class="form-control form-control-sm">
        </div>
        <div class="col-md-8">
          <label class="form-label small fw-bold">Ảnh đại diện</label>
          <div class="admin-avatar-editor">
            <div class="admin-avatar-editor__preview">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" :alt="form.fullName || 'Avatar người dùng'">
              <i v-else class="bi bi-person"></i>
            </div>
            <div class="admin-avatar-editor__controls">
              <div class="d-flex flex-wrap gap-2">
                <button type="button" class="btn btn-y2k-outline btn-sm" @click="avatarInput?.click()"><i class="bi bi-upload me-1"></i>Tải ảnh lên</button>
                <button v-if="form.avatarUrl" type="button" class="btn btn-outline-danger btn-sm" @click="clearAvatar"><i class="bi bi-trash me-1"></i>Xóa ảnh</button>
              </div>
              <input ref="avatarInput" type="file" accept="image/png,image/jpeg,image/webp" hidden @change="handleAvatarFile">
              <input v-model="form.avatarUrl" class="form-control form-control-sm mt-2" placeholder="Hoặc dán URL ảnh..."><small>PNG, JPG, WEBP · ảnh sẽ hiển thị dạng tròn.</small>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Vai trò</label>
          <select v-model="form.role" class="form-select form-select-sm">
            <option value="ROLE_USER">ROLE_USER</option>
            <option value="ROLE_ADMIN">ROLE_ADMIN</option>
          </select>
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <label class="admin-switch">
            <input v-model="form.status" type="checkbox" :disabled="editingUser?.role === 'ROLE_ADMIN'">
            <span></span>
            <strong>Hoạt động</strong>
          </label>
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <label class="admin-switch">
            <input v-model="form.emailVerified" type="checkbox">
            <span></span>
            <strong>Email đã xác nhận</strong>
          </label>
        </div>
        <div v-if="editingUser?.role === 'ROLE_ADMIN'" class="col-12">
          <div class="alert alert-warning py-2 mb-0 small">
            Tài khoản admin được bảo vệ, không thể khóa từ trang quản lý.
          </div>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu người dùng</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div>
          <strong>Danh sách người dùng</strong>
          <span>{{ filtered.length }} kết quả</span>
        </div>
        <span class="admin-db-chip">user</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="user in filtered" :key="user.id" class="admin-entity-row">
          <div class="admin-row-icon admin-user-avatar">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" :alt="user.fullName || user.email">
            <i v-else class="bi bi-person"></i>
          </div>
          <div class="admin-row-main">
            <div class="admin-row-title">
              {{ user.fullName || 'Chưa có tên' }}
              <span class="badge" :class="user.status === false ? 'bg-secondary' : 'bg-success'">
                {{ user.status === false ? 'Khóa' : 'Hoạt động' }}
              </span>
            </div>
            <div class="admin-row-sub">{{ user.email }}</div>
            <div class="admin-row-note">{{ user.phone || 'Chưa có SĐT' }} · {{ user.emailVerified ? 'Email đã xác nhận' : 'Chưa xác nhận' }} · tạo {{ formatDate(user.createdAt) || '—' }}</div>
          </div>
          <div class="admin-row-meta">
            <span class="admin-db-chip">{{ user.role || 'ROLE_USER' }}</span>
            <button
              class="btn btn-sm"
              :class="user.status === false ? 'btn-outline-secondary' : 'btn-outline-success'"
              :disabled="user.role === 'ROLE_ADMIN'"
              :title="user.role === 'ROLE_ADMIN' ? 'Không thể khóa tài khoản admin' : ''"
              @click="toggleStatus(user)"
            >
              {{ user.status === false ? 'Mở khóa' : 'Khóa' }}
            </button>
          </div>
          <div class="admin-row-actions">
            <button class="btn btn-sm btn-outline-dark" @click="openForm(user)"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-sm btn-outline-danger" :disabled="user.email === authStore.user?.email" @click="remove(user)">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </article>
        <div v-if="!filtered.length" class="admin-empty-state">
          <i class="bi bi-people"></i>
          <strong>Không có người dùng phù hợp</strong>
          <span>Thử đổi từ khóa hoặc bộ lọc vai trò.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { formatDate } from '../../composables/useFormat'
import { useToastStore } from '../../stores/toast'
import { useConfirmStore } from '../../stores/confirm'
import { useAuthStore } from '../../stores/auth'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const authStore = useAuthStore()
const users = ref([])
const search = ref('')
const roleFilter = ref('')
const showForm = ref(false)
const editingUser = ref(null)
const form = ref(emptyForm())
const avatarInput = ref(null)

const adminCount = computed(() => users.value.filter((user) => user.role === 'ROLE_ADMIN').length)
const activeCount = computed(() => users.value.filter((user) => user.status !== false).length)
const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  return users.value.filter((user) => {
    const matchSearch = !q ||
      user.email?.toLowerCase().includes(q) ||
      user.fullName?.toLowerCase().includes(q) ||
      user.phone?.includes(q)
    const matchRole = !roleFilter.value || user.role === roleFilter.value
    return matchSearch && matchRole
  })
})

function emptyForm() {
  return { fullName: '', email: '', phone: '', avatarUrl: '', emailVerified: false, role: 'ROLE_USER', status: true }
}

onMounted(fetchUsers)

async function fetchUsers() {
  const { data } = await axios.get('/api/admin/users')
  users.value = data
}

function openForm(user) {
  editingUser.value = user
  form.value = {
    fullName: user.fullName || '',
    email: user.email || '',
    phone: user.phone || '',
    avatarUrl: user.avatarUrl || '',
    emailVerified: user.emailVerified === true,
    role: user.role || 'ROLE_USER',
    status: user.status !== false
  }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingUser.value = null
  form.value = emptyForm()
}

function clearAvatar() {
  form.value.avatarUrl = ''
  if (avatarInput.value) avatarInput.value.value = ''
}

function handleAvatarFile(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    toast.error('Vui lòng chọn một tệp hình ảnh hợp lệ.')
    return
  }
  if (file.size > 4 * 1024 * 1024) {
    toast.error('Ảnh đại diện không được vượt quá 4MB.')
    return
  }
  const reader = new FileReader()
  reader.onload = () => { form.value.avatarUrl = String(reader.result || '') }
  reader.readAsDataURL(file)
}

async function saveUser() {
  if (!editingUser.value) return
  try {
    await axios.put(`/api/admin/users/${editingUser.value.id}`, form.value)
    toast.success('Đã lưu người dùng!')
    closeForm()
    fetchUsers()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể lưu người dùng!')
  }
}

async function toggleStatus(user) {
  if (user.role === 'ROLE_ADMIN') {
    toast.error('Không thể khóa tài khoản admin!')
    return
  }
  try {
    await axios.put(`/api/admin/users/${user.id}`, { ...user, status: !(user.status === false) })
    toast.success(`Đã cập nhật trạng thái cho ${user.email}`)
    fetchUsers()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể cập nhật trạng thái!')
    fetchUsers()
  }
}

async function remove(user) {
  const ok = await confirmStore.open({
    title: 'Xóa người dùng',
    message: `Xóa tài khoản "${user.email}"?`,
    confirmText: 'Xóa',
    variant: 'danger'
  })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/users/${user.id}`)
    toast.success('Đã xóa người dùng')
    fetchUsers()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa!')
  }
}

defineExpose({ refresh: fetchUsers })
</script>
