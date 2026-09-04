<template>
  <div class="admin-section admin-db-page">
    <header class="admin-db-hero">
      <div class="admin-db-hero__copy">
        <span class="admin-kicker">Database map</span>
        <h4 class="admin-page-title">Sơ đồ database</h4>
        <p class="admin-db-hero__lead">
          Trang này gom các bảng theo domain để bạn đọc nhanh luồng dữ liệu.
          Các cột ảnh hiện tại được lưu dưới dạng chuỗi, URL hoặc base64 trong DB, không phải file trên ổ đĩa.
        </p>
      </div>

      <div class="admin-db-hero__facts" aria-label="Tóm tắt database">
        <div class="admin-db-fact">
          <span>Tổng bảng</span>
          <strong>{{ tables.length }}</strong>
        </div>
        <div class="admin-db-fact">
          <span>Quản lý trực tiếp</span>
          <strong>{{ directCount }}</strong>
        </div>
        <div class="admin-db-fact">
          <span>Qua module liên quan</span>
          <strong>{{ linkedCount }}</strong>
        </div>
        <div class="admin-db-fact">
          <span>Ảnh / media dạng chuỗi</span>
          <strong>{{ mediaCount }}</strong>
        </div>
      </div>
    </header>

    <section class="admin-db-toolbar" aria-label="Bộ lọc bảng">
      <div class="admin-db-search">
        <i class="bi bi-search"></i>
        <input
          v-model="search"
          type="search"
          class="form-control form-control-sm"
          placeholder="Tìm bảng, cột hoặc mô tả..."
        >
      </div>

      <div class="admin-db-filters">
        <button
          type="button"
          class="btn btn-sm"
          :class="modeFilter === 'all' ? 'btn-y2k-primary' : 'btn-y2k-outline'"
          @click="modeFilter = 'all'"
        >
          Tất cả
        </button>
        <button
          type="button"
          class="btn btn-sm"
          :class="modeFilter === 'direct' ? 'btn-y2k-primary' : 'btn-y2k-outline'"
          @click="modeFilter = 'direct'"
        >
          Quản lý trực tiếp
        </button>
        <button
          type="button"
          class="btn btn-sm"
          :class="modeFilter === 'linked' ? 'btn-y2k-primary' : 'btn-y2k-outline'"
          @click="modeFilter = 'linked'"
        >
          Qua module
        </button>
      </div>
    </section>

    <section class="admin-db-legend">
      <div class="admin-db-legend__item">
        <span class="admin-db-chip admin-db-chip--crud">CRUD</span>
        <p>Bảng có màn hình quản trị riêng trong admin.</p>
      </div>
      <div class="admin-db-legend__item">
        <span class="admin-db-chip admin-db-chip--readonly">LINK</span>
        <p>Bảng nối hoặc bảng con, thường chỉnh qua form liên quan.</p>
      </div>
      <div class="admin-db-legend__item">
        <span class="admin-db-chip admin-db-chip--media">MEDIA</span>
        <p>Cột ảnh, video hoặc avatar lưu chuỗi JSON, base64 hoặc URL.</p>
      </div>
    </section>

    <div class="admin-db-groups">
      <article v-for="group in visibleGroups" :key="group.key" class="admin-db-group">
        <div class="admin-db-group__head">
          <div>
            <span class="admin-db-group__eyebrow">{{ group.eyebrow }}</span>
            <h5>{{ group.title }}</h5>
            <p>{{ group.description }}</p>
          </div>
          <span class="admin-db-group__count">{{ group.tables.length }} bảng</span>
        </div>

        <div class="admin-db-grid">
          <article v-for="table in group.tables" :key="table.name" class="admin-db-card">
            <div class="admin-db-card__header">
              <div>
                <div class="admin-db-card__title">{{ table.label }}</div>
                <div class="admin-db-card__meta">
                  <span>{{ table.name }}</span>
                  <span v-if="table.moduleHint">{{ table.moduleHint }}</span>
                </div>
              </div>
              <span :class="statusClass(table.mode)" class="admin-db-chip">
                {{ statusLabel(table.mode) }}
              </span>
            </div>

            <p class="admin-db-card__description">{{ table.description }}</p>

            <div class="admin-db-card__notes">
              <span v-if="table.highlight" class="admin-db-note">{{ table.highlight }}</span>
              <span v-if="table.keyRole" class="admin-db-note admin-db-note--soft">{{ table.keyRole }}</span>
            </div>

            <div class="admin-db-fields">
              <span v-for="field in table.fields" :key="field">{{ field }}</span>
            </div>

            <button
              v-if="table.targetTab"
              class="btn btn-y2k-outline btn-sm admin-db-card__action"
              @click="emit('navigate', table.targetTab)"
            >
              <i class="bi bi-box-arrow-in-right me-1"></i>
              Mở module
            </button>
          </article>
        </div>
      </article>
    </div>

    <div v-if="!visibleGroups.length" class="admin-db-empty">
      <i class="bi bi-diagram-3"></i>
      <strong>Không tìm thấy bảng phù hợp</strong>
      <span>Thử đổi từ khóa hoặc chuyển về bộ lọc “Tất cả”.</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const emit = defineEmits(['navigate'])

const search = ref('')
const modeFilter = ref('all')

const tables = [
  {
    group: 'core',
    name: 'role',
    label: 'Vai trò',
    mode: 'linked',
    targetTab: 'users',
    moduleHint: 'Gắn với users',
    description: 'Chuẩn hóa ROLE_USER / ROLE_ADMIN để phân quyền trong toàn hệ thống.',
    fields: ['id', 'role_name'],
    keyRole: 'Quyền truy cập'
  },
  {
    group: 'core',
    name: 'user',
    label: 'Người dùng',
    mode: 'direct',
    targetTab: 'users',
    moduleHint: 'Admin Users',
    description: 'Lưu tài khoản khách hàng/admin, avatar, trạng thái, xác minh email và nguồn đăng nhập.',
    fields: ['id', 'role_id', 'password', 'full_name', 'email', 'phone', 'avatar_url', 'email_verified', 'auth_provider', 'reset_token', 'reset_token_expires_at', 'created_at', 'status'],
    highlight: 'avatar_url lưu chuỗi ảnh',
    keyRole: 'Tài khoản'
  },
  {
    group: 'core',
    name: 'email_verifications',
    label: 'Xác minh email',
    mode: 'direct',
    targetTab: 'users',
    moduleHint: 'Auth / reset',
    description: 'Lưu mã OTP, mục đích xác minh và thời hạn hết hạn cho email.',
    fields: ['id', 'email', 'purpose', 'code', 'expires_at', 'created_at'],
    keyRole: 'OTP'
  },
  {
    group: 'core',
    name: 'address',
    label: 'Địa chỉ',
    mode: 'direct',
    targetTab: 'addresses',
    moduleHint: 'Admin Addresses',
    description: 'Địa chỉ giao hàng của từng tài khoản, kèm cờ địa chỉ mặc định.',
    fields: ['id', 'user_id', 'province', 'district', 'ward', 'detail', 'is_default'],
    keyRole: 'Giao hàng'
  },
  {
    group: 'catalog',
    name: 'categories',
    label: 'Danh mục',
    mode: 'direct',
    targetTab: 'categories',
    moduleHint: 'Admin Categories',
    description: 'Danh mục chung và tag, hỗ trợ quan hệ cha-con cho bộ lọc và phân loại.',
    fields: ['id', 'name', 'description', 'category_type', 'parent_category_id', 'status'],
    keyRole: 'Phân loại'
  },
  {
    group: 'catalog',
    name: 'products',
    label: 'Sản phẩm',
    mode: 'direct',
    targetTab: 'products',
    moduleHint: 'Admin Products',
    description: 'Bảng lõi của catalog: tên, mô tả, giá, ảnh đại diện và trạng thái.',
    fields: ['id', 'category_id', 'name', 'description', 'price', 'image', 'status', 'created_at'],
    highlight: 'image lưu chuỗi ảnh',
    keyRole: 'Catalog'
  },
  {
    group: 'catalog',
    name: 'product_categories',
    label: 'Nối danh mục',
    mode: 'linked',
    targetTab: 'products',
    moduleHint: 'Product form',
    description: 'Bảng liên kết cho phép một sản phẩm thuộc nhiều tag danh mục.',
    fields: ['product_id', 'category_id'],
    keyRole: 'Bảng nối'
  },
  {
    group: 'catalog',
    name: 'product_images',
    label: 'Ảnh phụ sản phẩm',
    mode: 'linked',
    targetTab: 'products',
    moduleHint: 'Product detail',
    description: 'Danh sách ảnh bổ sung hiển thị trong trang chi tiết sản phẩm.',
    fields: ['id', 'product_id', 'image_url'],
    highlight: 'image_url là chuỗi',
    keyRole: 'Media'
  },
  {
    group: 'catalog',
    name: 'product_variants',
    label: 'Biến thể',
    mode: 'linked',
    targetTab: 'products',
    moduleHint: 'Product detail',
    description: 'Size, màu, tồn kho, giá và trạng thái của từng biến thể.',
    fields: ['id', 'product_id', 'size', 'color', 'stock', 'price', 'status'],
    keyRole: 'Kho'
  },
  {
    group: 'content',
    name: 'banner',
    label: 'Banner',
    mode: 'direct',
    targetTab: 'banners',
    moduleHint: 'Admin Banners',
    description: 'Banner trang chủ, có thể gắn với một sản phẩm và thứ tự hiển thị.',
    fields: ['id', 'product_id', 'image', 'title', 'subtitle', 'button_text', 'sort_order', 'date_banner', 'status'],
    highlight: 'image lưu chuỗi/base64',
    keyRole: 'Hero content'
  },
  {
    group: 'content',
    name: 'promotions',
    label: 'Khuyến mãi',
    mode: 'direct',
    targetTab: 'promotions',
    moduleHint: 'Admin Promotions',
    description: 'Tên chương trình, phần trăm giảm giá, ngày hiệu lực và trạng thái.',
    fields: ['id', 'promotion_name', 'discount_percent', 'start_date', 'end_date', 'status'],
    keyRole: 'Giảm giá'
  },
  {
    group: 'content',
    name: 'product_promotions',
    label: 'Nối khuyến mãi',
    mode: 'linked',
    targetTab: 'promotions',
    moduleHint: 'Product promotions',
    description: 'Bảng nối giữa sản phẩm và chương trình khuyến mãi.',
    fields: ['product_id', 'promotion_id'],
    keyRole: 'Bảng nối'
  },
  {
    group: 'sales',
    name: 'coupons',
    label: 'Mã giảm giá',
    mode: 'direct',
    targetTab: 'coupons',
    moduleHint: 'Admin Coupons',
    description: 'Mã coupon, giá trị giảm và ngày hết hạn.',
    fields: ['id', 'promotion_id', 'coupon_code', 'discount_value', 'expire_date', 'status'],
    keyRole: 'Voucher'
  },
  {
    group: 'sales',
    name: 'orders',
    label: 'Đơn hàng',
    mode: 'direct',
    targetTab: 'orders',
    moduleHint: 'Admin Orders',
    description: 'Đơn hàng, tổng tiền, coupon và trạng thái xử lý.',
    fields: ['id', 'user_id', 'coupon_id', 'address_id', 'order_date', 'total_amount', 'status', 'hidden_from_history'],
    keyRole: 'Giao dịch'
  },
  {
    group: 'sales',
    name: 'order_details',
    label: 'Chi tiết đơn',
    mode: 'linked',
    targetTab: 'orders',
    moduleHint: 'Order modal',
    description: 'Từng dòng sản phẩm nằm trong một đơn hàng.',
    fields: ['id', 'order_id', 'variant_id', 'quantity', 'unit_price'],
    keyRole: 'Line items'
  },
  {
    group: 'sales',
    name: 'payments',
    label: 'Thanh toán',
    mode: 'direct',
    targetTab: 'payments',
    moduleHint: 'Admin Payments',
    description: 'Phương thức, số tiền, ngày thanh toán và trạng thái.',
    fields: ['id', 'order_id', 'method', 'amount', 'payment_date', 'status'],
    keyRole: 'Payment'
  },
  {
    group: 'activity',
    name: 'cart_items',
    label: 'Giỏ hàng',
    mode: 'linked',
    targetTab: 'users',
    moduleHint: 'Cart service',
    description: 'Item đang nằm trong giỏ, gắn với người dùng và biến thể.',
    fields: ['id', 'user_id', 'variant_id', 'quantity'],
    keyRole: 'Tạm thời'
  },
  {
    group: 'activity',
    name: 'reviews',
    label: 'Đánh giá',
    mode: 'direct',
    targetTab: 'reviews',
    moduleHint: 'Admin Reviews',
    description: 'Số sao, bình luận và media phản hồi của khách hàng.',
    fields: ['id', 'user_id', 'product_id', 'rating', 'comment', 'image_urls', 'video_urls', 'created_at'],
    highlight: 'image_urls/video_urls lưu JSON text',
    keyRole: 'Feedback'
  }
]

const groupMeta = [
  {
    key: 'core',
    eyebrow: 'Accounts',
    title: 'Người dùng và truy cập',
    description: 'Bảng gốc để xác thực, phân quyền và lưu thông tin giao hàng.'
  },
  {
    key: 'catalog',
    eyebrow: 'Catalog',
    title: 'Danh mục và sản phẩm',
    description: 'Các bảng mô tả hàng hóa, ảnh, tag và biến thể.'
  },
  {
    key: 'content',
    eyebrow: 'Content',
    title: 'Banner và khuyến mãi',
    description: 'Bảng phục vụ trang chủ, promo và liên kết marketing.'
  },
  {
    key: 'sales',
    eyebrow: 'Sales',
    title: 'Đơn hàng và thanh toán',
    description: 'Luồng mua hàng, coupon, thanh toán và chi tiết đơn.'
  },
  {
    key: 'activity',
    eyebrow: 'Activity',
    title: 'Tương tác khách hàng',
    description: 'Giỏ hàng và đánh giá sau mua.'
  }
]

const directCount = computed(() => tables.filter((table) => table.mode === 'direct').length)
const linkedCount = computed(() => tables.filter((table) => table.mode === 'linked').length)
const mediaCount = computed(() => tables.filter((table) => table.highlight).length)

function statusLabel(mode) {
  return mode === 'direct' ? 'Quản lý trực tiếp' : 'Qua module'
}

function statusClass(mode) {
  return mode === 'direct'
    ? 'admin-db-chip admin-db-chip--crud'
    : 'admin-db-chip admin-db-chip--readonly'
}

function matchesQuery(table, query) {
  if (!query) return true
  const haystack = [
    table.group,
    table.eyebrow,
    table.title,
    table.name,
    table.label,
    table.description,
    table.moduleHint,
    table.highlight,
    table.keyRole,
    ...(table.fields || [])
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()

  return haystack.includes(query)
}

const visibleGroups = computed(() => {
  const query = search.value.trim().toLowerCase()

  return groupMeta
    .map((group) => {
      const groupTables = tables.filter((table) => {
        const matchesGroup = table.group === group.key
        const matchesMode = modeFilter.value === 'all' || table.mode === modeFilter.value
        return matchesGroup && matchesMode && matchesQuery(table, query)
      })

      return {
        ...group,
        tables: groupTables
      }
    })
    .filter((group) => group.tables.length > 0)
})
</script>

<style scoped>
.admin-db-page {
  display: grid;
  gap: 1rem;
}

.admin-db-hero,
.admin-db-toolbar,
.admin-db-legend,
.admin-db-group,
.admin-db-empty {
  border: 1px solid rgba(17, 17, 17, 0.08);
  border-radius: 18px;
  background: #fff;
  box-shadow: var(--y2k-shadow);
}

.admin-db-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.9fr);
  gap: 1rem;
  padding: 1.25rem;
}

.admin-db-hero__copy {
  display: grid;
  align-content: start;
  gap: 0.6rem;
}

.admin-db-hero__lead {
  margin: 0;
  max-width: 760px;
  color: var(--y2k-muted);
  line-height: 1.7;
}

.admin-db-hero__facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.admin-db-fact {
  padding: 0.9rem;
  border-radius: 14px;
  border: 1px solid rgba(17, 17, 17, 0.06);
  background: var(--y2k-surface-2);
}

.admin-db-fact span {
  display: block;
  color: var(--y2k-muted);
  font-size: 0.78rem;
  line-height: 1.4;
}

.admin-db-fact strong {
  display: block;
  margin-top: 0.3rem;
  font-size: 1.7rem;
  line-height: 1;
}

.admin-db-toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.15rem;
}

.admin-db-search {
  flex: 1 1 320px;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  min-height: 44px;
  padding: 0 0.85rem;
  border-radius: 12px;
  border: 1px solid rgba(17, 17, 17, 0.1);
  background: var(--y2k-surface-2);
}

.admin-db-search i {
  color: var(--y2k-muted);
}

.admin-db-search input {
  border: 0;
  box-shadow: none;
  background: transparent;
}

.admin-db-search input:focus {
  outline: none;
  box-shadow: none;
}

.admin-db-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.admin-db-legend {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
  padding: 1rem 1.15rem;
}

.admin-db-legend__item {
  display: grid;
  gap: 0.45rem;
  padding: 0.85rem;
  border-radius: 14px;
  background: var(--y2k-surface-2);
}

.admin-db-legend__item p {
  margin: 0;
  color: var(--y2k-muted);
  font-size: 0.84rem;
  line-height: 1.55;
}

.admin-db-groups {
  display: grid;
  gap: 1rem;
}

.admin-db-group {
  padding: 1rem;
}

.admin-db-group__head {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
  margin-bottom: 1rem;
}

.admin-db-group__head h5 {
  margin: 0.1rem 0 0.3rem;
  font-family: var(--font-display);
  font-size: 1.12rem;
}

.admin-db-group__head p {
  margin: 0;
  color: var(--y2k-muted);
  line-height: 1.6;
}

.admin-db-group__eyebrow {
  display: inline-flex;
  margin-bottom: 0.2rem;
  color: var(--y2k-accent);
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
}

.admin-db-group__count {
  padding: 0.4rem 0.7rem;
  border-radius: 999px;
  border: 1px solid rgba(17, 17, 17, 0.08);
  background: var(--y2k-accent-soft);
  color: var(--y2k-accent-strong);
  font-size: 0.8rem;
  font-weight: 800;
  white-space: nowrap;
}

.admin-db-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.85rem;
}

.admin-db-card {
  display: grid;
  gap: 0.85rem;
  padding: 1rem;
  border: 1px solid rgba(17, 17, 17, 0.08);
  border-radius: 16px;
  background: #fff;
}

.admin-db-card__header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 0.75rem;
}

.admin-db-card__title {
  font-family: var(--font-display);
  font-size: 1rem;
}

.admin-db-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-top: 0.25rem;
  color: var(--y2k-muted);
  font-size: 0.75rem;
}

.admin-db-card__meta span + span::before {
  content: '•';
  margin-right: 0.45rem;
}

.admin-db-card__description {
  margin: 0;
  color: var(--y2k-text);
  line-height: 1.7;
}

.admin-db-card__notes {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.admin-db-note {
  padding: 0.32rem 0.6rem;
  border-radius: 999px;
  background: var(--y2k-accent-soft);
  color: var(--y2k-accent-strong);
  font-size: 0.72rem;
  font-weight: 800;
}

.admin-db-note--soft {
  background: #f5f5f5;
  color: var(--y2k-text);
}

.admin-db-card__action {
  width: fit-content;
}

.admin-db-empty {
  display: grid;
  justify-items: center;
  gap: 0.5rem;
  padding: 2rem 1rem;
  text-align: center;
}

.admin-db-empty i {
  font-size: 2rem;
  color: var(--y2k-accent);
}

.admin-db-empty span {
  color: var(--y2k-muted);
}

.admin-db-chip--media {
  background: #f5f5f5;
  color: var(--y2k-text);
}

@media (max-width: 1100px) {
  .admin-db-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .admin-db-hero,
  .admin-db-legend {
    grid-template-columns: 1fr;
  }

  .admin-db-hero__facts {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-db-grid,
  .admin-db-hero__facts,
  .admin-db-legend {
    grid-template-columns: 1fr;
  }

  .admin-db-group__head,
  .admin-db-card__header {
    flex-direction: column;
  }

  .admin-db-card__action {
    width: 100%;
  }
}
</style>
