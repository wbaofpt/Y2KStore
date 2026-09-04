<template>
  <div class="admin-section">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Products control</span>
        <h4 class="admin-page-title mb-0">Quản lý sản phẩm</h4>
        <p>Kiểm soát bảng products, product_categories, product_images, product_variants và product_promotions.</p>
      </div>
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm sản phẩm
      </button>
    </div>

    <div class="admin-mini-stats">
      <div class="admin-stat-card">
        <div class="admin-stat-label">Tổng sản phẩm</div>
        <div class="admin-stat-value">{{ products.length }}</div>
        <div class="admin-stat-meta">products</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Sắp hết hàng</div>
        <div class="admin-stat-value">{{ lowStockCount }}</div>
        <div class="admin-stat-meta">stock dưới 10</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-label">Đang khuyến mãi</div>
        <div class="admin-stat-value">{{ promotedCount }}</div>
        <div class="admin-stat-meta">product_promotions</div>
      </div>
    </div>

    <div class="admin-toolbar">
      <input
        v-model="search"
        type="text"
        class="form-control form-control-sm"
        placeholder="Tìm tên sản phẩm..."
        style="max-width: 280px;"
      >
      <select v-model.number="categoryFilter" class="form-select form-select-sm" style="max-width: 220px;">
        <option :value="0">Tất cả danh mục</option>
        <optgroup label="Danh mục chung">
          <option v-for="category in generalCategories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </optgroup>
        <optgroup v-if="tagCategories.length" label="Danh mục sản phẩm">
          <option v-for="category in tagCategories" :key="category.id" :value="category.id">
            #{{ category.name }}
          </option>
        </optgroup>
      </select>
      <select v-model.number="statusFilter" class="form-select form-select-sm" style="max-width: 190px;">
        <option :value="-1">Tất cả trạng thái</option>
        <option :value="0">Nháp</option>
        <option :value="1">Đang bán</option>
        <option :value="2">Hết hàng</option>
        <option :value="3">Sắp về</option>
        <option :value="4">Ẩn</option>
      </select>
    </div>

    <div v-if="showForm" class="admin-form-card y2k-animate-in">
      <h5 class="fw-bold mb-3">{{ editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h5>
      <form @submit.prevent="save" class="row g-3">
        <div class="col-md-4">
          <label class="form-label small fw-bold">Tên sản phẩm</label>
          <input v-model="form.name" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Giá bán</label>
          <input v-model.number="form.price" type="number" min="0" class="form-control form-control-sm" required>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Danh mục</label>
          <select v-model.number="form.categoryId" class="form-select form-select-sm" required>
            <option :value="null" disabled>Chọn danh mục chung</option>
            <option v-for="category in generalCategories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
          <div class="form-text">Danh mục chung lưu tại products.category_id, ví dụ: Quần.</div>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Tag sản phẩm</label>
          <select v-model="form.tagIds" class="form-select form-select-sm" multiple>
            <template v-for="group in tagGroups" :key="group.parent.id">
              <optgroup :label="group.parent.name">
                <option v-for="category in group.tags" :key="category.id" :value="category.id">
                  #{{ category.name }}
                </option>
              </optgroup>
            </template>
            <optgroup v-if="orphanTags.length" label="Tag chưa gắn cha">
              <option v-for="category in orphanTags" :key="category.id" :value="category.id">
                #{{ category.name }}
              </option>
            </optgroup>
          </select>
          <div class="form-text">product_categories: chọn nhiều tag con theo đúng danh mục chung đã chọn.</div>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Khuyến mãi áp dụng</label>
          <select v-model="form.promotionIds" class="form-select form-select-sm" multiple>
            <option v-for="promotion in promotions" :key="promotion.id" :value="promotion.id">
              {{ promotion.promotionName }} - {{ promotion.discountPercent }}%
            </option>
          </select>
          <div class="form-text">Bảng product_promotions: giữ Ctrl để chọn nhiều khuyến mãi.</div>
        </div>
        <div class="col-md-8">
          <label class="form-label small fw-bold">Ảnh đại diện</label>
          <input v-model="form.image" class="form-control form-control-sm" placeholder="https://... hoặc tải từ thiết bị">
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Tải ảnh</label>
          <input type="file" accept="image/*" class="form-control form-control-sm" @change="handleImageUpload">
        </div>
        <div v-if="form.image" class="col-12">
          <div class="d-flex align-items-center gap-3 p-2 border rounded bg-white">
            <img :src="form.image" alt="preview" class="rounded border" style="width:72px;height:72px;object-fit:cover">
            <div class="small text-secondary">Xem trước ảnh đại diện đang chọn</div>
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Trạng thái</label>
          <select v-model.number="form.status" class="form-select form-select-sm">
            <option :value="0">Nháp</option>
            <option :value="1">Đang bán</option>
            <option :value="2">Hết hàng</option>
            <option :value="3">Sắp về</option>
            <option :value="4">Ẩn</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-bold">Ngày tạo sản phẩm</label>
          <input v-model="form.createdAt" type="datetime-local" class="form-control form-control-sm">
          <div class="form-text">Sản phẩm trong 14 ngày gần nhất sẽ có nhãn NEW.</div>
        </div>
        <div class="col-12">
          <label class="form-label small fw-bold">Mô tả</label>
          <textarea v-model="form.description" class="form-control form-control-sm" rows="3"></textarea>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="submit" class="btn btn-y2k-primary btn-sm">Lưu</button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeForm">Hủy</button>
        </div>
      </form>
    </div>

    <div class="admin-panel-card p-0 overflow-hidden">
      <div class="table-responsive">
        <table class="table admin-table mb-0">
          <thead>
            <tr>
              <th>Ảnh</th>
              <th>Tên</th>
              <th>Giá</th>
              <th>Danh mục</th>
              <th>Tồn kho</th>
              <th>Ngày tạo</th>
              <th>Khuyến mãi</th>
              <th>Trạng thái</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in filtered" :key="product.id">
              <td><img :src="product.imageUrl || '/summer_y2k.png'" class="rounded border" style="width:48px;height:48px;object-fit:cover"></td>
              <td class="fw-semibold">
                <button class="btn btn-link p-0 text-decoration-none fw-semibold text-dark" @click="openDetail(product)">
                  {{ product.name }}
                </button>
                <div class="small text-secondary">#{{ product.id }}</div>
              </td>
              <td class="text-y2k-sale fw-bold">{{ formatPrice(product.price) }}</td>
              <td>
                <span v-for="name in displayCategoryNames(product)" :key="name" class="badge bg-light text-dark border me-1 mb-1">
                  {{ name }}
                </span>
              </td>
              <td><span :class="{ 'text-y2k-sale fw-bold': Number(product.stock) < 10 }">{{ product.stock ?? 0 }}</span></td>
              <td>
                <span class="small fw-semibold">{{ formatDateLabel(product.createdAt) }}</span>
                <span v-if="isNewArrival(product)" class="badge bg-info text-dark ms-1">NEW</span>
              </td>
              <td>
                <div v-if="product.promotionNames?.length" class="d-flex flex-wrap gap-1">
                  <span v-for="name in product.promotionNames" :key="name" class="badge bg-warning text-dark">{{ name }}</span>
                </div>
                <span v-else class="text-secondary small">—</span>
              </td>
              <td>
                <span class="badge" :class="statusBadgeClass(product)">{{ statusLabel(product) }}</span>
              </td>
              <td class="text-end">
                <button class="btn btn-sm btn-outline-primary me-1" @click="openDetail(product)"><i class="bi bi-sliders"></i></button>
                <button class="btn btn-sm btn-outline-dark me-1" @click="openForm(product)"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" @click="remove(product.id)"><i class="bi bi-trash"></i></button>
              </td>
            </tr>
            <tr v-if="!filtered.length">
              <td colspan="9" class="text-center text-secondary py-4">Không có sản phẩm phù hợp.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="modal">
        <div v-if="selectedProduct" class="y2k-modal-backdrop" @click.self="selectedProduct = null">
          <div class="y2k-modal text-start" style="max-width: 1080px;">
            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
              <div>
                <span class="admin-kicker">Product database control</span>
                <h5 class="fw-bold mb-1">{{ selectedProduct.name }}</h5>
                <p class="text-secondary small mb-0">Mã #{{ selectedProduct.id }} · quản lý dữ liệu liên kết của sản phẩm</p>
              </div>
              <button class="btn btn-y2k-outline btn-sm" @click="selectedProduct = null">Đóng</button>
            </div>

            <div class="admin-product-detail-grid">
              <div class="admin-product-detail-box">
                <div class="d-flex gap-3 align-items-start">
                  <img :src="selectedProduct.imageUrl || '/summer_y2k.png'" class="rounded border" style="width:128px;height:128px;object-fit:cover">
                  <div class="flex-grow-1">
                    <div class="mb-2">
                      <span v-for="name in displayCategoryNames(selectedProduct)" :key="name" class="badge bg-light text-dark border me-2">
                        {{ name }}
                      </span>
                      <span class="badge me-2" :class="statusBadgeClass(selectedProduct)">{{ statusLabel(selectedProduct) }}</span>
                    </div>
                    <div class="fw-bold fs-4 text-y2k-sale">{{ formatPrice(selectedProduct.price) }}</div>
                    <div class="small text-secondary mt-2">{{ selectedProduct.description || 'Chưa có mô tả.' }}</div>
                    <div class="small text-secondary mt-2">
                      Ngày tạo:
                      <span class="fw-semibold text-dark">{{ formatDateLabel(selectedProduct.createdAt) }}</span>
                      <span v-if="isNewArrival(selectedProduct)" class="badge bg-info text-dark ms-1">NEW</span>
                    </div>
                    <div v-if="selectedProduct.promotionName" class="small text-dark mt-2">
                      Khuyến mãi đang hiệu lực:
                      <span class="fw-semibold">{{ selectedProduct.promotionName }}</span>
                      <span v-if="selectedProduct.promotionDiscountPercent">(-{{ selectedProduct.promotionDiscountPercent }}%)</span>
                    </div>
                  </div>
                </div>

                <div class="admin-db-fields mt-3">
                  <span>products</span>
                  <span>product_categories</span>
                  <span>product_images</span>
                  <span>product_variants</span>
                  <span>product_promotions</span>
                </div>
              </div>

              <div class="admin-product-detail-box admin-control-stack">
                <div>
                  <div class="d-flex justify-content-between align-items-center gap-2 mb-2">
                    <h6 class="fw-bold mb-0">Khuyến mãi áp dụng</h6>
                    <button class="btn btn-y2k-outline btn-sm" @click="saveProductPromotions">
                      <i class="bi bi-check2-circle me-1"></i>Lưu
                    </button>
                  </div>
                  <select v-model="selectedPromotionIds" class="form-select form-select-sm" multiple>
                    <option v-for="promotion in promotions" :key="promotion.id" :value="promotion.id">
                      {{ promotion.promotionName }} - {{ promotion.discountPercent }}%
                    </option>
                  </select>
                  <div class="form-text">Bảng product_promotions: giữ Ctrl để chọn nhiều khuyến mãi.</div>
                </div>

                <div class="border-top pt-3">
                  <div class="d-flex justify-content-between align-items-center gap-2 mb-3">
                    <h6 class="fw-bold mb-0">Biến thể sản phẩm</h6>
                    <button class="btn btn-y2k-outline btn-sm" @click="openVariantForm()">
                      <i class="bi bi-plus-lg me-1"></i>Thêm
                    </button>
                  </div>

                  <form v-if="showVariantForm" class="admin-inline-form mb-3" @submit.prevent="saveVariant">
                    <input v-model="variantForm.size" class="form-control form-control-sm" placeholder="Size">
                    <input v-model="variantForm.color" class="form-control form-control-sm" placeholder="Màu">
                    <input v-model.number="variantForm.stock" type="number" min="0" class="form-control form-control-sm" placeholder="Tồn">
                    <input v-model.number="variantForm.price" type="number" min="0" class="form-control form-control-sm" placeholder="Giá">
                    <select v-model="variantForm.status" class="form-select form-select-sm">
                      <option :value="true">Bật</option>
                      <option :value="false">Ẩn</option>
                    </select>
                    <div class="d-flex gap-2">
                      <button class="btn btn-y2k-primary btn-sm" type="submit">Lưu</button>
                      <button class="btn btn-secondary btn-sm" type="button" @click="closeVariantForm">Hủy</button>
                    </div>
                  </form>

                  <div v-if="selectedProduct.variants?.length" class="admin-mini-list mb-4">
                    <div v-for="variant in selectedProduct.variants" :key="variant.id" class="admin-mini-list__item">
                      <div>
                        <div class="fw-semibold">{{ buildVariantLabel(variant) }}</div>
                        <div class="small text-secondary">Tồn kho: {{ variant.stock ?? 0 }}</div>
                      </div>
                      <div class="text-end">
                        <div class="fw-bold">{{ formatPrice(variant.price ?? selectedProduct.price) }}</div>
                        <div class="small" :class="variant.status === false ? 'text-secondary' : 'text-success'">
                          {{ variant.status === false ? 'Ẩn' : 'Hoạt động' }}
                        </div>
                        <div class="d-flex justify-content-end gap-1 mt-2">
                          <button class="btn btn-sm btn-outline-dark" @click="openVariantForm(variant)"><i class="bi bi-pencil"></i></button>
                          <button class="btn btn-sm btn-outline-danger" @click="removeVariant(variant)"><i class="bi bi-trash"></i></button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-secondary small mb-4">Chưa có biến thể.</div>
                </div>

                <div class="border-top pt-3">
                  <div class="d-flex justify-content-between align-items-center gap-2 mb-3">
                    <h6 class="fw-bold mb-0">Hình ảnh phụ</h6>
                    <button class="btn btn-y2k-outline btn-sm" @click="openImageForm()">
                      <i class="bi bi-plus-lg me-1"></i>Thêm
                    </button>
                  </div>

                  <form v-if="showImageForm" class="admin-inline-form admin-inline-form--image mb-3" @submit.prevent="saveImage">
                    <input v-model="imageForm.imageUrl" class="form-control form-control-sm" placeholder="URL hình ảnh">
                    <input type="file" accept="image/*" class="form-control form-control-sm" @change="handleGalleryUpload">
                    <div class="d-flex gap-2">
                      <button class="btn btn-y2k-primary btn-sm" type="submit">Lưu</button>
                      <button class="btn btn-secondary btn-sm" type="button" @click="closeImageForm">Hủy</button>
                    </div>
                  </form>

                  <div v-if="selectedProduct.images?.length" class="admin-image-grid">
                    <div v-for="image in selectedProduct.images" :key="image.id || image.imageUrl" class="admin-image-tile">
                      <img :src="image.imageUrl" alt="product image">
                      <div class="admin-image-actions">
                        <button class="btn btn-sm btn-light" @click="openImageForm(image)"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-light text-danger" @click="removeImage(image)"><i class="bi bi-trash"></i></button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-secondary small">Chưa có hình ảnh phụ.</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { formatPrice } from '../../composables/useFormat'
import { useToastStore } from '../../stores/toast'
import { useConfirmStore } from '../../stores/confirm'
import { isNewProduct, normalizeCategory, normalizeProduct, normalizeProductStatus } from '../../composables/useCatalog'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const products = ref([])
const categories = ref([])
const promotions = ref([])
const search = ref('')
const categoryFilter = ref(0)
const statusFilter = ref(-1)
const showForm = ref(false)
const editing = ref(null)
const selectedProduct = ref(null)
const selectedPromotionIds = ref([])
const showVariantForm = ref(false)
const editingVariant = ref(null)
const variantForm = ref(emptyVariantForm())
const showImageForm = ref(false)
const editingImage = ref(null)
const imageForm = ref(emptyImageForm())
const form = ref(emptyForm())

const filtered = computed(() => {
  const q = search.value.toLowerCase()
  return products.value.filter((product) => {
    const matchSearch = !q || product.name?.toLowerCase().includes(q)
    const matchCategory = !categoryFilter.value || Number(product.categoryId) === Number(categoryFilter.value) || product.categoryIds?.includes(categoryFilter.value)
    const matchStatus = statusFilter.value === -1 || normalizeProductStatus(product.statusCode ?? product.status) === statusFilter.value
    return matchSearch && matchCategory && matchStatus
  })
})

const generalCategories = computed(() => categories.value.filter((item) => item.categoryType !== 'TAG'))
const tagCategories = computed(() => categories.value.filter((item) => item.categoryType === 'TAG'))
const tagGroups = computed(() =>
  generalCategories.value.map((parent) => ({
    parent,
    tags: tagCategories.value.filter((tag) => Number(tag.parentCategoryId) === Number(parent.id))
  })).filter((group) => group.tags.length)
)
const orphanTags = computed(() => tagCategories.value.filter((tag) => !tag.parentCategoryId))

const lowStockCount = computed(() => products.value.filter((product) => Number(product.stock ?? 0) < 10).length)
const promotedCount = computed(() => products.value.filter((product) => product.promotionIds?.length || product.promotionDiscountPercent > 0).length)

function emptyForm() {
  return { name: '', price: 0, image: '', categoryId: null, tagIds: [], categoryIds: [], promotionIds: [], description: '', status: 1, createdAt: toDateTimeLocalValue(new Date()) }
}

function emptyVariantForm() {
  return { size: '', color: '', stock: 0, price: 0, status: true }
}

function emptyImageForm() {
  return { imageUrl: '' }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchPromotions()
})

async function fetchProducts() {
  const { data } = await axios.get('/api/admin/products')
  products.value = data.map(normalizeProduct)
  return products.value
}

async function fetchCategories() {
  const { data } = await axios.get('/api/admin/categories')
  categories.value = data.map(normalizeCategory)
  if (data.length && !form.value.categoryId) form.value.categoryId = generalCategories.value[0]?.id || data[0].id
}

async function fetchPromotions() {
  const { data } = await axios.get('/api/admin/promotions')
  promotions.value = data
}

function openForm(product = null) {
  editing.value = product
  form.value = product
    ? {
        name: product.name,
        price: product.price,
        image: product.imageUrl || '',
        categoryId: product.categoryId,
        tagIds: product.categoryIds?.filter((id) => id !== product.categoryId) || [],
        categoryIds: product.categoryIds?.length ? [...product.categoryIds] : [],
        promotionIds: product.promotionIds?.length ? [...product.promotionIds] : [],
        description: product.description || '',
        status: normalizeProductStatus(product.statusCode ?? product.status),
        createdAt: toDateTimeLocalValue(product.createdAt)
      }
    : { ...emptyForm(), categoryId: generalCategories.value[0]?.id || null }
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editing.value = null
  form.value = emptyForm()
}

function handleImageUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    form.value.image = reader.result || ''
  }
  reader.readAsDataURL(file)
}

function openDetail(product) {
  selectedProduct.value = product
  selectedPromotionIds.value = [...(product.promotionIds || [])]
  closeVariantForm()
  closeImageForm()
}

function displayCategoryNames(product) {
  return product?.categoryNames?.length
    ? product.categoryNames
    : [product?.categoryName || 'Không có danh mục']
}

function isNewArrival(product) {
  return isNewProduct(product)
}

function formatDateLabel(value) {
  if (!value) return 'Chưa có ngày'
  return new Date(value).toLocaleDateString('vi-VN')
}

function toDateTimeLocalValue(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (number) => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function statusLabel(product) {
  const map = {
    0: 'Nháp',
    1: 'Đang bán',
    2: 'Hết hàng',
    3: 'Sắp về',
    4: 'Ẩn'
  }
  return map[normalizeProductStatus(product?.statusCode ?? product?.status)] || 'Không rõ'
}

function statusBadgeClass(product) {
  const map = {
    0: 'bg-warning text-dark',
    1: 'bg-success',
    2: 'bg-danger',
    3: 'bg-info text-dark',
    4: 'bg-secondary'
  }
  return map[normalizeProductStatus(product?.statusCode ?? product?.status)] || 'bg-secondary'
}

function buildVariantLabel(variant) {
  const parts = [variant.size, variant.color].filter(Boolean)
  return parts.length ? parts.join(' / ') : `Biến thể #${variant.id}`
}

async function save() {
  try {
    if (!form.value.categoryId && categories.value.length) {
      form.value.categoryId = generalCategories.value[0]?.id || categories.value[0].id
    }
    const payload = {
      ...form.value,
      categoryId: form.value.categoryId || null,
      categoryIds: form.value.tagIds || [],
      createdAt: form.value.createdAt || null
    }
    if (editing.value) await axios.put(`/api/admin/products/${editing.value.id}`, payload)
    else await axios.post('/api/admin/products', payload)
    toast.success(editing.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!')
    closeForm()
    await refreshProducts()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Lỗi lưu sản phẩm!')
  }
}

async function remove(id) {
  const ok = await confirmStore.open({ title: 'Xóa sản phẩm', message: 'Thao tác không thể hoàn tác.', confirmText: 'Xóa' })
  if (!ok) return
  try {
    await axios.delete(`/api/admin/products/${id}`)
    toast.success('Xóa sản phẩm thành công!')
    await refreshProducts()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa sản phẩm!')
  }
}

async function refreshProducts(selectedId = selectedProduct.value?.id) {
  await fetchProducts()
  if (selectedId) {
    const updated = products.value.find((product) => product.id === selectedId)
    selectedProduct.value = updated || null
    selectedPromotionIds.value = [...(updated?.promotionIds || [])]
  }
}

async function saveProductPromotions() {
  if (!selectedProduct.value) return
  try {
    const { data } = await axios.put(`/api/admin/products/${selectedProduct.value.id}/promotions`, {
      promotionIds: selectedPromotionIds.value
    })
    const normalized = normalizeProduct(data)
    const index = products.value.findIndex((product) => product.id === normalized.id)
    if (index >= 0) products.value[index] = normalized
    selectedProduct.value = normalized
    selectedPromotionIds.value = [...(normalized.promotionIds || [])]
    toast.success('Đã cập nhật khuyến mãi cho sản phẩm!')
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể cập nhật khuyến mãi!')
  }
}

function openVariantForm(variant = null) {
  editingVariant.value = variant
  variantForm.value = variant
    ? { size: variant.size || '', color: variant.color || '', stock: variant.stock ?? 0, price: variant.price ?? selectedProduct.value?.price ?? 0, status: variant.status ?? true }
    : { ...emptyVariantForm(), price: selectedProduct.value?.price ?? 0 }
  showVariantForm.value = true
}

function closeVariantForm() {
  showVariantForm.value = false
  editingVariant.value = null
  variantForm.value = emptyVariantForm()
}

async function saveVariant() {
  if (!selectedProduct.value) return
  try {
    const productId = selectedProduct.value.id
    if (editingVariant.value) {
      await axios.put(`/api/admin/products/${productId}/variants/${editingVariant.value.id}`, variantForm.value)
    } else {
      await axios.post(`/api/admin/products/${productId}/variants`, variantForm.value)
    }
    toast.success('Đã lưu biến thể!')
    closeVariantForm()
    await refreshProducts(productId)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể lưu biến thể!')
  }
}

async function removeVariant(variant) {
  if (!selectedProduct.value) return
  const ok = await confirmStore.open({ title: 'Xóa biến thể', message: 'Biến thể đã phát sinh đơn hàng/giỏ hàng sẽ không thể xóa.', confirmText: 'Xóa' })
  if (!ok) return
  try {
    const productId = selectedProduct.value.id
    await axios.delete(`/api/admin/products/${productId}/variants/${variant.id}`)
    toast.success('Đã xóa biến thể!')
    await refreshProducts(productId)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa biến thể!')
  }
}

function openImageForm(image = null) {
  editingImage.value = image
  imageForm.value = image ? { imageUrl: image.imageUrl || '' } : emptyImageForm()
  showImageForm.value = true
}

function closeImageForm() {
  showImageForm.value = false
  editingImage.value = null
  imageForm.value = emptyImageForm()
}

function handleGalleryUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    imageForm.value.imageUrl = reader.result || ''
  }
  reader.readAsDataURL(file)
}

async function saveImage() {
  if (!selectedProduct.value) return
  try {
    const productId = selectedProduct.value.id
    if (editingImage.value) {
      await axios.put(`/api/admin/products/${productId}/images/${editingImage.value.id}`, imageForm.value)
    } else {
      await axios.post(`/api/admin/products/${productId}/images`, imageForm.value)
    }
    toast.success('Đã lưu hình ảnh phụ!')
    closeImageForm()
    await refreshProducts(productId)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể lưu hình ảnh!')
  }
}

async function removeImage(image) {
  if (!selectedProduct.value) return
  const ok = await confirmStore.open({ title: 'Xóa ảnh phụ', message: 'Xóa hình ảnh khỏi product_images.', confirmText: 'Xóa' })
  if (!ok) return
  try {
    const productId = selectedProduct.value.id
    await axios.delete(`/api/admin/products/${productId}/images/${image.id}`)
    toast.success('Đã xóa hình ảnh!')
    await refreshProducts(productId)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Không thể xóa hình ảnh!')
  }
}

defineExpose({ refresh: refreshProducts })
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
  max-height: calc(100vh - 2rem);
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  width: 100%;
  border: 2px solid var(--y2k-dark);
  box-shadow: 8px 8px 0 var(--y2k-dark);
}

.btn-link {
  text-align: left;
}

.btn-link:hover {
  color: var(--y2k-accent) !important;
}
</style>
