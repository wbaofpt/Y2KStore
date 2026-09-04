<template>
  <article class="product-card">
    <!-- KHỐI 1: HÌNH ẢNH VÀ CÁC NHÃN NỔI (BADGE) -->
    <router-link :to="productPath(product)" class="product-card__media">
      <!-- Ảnh sản phẩm: Nếu không có ảnh thì lấy ảnh dự phòng fallbackImage -->
      <img :src="product.imageUrl || fallbackImage" :alt="product.name">

      <!-- Nhãn giảm giá: Chỉ hiện khi phần trăm giảm > 0 -->
      <span v-if="discount > 0" class="product-card__badge">-{{ discount }}%</span>

      <!-- Nhãn sản phẩm mới: Có thêm class dịch xuống nếu vừa có giảm giá vừa là hàng mới -->
      <span v-if="isNewArrival" class="product-card__badge product-card__badge--new" :class="{ 'product-card__badge--offset': discount > 0 }">NEW</span>

      <!-- Icon yêu thích và nút xem nhanh -->
      <span class="product-card__wishlist"><i class="bi bi-heart"></i></span>
      <span class="product-card__quick-view"><i class="bi bi-arrow-up-right"></i> Xem item</span>
    </router-link>

    <!-- KHỐI 2: THÔNG TIN CHI TIẾT BÊN DƯỚI -->
    <div class="product-card__body">

      <!-- Tên sản phẩm: Bấm vào chuyển hướng sang trang chi tiết -->
      <router-link :to="productPath(product)" class="product-card__title">
        {{ product.name }}
      </router-link>

      <!-- Mô tả ngắn: Chỉ hiển thị khi component cha bật props showDescription -->
      <p v-if="showDescription" class="product-card__description">
        {{ shortDescription }}
      </p>

      <!-- Giá tiền: Giá sau khi giảm và giá gốc (nếu có giảm giá) -->
      <div class="product-card__footer">
        <div class="product-card__price-row">
          <span class="product-card__price">{{ formatPrice(finalPrice) }}</span>
          <span v-if="discount > 0" class="product-card__compare">{{ formatPrice(basePrice) }}</span>
        </div>
      </div>

      <!-- Đánh giá sao & số lượng review -->
      <div class="product-card__rating" :aria-label="`Đánh giá ${ratingAverageText} trên 5, ${reviewCountLabel} lượt đánh giá`">
        <span class="product-card__rating-stars" aria-hidden="true">
          <!-- Lặp 5 ngôi sao, ngôi sao nào nhỏ hơn hoặc bằng số sao thực tế sẽ được tô màu vàng -->
          <i v-for="star in 5" :key="star" class="bi bi-star-fill" :class="{ 'is-active': star <= roundedRating }"></i>
        </span>
        <strong>{{ ratingAverageText }}</strong>
        <span class="product-card__rating-count">({{ reviewCountLabel }})</span>
      </div>

      <!-- Nút thêm vào giỏ hàng -->
      <button class="btn product-card__button" @click="addToCart">
        <i class="bi bi-cart-plus me-2"></i>Thêm vào giỏ
      </button>

    </div>
  </article>

  <Teleport to="body">
    <div
      v-if="showVariantModal"
      class="product-variant-modal"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="`variant-title-${product.id}`"
      @click.self="closeVariantModal"
    >
      <div class="product-variant-modal__panel">
        <button type="button" class="product-variant-modal__close" aria-label="Đóng" @click="closeVariantModal">
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
        <span class="product-variant-modal__eyebrow">Chọn sản phẩm</span>
        <h2 :id="`variant-title-${product.id}`">{{ product.name }}</h2>
        <p class="product-variant-modal__hint">Chọn size hoặc màu bạn muốn thêm vào giỏ hàng.</p>

        <div class="product-variant-modal__list">
          <button
            v-for="variant in activeVariants"
            :key="variant.id"
            type="button"
            class="product-variant-modal__option"
            :class="{ 'is-selected': selectedVariantId === variant.id }"
            :disabled="Number(variant.stock || 0) <= 0"
            @click="selectedVariantId = variant.id"
          >
            <span class="product-variant-modal__radio" aria-hidden="true">
              <i v-if="selectedVariantId === variant.id" class="bi bi-check2"></i>
            </span>
            <span class="product-variant-modal__details">
              <strong>{{ variantLabel(variant) }}</strong>
              <small>{{ Number(variant.stock || 0) > 0 ? `Còn ${variant.stock} sản phẩm` : 'Hết hàng' }}</small>
            </span>
            <strong class="product-variant-modal__price">{{ formatPrice(variant.price || basePrice) }}</strong>
          </button>
        </div>

        <div class="product-variant-modal__actions">
          <button type="button" class="btn product-variant-modal__cancel" @click="closeVariantModal">Để sau</button>
          <button type="button" class="btn product-variant-modal__confirm" :disabled="!selectedVariant || Number(selectedVariant.stock || 0) <= 0" @click="confirmAddToCart">
            <i class="bi bi-cart-plus me-2" aria-hidden="true"></i>Thêm vào giỏ
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useCartStore } from '../stores/cart'
import { formatPrice } from '../composables/useFormat'
import { isNewProduct, productPath } from '../composables/useCatalog'

// Nhận dữ liệu truyền từ component cha vào
const props = defineProps({
  product: {
    type: Object,
    required: true // Bắt buộc phải có object sản phẩm
  },
  showDescription: {
    type: Boolean,
    default: false // Mặc định không hiện mô tả ngắn
  }
})

const cartStore = useCartStore()
const fallbackImage = '/favicon.svg' // Ảnh hiển thị tạm nếu ảnh chính bị lỗi
const showVariantModal = ref(false)
const selectedVariantId = ref(null)

// --- CÁC HÀM TÍNH TOÁN TỰ ĐỘNG (COMPUTED) ---
const discount = computed(() => Number(props.product?.promotionDiscountPercent || 0))
const isNewArrival = computed(() => isNewProduct(props.product))
const basePrice = computed(() => Number(props.product?.minVariantPrice || props.product?.price || 0))
const activeVariants = computed(() => (props.product?.variants || []).filter((variant) => variant.status !== false))
const selectedVariant = computed(() => activeVariants.value.find((variant) => variant.id === selectedVariantId.value) || null)

// Tự động tính giá cuối cùng sau khi trừ phần trăm giảm giá
const finalPrice = computed(() => {
  if (discount.value <= 0) return basePrice.value
  return Math.max(basePrice.value * (100 - discount.value) / 100, 0)
})

const reviewCount = computed(() => Number(props.product?.reviewCount || 0))
const ratingAverage = computed(() => Number(props.product?.ratingAverage || 0))
const roundedRating = computed(() => Math.round(ratingAverage.value))
const ratingAverageText = computed(() => ratingAverage.value > 0 ? ratingAverage.value.toFixed(1) : '0.0')

// Thu gọn số lượng đánh giá lớn thành dạng k (nghìn) hoặc m (triệu) cho gọn giao diện
const reviewCountLabel = computed(() => {
  const count = reviewCount.value
  if (count >= 1000000) return `${(count / 1000000).toFixed(1).replace('.0', '')}m`
  if (count >= 1000) return `${(count / 1000).toFixed(1).replace('.0', '')}k`
  return String(count)
})

// Cắt ngắn mô tả tối đa 92 ký tự để card không bị vỡ bố cục
const shortDescription = computed(() => {
  const value = props.product?.description || 'Everyday essentials with a sharper silhouette.'
  return value.length > 92 ? `${value.slice(0, 92)}...` : value
})

// --- HÀNH ĐỘNG KHI BẤM NÚT ---
function addToCart() {
  if (activeVariants.value.length > 1) {
    selectedVariantId.value = activeVariants.value.find((variant) => Number(variant.stock || 0) > 0)?.id || activeVariants.value[0]?.id
    showVariantModal.value = true
    return
  }

  const variant = activeVariants.value[0] || props.product
  cartStore.addItem(variant, 1)
}

function variantLabel(variant) {
  const parts = [variant?.size, variant?.color].filter(Boolean)
  return parts.length ? parts.join(' / ') : 'Biến thể mặc định'
}

function closeVariantModal() {
  showVariantModal.value = false
}

function confirmAddToCart() {
  if (!selectedVariant.value) return
  cartStore.addItem(selectedVariant.value, 1)
  closeVariantModal()
}
</script>

<style scoped>
/* Giao diện khối hiển thị sao đánh giá */
.product-card__rating {
  display: flex;
  align-items: center;
  gap: .35rem;
  min-height: 18px;
  color: #52525b;
  font-size: .7rem;
  line-height: 1;
  white-space: nowrap;
}

.product-card__rating-stars {
  display: inline-flex;
  gap: .08rem;
  color: #d4d4d8;
  font-size: .68rem;
}

/* Tô màu vàng cho các ngôi sao đạt chuẩn */
.product-card__rating-stars .is-active { color: #f59e0b; }
.product-card__rating strong { color: #3f3f46; font-size: .7rem; }
.product-card__rating-count { color: #8b95a2; }

/* Tùy chỉnh màu sắc riêng cho nhãn NEW */
.product-card__badge--new {
  background: #ecfeff !important;
  color: #0e7490 !important;
  box-shadow: 3px 3px 0 #f0abfc !important;
  letter-spacing: .08em;
}

/* Dịch nhãn NEW xuống nếu phía trên nó đã có nhãn giảm giá */
.product-card__badge--offset {
  top: 2.65rem !important;
}

.product-variant-modal {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, .58);
  backdrop-filter: blur(6px);
}

.product-variant-modal__panel {
  position: relative;
  width: min(100%, 520px);
  max-height: min(90vh, 680px);
  overflow: auto;
  padding: clamp(1.35rem, 4vw, 2rem);
  border: 1px solid #d7e4f4;
  border-radius: 1.35rem;
  background: #fff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, .25);
}

.product-variant-modal__close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 2.4rem;
  height: 2.4rem;
  border: 1px solid #dbe2ea;
  border-radius: 50%;
  background: #f8fafc;
  color: #334155;
}

.product-variant-modal__eyebrow {
  color: #0086a8;
  font-size: .72rem;
  font-weight: 900;
  letter-spacing: .14em;
  text-transform: uppercase;
}

.product-variant-modal h2 { max-width: calc(100% - 3rem); margin: .45rem 0 .35rem; color: #111827; font-size: clamp(1.35rem, 4vw, 1.8rem); }
.product-variant-modal__hint { margin-bottom: 1.2rem; color: #64748b; font-size: .9rem; }
.product-variant-modal__list { display: grid; gap: .65rem; }
.product-variant-modal__option { display: flex; align-items: center; gap: .8rem; width: 100%; padding: .9rem 1rem; border: 1px solid #dbe3ec; border-radius: .9rem; background: #fff; color: #1e293b; text-align: left; transition: border-color .2s, background .2s, transform .2s; }
.product-variant-modal__option:not(:disabled):hover { border-color: #09a5c8; transform: translateY(-1px); }
.product-variant-modal__option.is-selected { border-color: #08a8c9; background: #effcff; box-shadow: 0 0 0 3px rgba(8, 168, 201, .1); }
.product-variant-modal__option:disabled { cursor: not-allowed; opacity: .5; }
.product-variant-modal__radio { display: grid; place-items: center; flex: 0 0 1.25rem; width: 1.25rem; height: 1.25rem; border: 1px solid #94a3b8; border-radius: 50%; color: #fff; font-size: .8rem; }
.is-selected .product-variant-modal__radio { border-color: #0798b8; background: #0798b8; }
.product-variant-modal__details { display: grid; flex: 1; gap: .22rem; }
.product-variant-modal__details small { color: #64748b; font-size: .76rem; }
.product-variant-modal__price { color: #e11d48; font-size: .86rem; white-space: nowrap; }
.product-variant-modal__actions { display: flex; justify-content: flex-end; gap: .65rem; margin-top: 1.35rem; }
.product-variant-modal__cancel, .product-variant-modal__confirm { min-height: 2.75rem; padding: .65rem 1rem; border-radius: .65rem; font-weight: 800; }
.product-variant-modal__cancel { border: 1px solid #cbd5e1; background: #fff; color: #334155; }
.product-variant-modal__confirm { border: 1px solid #111827; background: #111827; color: #fff; box-shadow: 4px 4px 0 #6ee7f5; }
.product-variant-modal__confirm:disabled { cursor: not-allowed; opacity: .5; box-shadow: none; }

@media (max-width: 480px) {
  .product-variant-modal__panel { border-radius: 1rem; }
  .product-variant-modal__actions { flex-direction: column-reverse; }
  .product-variant-modal__actions button { width: 100%; }
}
</style>
