<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Category control</span>
        <h4 class="admin-page-title mb-0">Quản lý danh mục</h4>
        <p>Danh mục chung là nhóm lớn, danh mục sản phẩm là tag con được gắn theo từng danh mục cha.</p>
      </div>
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm danh mục
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card">
        <div class="admin-stat-label">Danh mục chung</div>
        <div class="admin-stat-value">{{ generalCount }}</div>
        <div class="admin-stat-meta">GENERAL</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Danh mục sản phẩm</div>
        <div class="admin-stat-value">{{ tagCount }}</div>
        <div class="admin-stat-meta">TAG</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Đang hiển thị</div>
        <div class="admin-stat-value">{{ activeCount }}</div>
        <div class="admin-stat-meta">status = 1</div>
      </div>
    </div>

    <div class="admin-toolbar">
      <input
          v-model="search"
          type="text"
          class="form-control form-control-sm"
          placeholder="Tìm danh mục..."
          style="max-width: 280px;"
      >
      <select v-model="typeFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả loại</option>
        <option value="GENERAL">Danh mục chung</option>
        <option value="TAG">Danh mục sản phẩm</option>
      </select>
      <select v-model="statusFilter" class="form-select form-select-sm" style="max-width: 180px;">
        <option value="">Tất cả trạng thái</option>
        <option value="active">Hoạt động</option>
        <option value="hidden">Đã ẩn</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <h5 class="fw-bold mb-0">{{ editing ? 'Sửa danh mục' : 'Thêm danh mục' }}</h5>
        <button type="button" class="btn btn-y2k-outline btn-sm" @click="closeForm">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <form @submit.prevent="save" class="row g-3">
        <div class="col-md-4">
          <label class="form-label small fw-bold">Tên danh mục</label>
          <input v-model="form.name" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Mô tả</label>
          <input v-model="form.description" class="form-control form-control-sm" placeholder="Ví dụ: Quần jeans, quần dài...">
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-bold">Loại</label>
          <select v-model="form.categoryType" class="form-select form-select-sm">
            <option value="GENERAL">Danh mục chung</option>
            <option value="TAG">Danh mục sản phẩm</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-bold">Hiển thị</label>
          <label class="admin-switch">
            <input v-model="form.status" type="checkbox">
            <span></span>
            <strong>Hoạt động</strong>
          </label>
        </div>
        <div v-if="form.categoryType === 'TAG'" class="col-md-4">
          <label class="form-label small fw-bold">Danh mục cha</label>
          <select v-model.number="form.parentCategoryId" class="form-select form-select-sm" required>
            <option :value="null" disabled>Chọn danh mục chung</option>
            <option v-for="category in generalCategories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>

          <div class="form-text">Danh mục sản phẩm sẽ được gắn dưới danh mục cha tương ứng.</div>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu danh mục</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-list-card">
      <div class="admin-list-header">
        <div>
          <strong>Cấu trúc danh mục</strong>
          <span>{{ filteredCategories.length }} kết quả</span>
        </div>
        <span class="admin-db-chip">categories</span>
      </div>

      <div class="admin-category-rail">
        <article v-for="group in groupedCategories" :key="group.id" class="admin-category-group-card">
          <div class="admin-category-group-card__head">
            <div>
              <div class="admin-row-title mb-1">
                {{ group.name }}
                <span class="badge bg-info-subtle text-dark">Chung</span>
                <span class="badge" :class="group.status === false ? 'bg-secondary' : 'bg-success'">
                  {{ group.status === false ? 'Ẩn' : 'Hoạt động' }}
                </span>
              </div>
              <div class="admin-row-sub">{{ group.description || 'Không có mô tả' }}</div>
            </div>
            <div class="admin-row-actions">
              <button class="btn btn-sm btn-outline-dark" @click="openForm(group)"><i class="bi bi-pencil"></i></button>
              <button class="btn btn-sm btn-outline-danger" @click="remove(group.id)"><i class="bi bi-trash"></i></button>
            </div>
          </div>

          <div class="admin-category-tags">
            <span v-if="group.children.length === 0" class="text-secondary small">Chưa có danh mục con.</span>
            <template v-else>
              <span
                  v-for="tag in group.children"
                  :key="tag.id"
                  class="admin-category-tag"
              >
                <span>
                  <strong>{{ tag.name }}</strong>
                  <small>{{ tag.description || 'Danh mục sản phẩm' }}</small>
                </span>
                <span class="admin-category-tag__actions">
                  <span class="badge bg-light text-dark border">{{ tag.productCount || 0 }} sp</span>
                  <button type="button" class="btn btn-sm btn-outline-dark" title="Sửa danh mục con" @click="openForm(tag)"><i class="bi bi-pencil"></i></button>
                  <button type="button" class="btn btn-sm btn-outline-danger" title="Xóa danh mục con" @click="remove(tag.id)"><i class="bi bi-trash"></i></button>
                </span>
              </span>
            </template>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { useToastStore } from '../../stores/toast'
import { useConfirmStore } from '../../stores/confirm'
import { normalizeCategory } from '../../composables/useCatalog'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const categories = ref([])
const search = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const showForm = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

// Lấy các danh mục sản phẩm (TAG) phù hợp với bộ lọc
const visibleTagCategories = computed(() =>
    categories.value.filter((item) =>
        // Chỉ lấy TAG và phải thỏa điều kiện lọc
        item.categoryType === 'TAG' && matchesFilter(item)
    )
)

// Lấy các danh mục chung (GENERAL)
const generalCategories = computed(() =>
    categories.value.filter((item) => {
      // Nếu là TAG thì bỏ qua vì đây là danh mục con
      if (item.categoryType === 'TAG') return false
      // Kiểm tra danh mục này có TAG con phù hợp bộ lọc không
      const hasMatchingChild = visibleTagCategories.value.some(
          (tag) => Number(tag.parentCategoryId) === Number(item.id)
      )
      // Giữ lại nếu bản thân phù hợp hoặc có TAG con phù hợp
      return matchesFilter(item) || hasMatchingChild
    })
)

// Đặt tên khác cho visibleTagCategories để dễ sử dụng
const tagCategories = visibleTagCategories

// Ghép danh mục cha với các danh mục con tương ứng
const groupedCategories = computed(() =>
    generalCategories.value.map((parent) => ({
      // Giữ toàn bộ thông tin của danh mục cha
      ...parent,
      // Lấy các TAG có parentCategoryId trùng với id của cha
      children: tagCategories.value.filter(
          (tag) => Number(tag.parentCategoryId) === Number(parent.id)
      )
    }))
)

// danh mục đang hiển thị
const activeCount = computed(() => categories.value.filter((item) => item.status !== false).length)
// danh mục chung
const generalCount = computed(() => categories.value.filter((item) => item.categoryType !== 'TAG').length)
// danh mục sản phẩm
const tagCount = computed(() => categories.value.filter((item) => item.categoryType === 'TAG').length)
// hàm lọc dựa theo điều kiện mà ng dùng chọn danh mục chung , danh mục con hoặc all
const filteredCategories = computed(() => categories.value.filter(matchesFilter))

onMounted(fetchCategories)

// lấy danh mục từ backend đưa vào categories.value
async function fetchCategories() {
  const { data } = await axios.get('/api/admin/categories')
  categories.value = (data || []).map(normalizeCategory)
}

// kiểm tra bộ lọc có phù hợp với danh mục hay không
function matchesFilter(item) {
  // Lấy từ khóa tìm kiếm và chuyển thành chữ thường
  const q = search.value.trim().toLowerCase()
  // Kiểm tra từ khóa có trong tên, mô tả hoặc danh mục cha
  const matchesSearch =
      !q ||
      item.name?.toLowerCase().includes(q) ||
      item.description?.toLowerCase().includes(q) ||
      item.parentCategoryName?.toLowerCase().includes(q)
  // Kiểm tra loại danh mục: GENERAL hoặc TAG
  // Chưa chọn loại thì cho qua tất cả
  const matchesType =
      !typeFilter.value || item.categoryType === typeFilter.value
  // Kiểm tra trạng thái: active hoặc hidden
  // Chưa chọn trạng thái thì cho qua tất cả
  const matchesStatus =
      !statusFilter.value ||
      (statusFilter.value === 'active' && item.status !== false) ||
      (statusFilter.value === 'hidden' && item.status === false)
  // Phải thỏa cả 3 điều kiện mới được giữ lại
  return matchesSearch && matchesType && matchesStatus
}

function emptyForm() {
  return { name: '', description: '', categoryType: 'GENERAL', parentCategoryId: null, status: true }
}

function openForm(category = null) {
  editing.value = category // lấy dữ liệu category đưa vao editting
  form.value = category
      ? {
        name: category.name,
        description: category.description || '',
        categoryType: category.categoryType || 'GENERAL',
        parentCategoryId: category.parentCategoryId || null,
        status: category.status ?? true
      }
      : emptyForm()
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
}

async function save() {
  try {
    const payload = {
      ...form.value,
      parentCategoryId: form.value.categoryType === 'TAG' ? form.value.parentCategoryId : null
    }
    if (editing.value) {
      await axios.put(`/api/admin/categories/${editing.value.id}`, payload)
    } else {
      await axios.post('/api/admin/categories', payload)
    }
    toast.success('Lưu danh mục thành công!')
    closeForm()
    await fetchCategories()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi lưu danh mục!')
  }
}

async function remove(id) {
  const ok = await confirmStore.open({
    title: 'Xóa danh mục',
    message: 'Danh mục phải không còn sản phẩm hoặc liên kết con.',
    confirmText: 'Xóa'
  })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/categories/${id}`)
    toast.success('Xóa danh mục thành công!')
    await fetchCategories()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa danh mục!')
  }
}

defineExpose({ refresh: fetchCategories })
</script>

<style scoped>
.admin-category-rail {
  display: grid;
  gap: 1rem;
}

.admin-category-group-card,
.admin-orphan-box {
  border: 1px solid rgba(20, 24, 40, 0.08);
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f7faff 100%);
  padding: 1rem;
}

.admin-category-group-card__head,
.admin-orphan-box__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.admin-category-tags {
  display: grid;
  gap: 0.75rem;
}

.admin-category-tag {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.8rem 0.95rem;
  border-radius: 16px;
  border: 1px solid rgba(20, 24, 40, 0.08);
  background: #fff;
}

.admin-category-tag strong {
  display: block;
}

.admin-category-tag small {
  display: block;
  color: #6c757d;
  margin-top: 0.15rem;
}

.admin-category-tag__actions {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  flex: 0 0 auto;
}

.admin-category-tag__actions .btn {
  width: 32px;
  height: 32px;
  display: inline-grid;
  place-items: center;
  padding: 0;
  border-radius: .55rem;
}
</style>