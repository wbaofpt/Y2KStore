<template>
  <div class="y2k-page-container catalog-shell shop-page-v2">
    <aside class="filters-panel">
      <div class="surface-card surface-card--compact filters-panel__card">
        <div class="filters-panel__head">
          <div>
            <span class="section-heading__eyebrow">Bộ lọc</span>
            <h4>Lọc nhanh theo nhu cầu</h4>
          </div>
          <button class="btn btn-link btn-sm text-decoration-none" @click="resetFilters">Xóa</button>
        </div>

        <form class="filters-stack" @submit.prevent="applyFilters">
          <label class="filter-field">
            <span>Từ khóa</span>
            <div class="filter-input-shell">
              <i class="bi bi-search"></i>
              <input
                v-model="draftSearch"
                type="text"
                placeholder="Tìm áo, quần, phụ kiện..."
              >
            </div>
          </label>

          <label class="filter-field">
            <span>Danh mục</span>
            <div class="filter-select" :class="{ 'is-open': categoryDropdownOpen }">
              <button type="button" class="filter-select__trigger" @click.stop="categoryDropdownOpen = !categoryDropdownOpen">
                <span>{{ selectedCategoryLabel || 'Tất cả danh mục' }}</span>
                <i class="bi bi-chevron-down"></i>
              </button>
              <div v-if="categoryDropdownOpen" class="filter-select__menu" @click.stop>
                <button type="button" class="filter-select__option filter-select__option--all" :class="{ active: !draftCategoryId }" @click="selectCategory(null)">
                  <i class="bi bi-grid-3x3-gap"></i>
                  <span>Tất cả danh mục</span>
                  <i v-if="!draftCategoryId" class="bi bi-check2"></i>
                </button>
                <div class="filter-select__group-label">Danh mục chung</div>
                <button v-for="category in generalCategories" :key="category.id" type="button" class="filter-select__option" :class="{ active: Number(draftCategoryId) === Number(category.id) }" @click="selectCategory(category.id)">
                  <i class="bi bi-tag"></i>
                  <span>{{ category.name }}</span>
                  <i v-if="Number(draftCategoryId) === Number(category.id)" class="bi bi-check2"></i>
                </button>
                <template v-if="tagCategories.length">
                  <div class="filter-select__group-label">Tag sản phẩm</div>
                  <button v-for="category in tagCategories" :key="category.id" type="button" class="filter-select__option" :class="{ active: Number(draftCategoryId) === Number(category.id) }" @click="selectCategory(category.id)">
                    <i class="bi bi-hash"></i>
                    <span>#{{ category.name }}</span>
                    <i v-if="Number(draftCategoryId) === Number(category.id)" class="bi bi-check2"></i>
                  </button>
                </template>
              </div>
            </div>
          </label>

          <div class="filter-field">
            <span>Khoảng giá</span>
            <div class="filter-range-grid">
              <input
                v-model="draftMinPrice"
                type="number"
                min="0"
                step="1000"
                class="y2k-input"
                placeholder="Từ"
              >
              <input
                v-model="draftMaxPrice"
                type="number"
                min="0"
                step="1000"
                class="y2k-input"
                placeholder="Đến"
              >
            </div>
          </div>

          <label class="filter-field">
            <span>Sắp xếp</span>
            <div class="filter-select" :class="{ 'is-open': sortDropdownOpen }">
              <button type="button" class="filter-select__trigger" @click.stop="sortDropdownOpen = !sortDropdownOpen">
                <span>{{ selectedSortLabel }}</span>
                <i class="bi bi-chevron-down"></i>
              </button>
              <div v-if="sortDropdownOpen" class="filter-select__menu filter-select__menu--sort" @click.stop>
                <button v-for="option in sortOptions" :key="option.value" type="button" class="filter-select__option" :class="{ active: sortBy === option.value }" @click="selectSort(option.value)">
                  <i :class="option.icon"></i>
                  <span>{{ option.label }}</span>
                  <i v-if="sortBy === option.value" class="bi bi-check2"></i>
                </button>
              </div>
            </div>
          </label>

          <button type="submit" class="btn btn-y2k-primary w-100">
            Áp dụng bộ lọc
          </button>

          <button
            type="button"
            class="btn btn-y2k-outline w-100"
            :class="{ active: showSaleOnly }"
            @click="toggleSaleOnly"
          >
            <i class="bi bi-lightning-charge me-1"></i>
            {{ showSaleOnly ? 'Đang xem sale' : 'Chỉ xem sale' }}
          </button>
        </form>
      </div>
    </aside>

    <section>
      <div class="shop-hero-banner">
        <div>
          <span class="section-heading__eyebrow">Y2K Store</span>
          <h1>Tìm item đúng vibe, mua nhanh hơn</h1>
          <p>
            Lọc theo danh mục, giá, sale và từ khóa với giao diện gọn hơn, dễ quét hơn trên mọi màn hình.
          </p>
        </div>
      </div>

      <div class="shop-category-pills">
        <span>Khám phá nhanh</span>
        <router-link to="/shop" :class="{ active: !draftCategoryId }">Tất cả</router-link>
        <router-link v-for="category in generalCategories.slice(0, 7)" :key="category.id" :to="`/shop?category=${category.id}`" :class="{ active: Number(draftCategoryId) === Number(category.id) }">{{ category.name }}</router-link>
        <router-link to="/shop?sale=true" class="shop-category-pills__sale"><i class="bi bi-lightning-charge"></i> Sale</router-link>
      </div>

      <div class="section-heading shop-section-heading">
        <div>
          <span class="section-heading__eyebrow">Shop</span>
          <h1>Cửa hàng</h1>
          <p>Sản phẩm lấy trực tiếp từ backend, có thể lọc ngay theo nhu cầu thực tế của bạn.</p>
        </div>
      </div>

      <div class="catalog-toolbar">
        <div class="catalog-toolbar__stats">
          Tìm thấy {{ sortedProducts.length }} sản phẩm
          <template v-if="searchQuery"> cho “{{ searchQuery }}”</template>
        </div>

        <div class="catalog-toolbar__controls">
          <div class="filter-select filter-select--toolbar" :class="{ 'is-open': toolbarSortDropdownOpen }">
            <button type="button" class="filter-select__trigger" @click.stop="toolbarSortDropdownOpen = !toolbarSortDropdownOpen">
              <span>{{ selectedSortLabel }}</span>
              <i class="bi bi-chevron-down"></i>
            </button>
            <div v-if="toolbarSortDropdownOpen" class="filter-select__menu filter-select__menu--sort" @click.stop>
              <button v-for="option in sortOptions" :key="option.value" type="button" class="filter-select__option" :class="{ active: sortBy === option.value }" @click="selectSort(option.value)">
                <i :class="option.icon"></i>
                <span>{{ option.label }}</span>
                <i v-if="sortBy === option.value" class="bi bi-check2"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="filter-chip-row" v-if="activeFilterLabels.length">
        <span v-for="label in activeFilterLabels" :key="label" class="filter-chip">
          {{ label }}
        </span>
      </div>

      <div v-if="loading" class="surface-card">Đang tải sản phẩm...</div>
      <div v-else-if="sortedProducts.length === 0" class="empty-state">
        <h4>Không có sản phẩm phù hợp</h4>
        <p class="muted-copy">Bạn hãy đổi bộ lọc hoặc quay lại catalog tổng.</p>
        <button class="btn btn-y2k-outline" @click="resetFilters">Xóa bộ lọc</button>
      </div>
      <div v-else class="product-grid shop-product-grid">
        <ProductCard v-for="product in sortedProducts" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import ProductCard from '../components/ProductCard.vue'
import { isNewProduct, normalizeCategory, normalizeProduct } from '../composables/useCatalog'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const generalCategories = computed(() => categories.value.filter((category) => category.categoryType !== 'TAG'))
const tagCategories = computed(() => categories.value.filter((category) => category.categoryType === 'TAG'))
const products = ref([])
const loading = ref(false)
const hasLoadedProducts = ref(false)
let productsRequestId = 0

const draftSearch = ref('')
const draftCategoryId = ref(null)
const draftMinPrice = ref('')
const draftMaxPrice = ref('')
const showSaleOnly = ref(false)
const sortBy = ref('default')
const appliedSearch = ref('')
const appliedCategoryId = ref(null)
const appliedMinPrice = ref('')
const appliedMaxPrice = ref('')
const appliedShowSaleOnly = ref(false)
const appliedSortBy = ref('default')
const categoryDropdownOpen = ref(false)
const sortDropdownOpen = ref(false)
const toolbarSortDropdownOpen = ref(false)
const sortOptions = [
  { value: 'default', label: 'Mặc định', icon: 'bi bi-stars' },
  { value: 'new', label: 'Hàng mới', icon: 'bi bi-stars' },
  { value: 'best-selling', label: 'Bán chạy nhất', icon: 'bi bi-fire' },
  { value: 'rating', label: 'Đánh giá cao', icon: 'bi bi-star' },
  { value: 'price-asc', label: 'Giá tăng dần', icon: 'bi bi-sort-numeric-down' },
  { value: 'price-desc', label: 'Giá giảm dần', icon: 'bi bi-sort-numeric-up' },
  { value: 'name-asc', label: 'Tên A-Z', icon: 'bi bi-sort-alpha-down' }
]
const selectedSortLabel = computed(() => sortOptions.find((option) => option.value === sortBy.value)?.label || 'Mặc định')

onMounted(async () => {
  window.addEventListener('click', closeCategoryDropdown)
  await fetchCategories()
  await syncFromRoute()
  await fetchProducts()
})

onUnmounted(() => {
  window.removeEventListener('click', closeCategoryDropdown)
})

function closeCategoryDropdown() {
  categoryDropdownOpen.value = false
  sortDropdownOpen.value = false
  toolbarSortDropdownOpen.value = false
}

watch(() => route.query, async () => {
  await syncFromRoute()
  await fetchProducts()
}, { deep: true })

const filteredProducts = computed(() => {
  let list = [...products.value]
  if (appliedShowSaleOnly.value) {
    list = list.filter((item) => Number(item.promotionDiscountPercent || 0) > 0)
  }
  if (appliedSortBy.value === 'new') {
    list = list.filter((item) => isNewProduct(item))
  }
  return list
})

const sortedProducts = computed(() => {
  const list = [...filteredProducts.value]
    switch (appliedSortBy.value) {
    case 'new':
      return list.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0) || Number(b.id || 0) - Number(a.id || 0))
    case 'best-selling':
      return list.sort((a, b) => Number(b.soldCount || 0) - Number(a.soldCount || 0) || Number(b.ratingAverage || 0) - Number(a.ratingAverage || 0))
    case 'rating':
      return list.sort((a, b) => Number(b.ratingAverage || 0) - Number(a.ratingAverage || 0) || Number(b.reviewCount || 0) - Number(a.reviewCount || 0) || Number(b.soldCount || 0) - Number(a.soldCount || 0))
    case 'price-asc':
      return list.sort((a, b) => Number(a.minVariantPrice || a.price || 0) - Number(b.minVariantPrice || b.price || 0))
    case 'price-desc':
      return list.sort((a, b) => Number(b.minVariantPrice || b.price || 0) - Number(a.minVariantPrice || a.price || 0))
    case 'name-asc':
      return list.sort((a, b) => a.name.localeCompare(b.name))
    default:
      return list
  }
})

const activeFilterLabels = computed(() => {
  const labels = []
  if (appliedSearch.value) labels.push(`Từ khóa: ${appliedSearch.value}`)
  if (appliedCategoryLabel.value) labels.push(`Danh mục: ${appliedCategoryLabel.value}`)
  if (appliedMinPrice.value) labels.push(`Từ ${formatCurrency(appliedMinPrice.value)}`)
  if (appliedMaxPrice.value) labels.push(`Đến ${formatCurrency(appliedMaxPrice.value)}`)
  if (appliedShowSaleOnly.value) labels.push('Chỉ sale')
  if (appliedSortBy.value === 'new') labels.push('Hàng mới trong 14 ngày')
  return labels
})

const searchQuery = computed(() => appliedSearch.value)
const selectedCategoryLabel = computed(() => {
  if (!draftCategoryId.value) return ''
  return categories.value.find((item) => item.id === Number(draftCategoryId.value))?.name || ''
})
const appliedCategoryLabel = computed(() => {
  if (!appliedCategoryId.value) return ''
  return categories.value.find((item) => item.id === Number(appliedCategoryId.value))?.name || ''
})

function formatCurrency(value) {
  if (value === '' || value === null || value === undefined) return ''
  return `${new Intl.NumberFormat('vi-VN').format(Number(value))} đ`
}

async function syncFromRoute() {
  draftSearch.value = route.query.search || ''
  if (route.query.category) {
    draftCategoryId.value = Number(route.query.category)
  } else if (route.query.categoryName) {
    const requestedName = normalizeCategoryName(route.query.categoryName)
    const matchedCategory = generalCategories.value.find((category) => {
      const categoryName = normalizeCategoryName(category.name)
      return categoryName === requestedName || categoryName.includes(requestedName) || requestedName.includes(categoryName)
    })
    draftCategoryId.value = matchedCategory?.id || null
  } else {
    draftCategoryId.value = null
  }
  draftMinPrice.value = route.query.minPrice || ''
  draftMaxPrice.value = route.query.maxPrice || ''
  showSaleOnly.value = route.query.sale === 'true'
  sortBy.value = route.query.sort || 'default'
  appliedSearch.value = draftSearch.value.trim()
  appliedCategoryId.value = draftCategoryId.value
  appliedMinPrice.value = draftMinPrice.value
  appliedMaxPrice.value = draftMaxPrice.value
  appliedShowSaleOnly.value = showSaleOnly.value
  appliedSortBy.value = sortBy.value
}

function normalizeCategoryName(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

async function fetchCategories() {
  try {
    const { data } = await axios.get('/api/categories')
    categories.value = (data || []).map(normalizeCategory)
  } catch {
    categories.value = []
  }
}

async function fetchProducts() {
  const requestId = ++productsRequestId
  const isInitialLoad = !hasLoadedProducts.value
  loading.value = isInitialLoad
  try {
    const params = {}
    if (appliedCategoryId.value) params.categoryId = appliedCategoryId.value
    if (searchQuery.value) params.search = searchQuery.value
    if (appliedMinPrice.value !== '') params.minPrice = appliedMinPrice.value
    if (appliedMaxPrice.value !== '') params.maxPrice = appliedMaxPrice.value
    const { data } = await axios.get('/api/products', { params })
    if (requestId !== productsRequestId) return
    products.value = (data || []).map(normalizeProduct)
  } catch {
    if (requestId === productsRequestId && !hasLoadedProducts.value) products.value = []
  } finally {
    if (requestId === productsRequestId) {
      hasLoadedProducts.value = true
      loading.value = false
    }
  }
}

function applyFilters() {
  categoryDropdownOpen.value = false
  sortDropdownOpen.value = false
  toolbarSortDropdownOpen.value = false
  router.push({
    path: '/shop',
    query: {
      ...(draftSearch.value ? { search: draftSearch.value } : {}),
      ...(draftCategoryId.value ? { category: draftCategoryId.value } : {}),
      ...(draftMinPrice.value !== '' ? { minPrice: draftMinPrice.value } : {}),
      ...(draftMaxPrice.value !== '' ? { maxPrice: draftMaxPrice.value } : {}),
      ...(showSaleOnly.value ? { sale: 'true' } : {}),
      ...(sortBy.value !== 'default' ? { sort: sortBy.value } : {})
    }
  })
}

function selectCategory(categoryId) {
  draftCategoryId.value = categoryId
  categoryDropdownOpen.value = false
}

function selectSort(value) {
  sortBy.value = value
  sortDropdownOpen.value = false
  toolbarSortDropdownOpen.value = false
}

function toggleSaleOnly() {
  showSaleOnly.value = !showSaleOnly.value
}

function resetFilters() {
  router.push('/shop')
}
</script>
