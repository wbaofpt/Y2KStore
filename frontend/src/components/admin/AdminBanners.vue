<template>
  <!-- Khung chính của trang quản lý banner -->
  <div class="admin-section">

    <!-- Tiêu đề trang và nút thêm banner -->
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Banner control</span>
        <h4 class="admin-page-title mb-0">Quản lý banner trang chủ</h4>
        <p>
          Quản lý nội dung, ảnh, thứ tự, lịch hiển thị và sản phẩm liên kết theo bảng banner.
        </p>
      </div>

      <!-- Mở form để thêm banner mới -->
      <button class="btn btn-y2k-primary btn-sm" @click="openForm()">
        <i class="bi bi-plus-circle me-1"></i> Thêm banner
      </button>
    </div>


    <!-- ================= THỐNG KÊ BANNER ================= -->
    <div class="admin-mini-stats">

      <!-- Tổng số banner -->
      <div class="admin-stat-card">
        <div class="admin-stat-label">Tổng banner</div>

        <!-- banners.length = tổng số banner -->
        <div class="admin-stat-value">{{ banners.length }}</div>

        <div class="admin-stat-meta">banner</div>
      </div>

      <!-- Số banner đang hiển thị -->
      <div class="admin-stat-card">
        <div class="admin-stat-label">Đang hiện</div>
        <!-- activeCount = số banner có trạng thái đang hiện -->
        <div class="admin-stat-value">{{ activeCount }}</div>
        <div class="admin-stat-meta">status = 1</div>
      </div>
      <!-- Số banner đang ẩn -->
      <div class="admin-stat-card">
        <div class="admin-stat-label">Đã ẩn</div>
        <!-- hiddenCount = số banner có trạng thái ẩn -->
        <div class="admin-stat-value">{{ hiddenCount }}</div>

        <div class="admin-stat-meta">status = 0</div>
      </div>

    </div>


    <!-- ================= TÌM KIẾM VÀ LỌC ================= -->
    <div class="admin-toolbar">

      <!-- Ô tìm kiếm banner -->
      <!-- v-model liên kết dữ liệu nhập vào với biến search -->
      <input
          v-model="search"
          type="text"
          class="form-control form-control-sm"
          placeholder="Tìm sản phẩm hoặc mã banner..."
          style="max-width: 280px;"
      >

      <!-- Bộ lọc trạng thái banner -->
      <!-- v-model liên kết lựa chọn với biến statusFilter -->
      <select
          v-model="statusFilter"
          class="form-select form-select-sm"
          style="max-width: 180px;"
      >

        <!-- Hiển thị tất cả banner -->
        <option value="">Tất cả trạng thái</option>

        <!-- Chỉ hiển thị banner đang hoạt động -->
        <option value="active">Đang hiển thị</option>

        <!-- Chỉ hiển thị banner đang ẩn -->
        <option value="hidden">Đã ẩn</option>

      </select>
    </div>


    <!-- ================= FORM THÊM / SỬA BANNER ================= -->

    <!-- Chỉ hiển thị form khi showForm = true -->
    <div v-if="showForm" class="admin-form-card y2k-animate-in">

      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <div>
          <!-- Nếu có editing thì là sửa, ngược lại là thêm -->
          <h5 class="fw-bold mb-1">
            {{ editing ? 'Sửa banner' : 'Thêm banner mới' }}
          </h5>

          <p class="text-secondary small mb-0">
            Ảnh banner được dùng ở trang chủ, nên giữ tỉ lệ ngang để hiển thị đẹp.
          </p>

        </div>

        <!-- Đóng form -->
        <button
            type="button"
            class="btn btn-y2k-outline btn-sm"
            @click="closeForm"
        >
          <i class="bi bi-x-lg"></i>
        </button>

      </div>


      <!-- Form thêm / sửa banner -->
      <!-- @submit.prevent gọi hàm save và không reload trang -->
      <form @submit.prevent="save" class="row g-3">

        <!-- ================= CHỌN SẢN PHẨM ================= -->
        <div class="col-lg-5">
          <label class="form-label small fw-bold">
            Sản phẩm liên kết
          </label>

          <!-- Chọn sản phẩm liên kết với banner -->
          <select
              v-model="form.productId"
              class="form-select form-select-sm"
              required
          >

            <option disabled value="">
              Chọn sản phẩm
            </option>

            <!-- Duyệt danh sách sản phẩm để tạo option -->
            <option
                v-for="product in products"
                :key="product.id"
                :value="product.id"
            >
              {{ product.name }}
            </option>

          </select>
        </div>


        <!-- ================= TIÊU ĐỀ ================= -->
        <div class="col-lg-4">
          <label class="form-label small fw-bold">
            Tiêu đề hiển thị
          </label>

          <!-- Nhập tiêu đề banner -->
          <input
              v-model="form.title"
              class="form-control form-control-sm"
              placeholder="Summer drop"
          >
        </div>


        <!-- ================= NÚT HÀNH ĐỘNG ================= -->
        <div class="col-lg-4">
          <label class="form-label small fw-bold">
            Nút hành động
          </label>

          <!-- Nhập nội dung nút trên banner -->
          <input
              v-model="form.buttonText"
              class="form-control form-control-sm"
              placeholder="Mua ngay"
          >
        </div>


        <!-- ================= THỨ TỰ ================= -->
        <div class="col-lg-2">
          <label class="form-label small fw-bold">
            Thứ tự
          </label>

          <!-- v-model.number giúp dữ liệu nhận vào dạng số -->
          <input
              v-model.number="form.sortOrder"
              type="number"
              min="0"
              class="form-control form-control-sm"
          >
        </div>


        <!-- ================= MÔ TẢ ================= -->
        <div class="col-lg-10">
          <label class="form-label small fw-bold">
            Mô tả ngắn
          </label>

          <!-- Nhập mô tả ngắn cho banner -->
          <input
              v-model="form.subtitle"
              class="form-control form-control-sm"
              placeholder="Nội dung nổi bật của banner"
          >
        </div>


        <!-- ================= NGÀY BANNER ================= -->
        <div class="col-lg-4">
          <label class="form-label small fw-bold">
            Ngày banner
          </label>

          <!-- Chọn ngày hiển thị banner -->
          <input
              v-model="form.dateBanner"
              type="date"
              class="form-control form-control-sm"
          >
        </div>


        <!-- ================= TRẠNG THÁI ================= -->
        <div class="col-lg-3 d-flex align-items-end">

          <!-- Checkbox bật / tắt banner -->
          <label class="admin-switch">

            <input
                v-model="form.status"
                type="checkbox"
            >
            <span></span>
            <strong>Đang hiển thị</strong>
          </label>
        </div>
        <!-- ================= URL ẢNH ================= -->
        <div class="col-lg-8">
          <label class="form-label small fw-bold">
            URL ảnh
          </label>

          <!-- Nhập đường dẫn ảnh banner -->
          <input
              v-model="form.image"
              class="form-control form-control-sm"
              placeholder="https://... hoặc tải từ thiết bị"
          >
        </div>


        <!-- ================= TẢI ẢNH ================= -->
        <div class="col-lg-4">
          <label class="form-label small fw-bold">
            Tải ảnh từ thiết bị
          </label>

          <!-- Chọn ảnh từ máy tính -->
          <!-- Khi chọn ảnh sẽ gọi handleImageUpload -->
          <input
              type="file"
              accept="image/*"
              class="form-control form-control-sm"
              @change="handleImageUpload"
          >
        </div>


        <!-- ================= XEM TRƯỚC ẢNH ================= -->

        <!-- Chỉ hiển thị nếu form.image có dữ liệu -->
        <div v-if="form.image" class="col-12">

          <div class="admin-banner-form-preview">

            <!-- Hiển thị ảnh xem trước -->
            <img
                :src="form.image"
                alt="Banner preview"
            >

            <div>
              <strong>Xem trước banner</strong>
            </div>

          </div>
        </div>
        <!-- ================= NÚT LƯU / HỦY ================= -->
        <div class="col-12 d-flex gap-2">

          <!-- Submit form để thêm hoặc cập nhật banner -->
          <button type="submit" class="btn btn-y2k-primary btn-sm">banner Lưu</button>

          <!-- Hủy và đóng form -->
          <button
              type="button"
              class="btn btn-secondary btn-sm"
              @click="closeForm">Hủy</button>

        </div>

      </form>
    </div>


    <!-- ================= DANH SÁCH BANNER ================= -->
    <div class="admin-list-card">

      <div class="admin-list-header">
        <div>

          <strong>Danh sách banner</strong>

          <!-- Hiển thị số kết quả sau khi tìm kiếm / lọc -->
          <span>{{ filteredBanners.length }} kết quả</span>

        </div>

        <span class="admin-db-chip">banner</span>
      </div>


      <div class="admin-banner-list">

        <!-- Duyệt qua danh sách banner đã được tìm kiếm / lọc -->
        <article
            v-for="banner in filteredBanners"
            :key="banner.id"
            class="admin-banner-row"
        >

          <!-- ================= ẢNH BANNER ================= -->
          <div class="admin-banner-thumb">

            <!-- Nếu có ảnh thì hiển thị ảnh -->
            <img
                v-if="banner.imageUrl"
                :src="banner.imageUrl"
                alt=""
            >

            <!-- Nếu không có ảnh thì hiển thị icon -->
            <div v-else class="admin-banner-empty">
              <i class="bi bi-image"></i>
            </div>

          </div>


          <!-- ================= THÔNG TIN BANNER ================= -->
          <div class="admin-banner-content">

            <div class="d-flex align-items-center flex-wrap gap-2 mb-1">

              <!-- Hiển thị tên sản phẩm liên kết -->
              <strong>
                {{ banner.productName || `Sản phẩm #${banner.productId || '-'}` }}
              </strong>


              <!-- Hiển thị trạng thái banner -->
              <span
                  class="badge"
                  :class="banner.status === false ? 'bg-secondary' : 'bg-success'"
              >
                {{ banner.status === false ? 'Ẩn' : 'Hiện' }}
              </span>

            </div>
            <!-- Thông tin phụ của banner -->
            <div class="admin-banner-sub">
              <!-- ID banner -->
              <span>
                <i class="bi bi-hash"></i>
                Banner #{{ banner.id }}
              </span>
              <!-- ID sản phẩm -->
              <span>
                <i class="bi bi-bag"></i>
                SP #{{ banner.productId || '-' }}
              </span>
              <!-- Ngày banner -->
              <span>
<i class="bi bi-calendar3"></i>
                {{ formatDateLabel(banner.dateBanner) }}
              </span>
            </div>
            <!-- Hiển thị URL ảnh -->
            <div class="admin-banner-url">
              {{ banner.imageUrl || 'Chưa có ảnh banner' }}
            </div>
            <!-- Hiển thị tiêu đề và mô tả nếu có -->
            <div
                v-if="banner.title || banner.subtitle"
                class="admin-banner-url"
            >
              {{ banner.title }}
              <span v-if="banner.subtitle">
                · {{ banner.subtitle }}
              </span>

            </div>

          </div>


          <!-- ================= CÁC NÚT THAO TÁC ================= -->
          <div class="admin-banner-actions">

            <!-- Nút sửa banner -->
            <button
                class="btn btn-sm btn-outline-dark"
                @click="openForm(banner)"
            >
              <i class="bi bi-pencil"></i>
            </button>

            <!-- Nút xóa banner -->
            <button
                class="btn btn-sm btn-outline-danger"
                @click="remove(banner.id)"
            >
              <i class="bi bi-trash"></i>
            </button>

          </div>

        </article>


        <!-- Nếu không có banner phù hợp với điều kiện tìm kiếm / lọc -->
        <div
            v-if="!filteredBanners.length"
            class="admin-empty-state"
        >
          <i class="bi bi-images"></i>

          <strong>Không có banner phù hợp</strong>

          <span>
            Thử đổi bộ lọc hoặc thêm banner mới.
          </span>
        </div>

      </div>
    </div>

  </div>
</template>


<script setup>

import {
  computed,
  ref,
  onMounted
} from 'vue'


import axios from 'axios'


import { useToastStore } from '../../stores/toast'

import { useConfirmStore } from '../../stores/confirm'

import {
  normalizeBanner,
  normalizeProduct
} from '../../composables/useCatalog'

// ================= KHỞI TẠO =================

// Store thông báo
const toast = useToastStore()

// Store xác nhận xóa
const confirmStore = useConfirmStore()

// Danh sách banner
const banners = ref([])

// Danh sách sản phẩm
const products = ref([])

// Từ khóa người dùng nhập vào ô tìm kiếm
const search = ref('')

// Trạng thái đang lọc:
// ''       = tất cả
// 'active' = đang hiện
// 'hidden' = đang ẩn
const statusFilter = ref('')

// Điều khiển việc hiển thị form
// false = ẩn form
// true = hiện form
const showForm = ref(false)

// Lưu banner đang được chỉnh sửa
// null = đang thêm mới
const editing = ref(null)

// Dữ liệu trong form thêm / sửa banner
const form = ref(emptyForm())


// ================= THỐNG KÊ =================
// Đếm số banner đang hiển thị
const activeCount = computed(() =>
    banners.value.filter(
        (banner) => banner.status !== false // neu status không false thì banner hiển thị và ngược lại
    ).length
)

// Đếm số banner đang ẩn
const hiddenCount = computed(() =>
    banners.value.filter(
        (banner) => banner.status === false
    ).length
)


// ================= TÌM KIẾM + LỌC =================

// filteredBanners là danh sách banner sau khi
// tìm kiếm và lọc theo trạng thái
const filteredBanners = computed(() => {

  // Lấy từ khóa tìm kiếm
  // trim() bỏ khoảng trắng
  // toLowerCase() chuyển thành chữ thường
  const q = search.value.trim().toLowerCase()

  // Lọc danh sách banner
  return banners.value.filter((banner) => {

    // Kiểm tra banner có khớp với từ khóa tìm kiếm không
    // Có thể tìm theo:
    // - ID banner
    // - ID sản phẩm
    // - Tên sản phẩm
    const matchSearch =
        !q ||
        String(banner.id).includes(q) ||
        String(banner.productId || '').includes(q) ||
        banner.productName?.toLowerCase().includes(q)

    // Kiểm tra trạng thái banner
    const matchStatus =
        !statusFilter.value ||
        (
            statusFilter.value === 'active' &&
            banner.status !== false
        ) ||
        (
            statusFilter.value === 'hidden' &&
            banner.status === false
        )
    // Banner phải thỏa cả điều kiện tìm kiếm
    // và điều kiện trạng thái
    return matchSearch && matchStatus
  })
})
// ================= FORM MẶC ĐỊNH =================
// Tạo dữ liệu mặc định khi thêm banner mới
function emptyForm() {
  return {
    productId: '',       // ID sản phẩm liên kết
    image: '',           // URL / dữ liệu ảnh
    title: '',           // Tiêu đề banner
    subtitle: '',        // Mô tả ngắn
    buttonText: 'Mua ngay', // Nội dung nút
    sortOrder: 0,        // Thứ tự hiển thị
    dateBanner: '',      // Ngày banner
    status: true         // Mặc định đang hiển thị
  }
}
// ================= KHI COMPONENT ĐƯỢC TẢI =================
// onMounted chạy khi component được hiển thị
onMounted(() => {
  // Lấy danh sách banner từ Backend
  fetchBanners()
  // Lấy danh sách sản phẩm từ Backend
  fetchProducts()

})


// ================= LẤY DANH SÁCH BANNER =================

async function fetchBanners() {

  // Gọi API Backend để lấy danh sách banner
  const { data } = await axios.get('/api/admin/banners')

  // Chuẩn hóa dữ liệu rồi lưu vào banners
  banners.value = data.map(normalizeBanner)
}


// ================= LẤY DANH SÁCH SẢN PHẨM =================

async function fetchProducts() {

  // Gọi API Backend lấy danh sách sản phẩm
  const { data } = await axios.get('/api/admin/products')

  // Chuẩn hóa dữ liệu sản phẩm
  products.value = data.map(normalizeProduct)
  // Nếu form chưa có sản phẩm
  // và danh sách sản phẩm có dữ liệu
  // thì mặc định chọn sản phẩm đầu tiên
  if (
      !form.value.productId &&
      products.value.length
  ) {
    form.value.productId = products.value[0].id
  }
}


// ================= MỞ FORM THÊM / SỬA =================

function openForm(banner = null) {

  // Lưu banner đang sửa
  // null nếu đang thêm mới
  editing.value = banner

  // Nếu có banner → đưa dữ liệu cũ vào form để sửa
  // Nếu không có banner → tạo form mới
  form.value = banner
      ? {
        productId: banner.productId || '',
        image: banner.imageUrl || '',
        title: banner.title || '',
        subtitle: banner.subtitle || '',
        buttonText: banner.buttonText || 'Mua ngay',
        sortOrder: banner.sortOrder || 0,
        dateBanner: banner.dateBanner || '',
        status: banner.status ?? true
      }

      // Thêm banner mới
      : {
        ...emptyForm(),
        productId: products.value[0]?.id || ''
      }

  // Hiển thị form
  showForm.value = true
}


// ================= ĐÓNG FORM =================

function closeForm() {
  // Ẩn form
  showForm.value = false
  // Xóa thông tin banner đang sửa
  editing.value = null
  // Reset form về trạng thái ban đầu
  form.value = emptyForm()
}
// ================= TẢI ẢNH TỪ THIẾT BỊ =================
function handleImageUpload(event) {
  // Lấy file đầu tiên người dùng chọn
  const file = event.target.files?.[0]
  // Không có file thì dừng
  if (!file) return
  // Tạo FileReader để đọc file ảnh
  const reader = new FileReader()
  // Sau khi đọc ảnh thành công
  reader.onload = () => {
    // Lưu dữ liệu ảnh vào form
    form.value.image = reader.result || ''
  }
  // Đọc file và chuyển thành Data URL
  reader.readAsDataURL(file)
}


// ================= ĐỊNH DẠNG NGÀY =================

function formatDateLabel(value) {

  // Nếu không có ngày
  // thì hiển thị "Chưa đặt ngày"
  if (!value) return 'Chưa đặt ngày'

  // Chuyển ngày sang định dạng Việt Nam
  return new Date(value).toLocaleDateString('vi-VN')
}


// ================= THÊM / CẬP NHẬT BANNER =================

async function save() {
  try {
    // Nếu editing có dữ liệu
    // nghĩa là đang sửa banner
    if (editing.value)
        // PUT dùng để cập nhật banner
      await axios.put(
          `/api/admin/banners/${editing.value.id}`,
          form.value
      )
    // Nếu editing = null
    else
        // POST dùng để tạo banner mới
      await axios.post(
          '/api/admin/banners',
          form.value
      )
    // Thông báo lưu thành công
    toast.success('Lưu banner thành công!')
    // Đóng form và reset dữ liệu
    closeForm()
// Lấy lại danh sách banner mới nhất
    fetchBanners()
  } catch (error) {
    // Nếu API bị lỗi thì hiển thị thông báo lỗi
    toast.error(
        error.response?.data?.message ||
        'Lỗi lưu banner!'
    )
  }
}


// ================= XÓA BANNER =================

async function remove(id) {

  // Hiển thị hộp thoại xác nhận trước khi xóa
  const ok = await confirmStore.open({
    title: 'Xóa banner',
    message: 'Banner sẽ bị xóa khỏi trang chủ.',
    confirmText: 'Xóa'
  })

  // Nếu người dùng không đồng ý thì dừng
  if (!ok) return
  try {
    // Gọi API DELETE để xóa banner
    await axios.delete(
        `/api/admin/banners/${id}`
    )
    // Thông báo xóa thành công
    toast.success('Xóa banner thành công!')
    // Lấy lại danh sách banner sau khi xóa
    fetchBanners()
  } catch (error) {
    // Hiển thị thông báo nếu xóa thất bại
    toast.error(
        error.response?.data?.message ||
        'Không thể xóa banner!'
    )
  }
}

// ================= CHO COMPONENT KHÁC GỌI REFRESH =================

// Cho phép component bên ngoài gọi refresh()
// để tải lại danh sách banner
defineExpose({
  refresh: fetchBanners
})

</script>
