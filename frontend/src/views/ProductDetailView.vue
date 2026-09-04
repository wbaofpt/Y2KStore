<template>
  <main class="product-detail-v2">
    <div v-if="loading" class="product-detail-v2__loading surface-card">
      <span class="spinner-border spinner-border-sm"></span>
      Đang tải sản phẩm...
    </div>

    <template v-else-if="product">
      <nav class="product-detail-v2__breadcrumb" aria-label="breadcrumb">
        <router-link to="/">Trang chủ</router-link>
        <i class="bi bi-chevron-right"></i>
        <router-link to="/shop">Cửa hàng</router-link>
        <i class="bi bi-chevron-right"></i>
        <span>{{ product.name }}</span>
      </nav>

      <section class="product-detail-v2__hero">
        <div class="product-detail-v2__gallery">
          <div class="product-detail-v2__main-image">
            <img :src="mainImage" :alt="product.name">
            <span v-if="hasPromotion" class="product-detail-v2__sale">-{{ product.promotionDiscountPercent }}%</span>
            <span class="product-detail-v2__image-count">{{ galleryImages.length }} ảnh</span>
          </div>
          <div v-if="galleryImages.length > 1" class="product-detail-v2__thumbs">
            <button
              v-for="image in galleryImages"
              :key="image.id || image.imageUrl"
              type="button"
              class="product-detail-v2__thumb"
              :class="{ 'is-active': mainImage === image.imageUrl }"
              @click="selectedImage = image.imageUrl"
            >
              <img :src="image.imageUrl" :alt="product.name">
            </button>
          </div>
          <div class="product-detail-v2__gallery-note">
            <i class="bi bi-stars"></i>
            <span>Ảnh sản phẩm được giữ tỷ lệ lớn để bạn xem rõ chất liệu và form.</span>
          </div>
        </div>

        <aside class="product-detail-v2__summary">
          <div class="product-detail-v2__eyebrow-row">
            <div class="product-detail-v2__chips">
              <span v-for="category in categoryChips" :key="category">{{ category }}</span>
              <span v-if="!categoryChips.length">Y2K collection</span>
            </div>
            <span class="product-detail-v2__rating"><i class="bi bi-star-fill"></i> {{ ratingAverageText }} · {{ reviewCount }}</span>
          </div>

          <h1>{{ product.name }}</h1>
          <p class="product-detail-v2__intro">
            {{ product.description || 'Một item Y2K dễ phối, form gọn và sẵn sàng nâng cấp outfit hằng ngày của bạn.' }}
          </p>

          <div class="product-detail-v2__price-row">
            <div>
              <span class="product-detail-v2__price-label">Giá bán</span>
              <div class="product-detail-v2__price">
                <strong>{{ formatPrice(finalPrice) }}</strong>
                <del v-if="hasPromotion">{{ formatPrice(basePrice) }}</del>
              </div>
            </div>
            <span v-if="hasPromotion" class="product-detail-v2__saving">Tiết kiệm {{ product.promotionDiscountPercent }}%</span>
          </div>

          <div class="product-detail-v2__facts">
            <div><i class="bi bi-box-seam"></i><span><b>{{ stockAvailable }}</b> sản phẩm còn lại</span></div>
            <div><i class="bi bi-bag-check"></i><span><b>{{ product.soldCount || 0 }}</b> lượt mua</span></div>
            <div><i class="bi bi-shield-check"></i><span>Đổi trả trong 7 ngày</span></div>
          </div>

          <div v-if="colors.length" class="product-detail-v2__option">
            <div class="product-detail-v2__option-head"><span>Màu sắc</span><b>{{ selectedColor || 'Chọn màu' }}</b></div>
            <div class="product-detail-v2__choices">
              <button v-for="color in colors" :key="color" type="button" :class="{ 'is-active': selectedColor === color }" @click="selectedColor = color">{{ color }}</button>
            </div>
          </div>

          <div v-if="sizes.length" class="product-detail-v2__option">
            <div class="product-detail-v2__option-head"><span>Kích thước</span><b>{{ selectedSize || 'Chọn size' }}</b></div>
            <div class="product-detail-v2__choices">
              <button v-for="size in sizes" :key="size" type="button" :class="{ 'is-active': selectedSize === size }" @click="selectedSize = size">{{ size }}</button>
            </div>
          </div>

          <div class="product-detail-v2__buy-row">
            <div class="product-detail-v2__quantity">
              <button type="button" aria-label="Giảm số lượng" @click="changeQty(-1)">−</button>
              <strong>{{ quantity }}</strong>
              <button type="button" aria-label="Tăng số lượng" @click="changeQty(1)">+</button>
            </div>
            <span>{{ selectedVariant?.stock ?? product.stock ?? 0 }} có sẵn</span>
          </div>

          <div class="product-detail-v2__actions">
            <button class="btn btn-y2k-primary" :disabled="!selectedVariant" @click="addToCart(false)"><i class="bi bi-bag-plus me-2"></i>Thêm vào giỏ</button>
            <button class="btn product-detail-v2__buy-now" :disabled="!selectedVariant" @click="addToCart(true)">Mua ngay</button>
          </div>

          <div class="product-detail-v2__shipping">
            <div><i class="bi bi-truck"></i><span><b>Giao hàng nhanh</b><small>Miễn phí từ đơn 499.000đ</small></span></div>
            <div><i class="bi bi-chat-heart"></i><span><b>Cần tư vấn?</b><small>Nhắn tin để được phối đồ</small></span></div>
          </div>
        </aside>
      </section>

      <section class="product-detail-v2__below">
        <div class="product-detail-v2__content surface-card">
          <div class="product-detail-v2__section-head">
            <span>Thông tin sản phẩm</span>
            <h2>Chi tiết item</h2>
          </div>
          <div class="product-detail-v2__description-wrap">
            <p class="product-detail-v2__description">{{ descriptionExpanded || !hasLongDescription ? fullDescription : `${descriptionPreview}...` }}</p>
            <button v-if="hasLongDescription" type="button" class="product-detail-v2__more" @click="descriptionExpanded = !descriptionExpanded">
              {{ descriptionExpanded ? 'Thu gọn mô tả' : 'Xem thêm mô tả' }}
              <i class="bi" :class="descriptionExpanded ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
            </button>
          </div>
          <div class="product-detail-v2__bullets">
            <div><i class="bi bi-check2"></i><span>Chọn đúng màu, size và số lượng trước khi thêm vào giỏ.</span></div>
            <div><i class="bi bi-check2"></i><span>Giá hiển thị đã tự động áp dụng khuyến mãi đang hoạt động.</span></div>
            <div><i class="bi bi-check2"></i><span>Tồn kho được kiểm tra theo đúng biến thể bạn chọn.</span></div>
          </div>

          <div class="product-detail-v2__reviews">
            <div class="product-detail-v2__review-head">
              <div><span>Feedback</span><h2>Khách hàng nói gì?</h2></div>
              <div class="product-detail-v2__review-score"><strong>{{ ratingAverageText }}</strong><div class="product-detail-v2__stars"><i v-for="star in 5" :key="star" class="bi bi-star-fill" :class="{ 'is-active': star <= roundedRating }"></i></div><small>{{ reviewCount }} đánh giá</small></div>
            </div>

            <form v-if="reviewEligibility.canReview" class="product-detail-v2__review-form" @submit.prevent="submitReview">
              <div class="product-detail-v2__review-stars"><span>Đánh giá của bạn</span><button v-for="star in 5" :key="star" type="button" :class="{ 'is-active': star <= reviewForm.rating }" @click="reviewForm.rating = star"><i class="bi bi-star-fill"></i></button></div>
              <textarea v-model="reviewForm.comment" class="form-control" rows="4" placeholder="Chia sẻ cảm nhận sau khi mua sản phẩm..."></textarea>
              <label class="product-detail-v2__review-upload">
                <i class="bi bi-cloud-arrow-up"></i>
                <span>Thêm ảnh hoặc video</span>
                <small>Tối đa 5 ảnh và 2 video, mỗi tệp dưới 12MB.</small>
                <input type="file" accept="image/*,video/*" multiple @change="handleReviewMediaUpload">
              </label>
              <div v-if="reviewForm.imageUrls.length || reviewForm.videoUrls.length" class="product-detail-v2__review-media-preview">
                <button v-for="(image, index) in reviewForm.imageUrls" :key="`image-${index}`" type="button" @click="removeReviewMedia('imageUrls', index)">
                  <img :src="image" alt="Ảnh đánh giá">
                  <i class="bi bi-x-circle-fill"></i>
                </button>
                <button v-for="(video, index) in reviewForm.videoUrls" :key="`video-${index}`" type="button" @click="removeReviewMedia('videoUrls', index)">
                  <video :src="video" muted></video>
                  <i class="bi bi-x-circle-fill"></i>
                </button>
              </div>
              <button class="btn btn-y2k-primary" :disabled="submittingReview">{{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}</button>
            </form>
            <div v-else class="product-detail-v2__review-gate"><i class="bi bi-lock"></i><div><b>{{ reviewGateTitle }}</b><p>{{ reviewGateMessage }}</p></div></div>

            <div v-if="reviews.length" class="product-detail-v2__review-list">
              <article v-for="review in reviews" :key="review.id" class="product-detail-v2__review-card">
                <div class="product-detail-v2__review-author"><span class="product-detail-v2__avatar">{{ reviewerInitials(review) }}</span><div><b>{{ review.userFullName || 'Khách hàng' }}</b><small>{{ formatDate(review.createdAt) }}</small></div></div>
                <div class="product-detail-v2__stars product-detail-v2__stars--small"><i v-for="star in 5" :key="star" class="bi bi-star-fill" :class="{ 'is-active': star <= Number(review.rating || 0) }"></i></div>
                <p>{{ review.comment || 'Khách chưa để lại nội dung đánh giá.' }}</p>
                <div v-if="reviewMedia(review).length" class="product-detail-v2__review-media-grid">
                  <template v-for="media in reviewMedia(review)" :key="media.src">
                    <img v-if="media.type === 'image'" :src="media.src" alt="Ảnh đánh giá sản phẩm">
                    <video v-else :src="media.src" controls></video>
                  </template>
                </div>
              </article>
            </div>
            <p v-else class="product-detail-v2__empty-review">Chưa có đánh giá. Những feedback đầu tiên sẽ xuất hiện tại đây.</p>
          </div>
        </div>

        <aside class="product-detail-v2__side-note">
          <div><span>Styling note</span><h3>Chọn item đúng mood của bạn.</h3><p>Phối cùng denim, sneaker hoặc layer sáng màu để tạo một outfit Y2K có điểm nhấn.</p><router-link to="/shop" class="btn btn-y2k-outline">Khám phá thêm</router-link></div>
        </aside>
      </section>

      <section class="product-detail-v2__experience">
        <div class="product-detail-v2__section-head"><span>Why you'll love it</span><h2>Một item, nhiều cách mặc.</h2></div>
        <div class="product-detail-v2__experience-grid">
          <article><i class="bi bi-stars"></i><b>Đúng chất Y2K</b><p>Điểm nhấn đủ nổi bật để outfit không bị nhàm chán.</p></article>
          <article><i class="bi bi-layers"></i><b>Dễ layer</b><p>Phối cùng áo thun, denim hoặc cardigan đều hợp mood.</p></article>
          <article><i class="bi bi-rulers"></i><b>Chọn size nhanh</b><p>Form được hiển thị rõ, kiểm tra tồn kho theo từng biến thể.</p></article>
          <article><i class="bi bi-box2-heart"></i><b>Đóng gói chỉn chu</b><p>Đơn hàng được kiểm tra trước khi rời khỏi shop.</p></article>
        </div>
      </section>

      <section class="product-detail-v2__guide">
        <div class="product-detail-v2__guide-card product-detail-v2__guide-card--pink">
          <span class="section-heading__eyebrow">Style idea / 01</span><h3>Soft girl hay cyber?</h3><p>Đổi vibe chỉ bằng cách thay phụ kiện và một đôi giày. Item này sẵn sàng đi cùng bạn.</p><router-link to="/shop" class="product-detail-v2__text-link">Xem thêm cảm hứng <i class="bi bi-arrow-up-right"></i></router-link>
        </div>
        <div class="product-detail-v2__guide-card product-detail-v2__guide-card--cyan">
          <span class="section-heading__eyebrow">Size guide / 02</span><h3>Chọn size thật dễ</h3><div class="product-detail-v2__size-line"><span>Form</span><b>Regular / Oversize</b></div><div class="product-detail-v2__size-line"><span>Gợi ý</span><b>Chọn size thường mặc</b></div><small>Nếu cần tư vấn size, hãy nhắn tin cho shop trước khi đặt hàng.</small>
        </div>
      </section>

      <section class="product-detail-v2__faq surface-card">
        <div class="product-detail-v2__section-head"><span>Need to know</span><h2>Câu hỏi nhanh</h2></div>
        <div class="product-detail-v2__faq-grid"><details open><summary>Sản phẩm có được kiểm tra trước khi gửi?</summary><p>Có. Shop kiểm tra mẫu, biến thể và số lượng trước khi đóng gói.</p></details><details><summary>Đổi size trong bao lâu?</summary><p>Hỗ trợ đổi trong 7 ngày khi sản phẩm còn nguyên tem và chưa qua sử dụng.</p></details><details><summary>Khi nào đơn được giao?</summary><p>Đơn thường được xử lý trong 1–2 ngày làm việc và có thông báo trạng thái.</p></details></div>
      </section>

      <section v-if="relatedProducts.length" class="product-detail-v2__related">
        <div class="product-detail-v2__related-head">
          <div class="product-detail-v2__section-head"><span>Sản phẩm cùng danh mục</span><h2>Có thể bạn sẽ thích</h2></div>
          <div v-if="relatedProducts.length > relatedVisibleCount" class="product-detail-v2__related-controls">
            <button type="button" aria-label="Xem sản phẩm trước" :disabled="relatedOffset === 0" @click="moveRelated(-1)"><i class="bi bi-arrow-left"></i></button>
            <button type="button" aria-label="Xem sản phẩm tiếp theo" :disabled="relatedOffset >= relatedMaxOffset" @click="moveRelated(1)"><i class="bi bi-arrow-right"></i></button>
          </div>
        </div>
        <div class="product-detail-v2__related-viewport">
          <div class="product-detail-v2__related-track" :style="relatedTrackStyle">
            <div v-for="item in relatedProducts" :key="item.id" class="product-detail-v2__related-item"><ProductCard :product="item" /></div>
          </div>
        </div>
      </section>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import ProductCard from '../components/ProductCard.vue'
import { normalizeProduct, slugifyProduct } from '../composables/useCatalog'
import { formatDate, formatPrice } from '../composables/useFormat'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useToastStore } from '../stores/toast'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast = useToastStore()
const loading = ref(false)
const product = ref(null)
const reviews = ref([])
const relatedProducts = ref([])
const relatedOffset = ref(0)
const relatedVisibleCount = 5
const quantity = ref(1)
const selectedImage = ref('')
const selectedColor = ref('')
const selectedSize = ref('')
const submittingReview = ref(false)
const reviewEligibility = ref({ canReview: false, purchased: false, reviewed: false })
const reviewForm = reactive({ rating: 5, comment: '', imageUrls: [], videoUrls: [] })
const descriptionExpanded = ref(false)
const productId = computed(() => product.value?.id)
const variants = computed(() => (product.value?.variants || []).filter((variant) => variant.status !== false))
const colors = computed(() => [...new Set(variants.value.map((variant) => variant.color).filter(Boolean))])
const sizes = computed(() => [...new Set(variants.value.map((variant) => variant.size).filter(Boolean))])
const selectedVariant = computed(() => variants.value.find((variant) => (!selectedColor.value || variant.color === selectedColor.value) && (!selectedSize.value || variant.size === selectedSize.value)) || variants.value[0] || null)
const basePrice = computed(() => Number(selectedVariant.value?.price || product.value?.price || 0))
const hasPromotion = computed(() => Number(product.value?.promotionDiscountPercent || 0) > 0)
const finalPrice = computed(() => hasPromotion.value ? Math.max(basePrice.value * (100 - Number(product.value.promotionDiscountPercent)) / 100, 0) : basePrice.value)
const categoryChips = computed(() => (product.value?.categoryNames || []).slice(0, 4))
const reviewCount = computed(() => Number(product.value?.reviewCount || reviews.value.length || 0))
const ratingAverageText = computed(() => Number(product.value?.ratingAverage || 0).toFixed(1))
const roundedRating = computed(() => Math.round(Number(product.value?.ratingAverage || 0)))
const stockAvailable = computed(() => selectedVariant.value?.stock ?? product.value?.stock ?? 0)
const reviewGateTitle = computed(() => !authStore.isAuthenticated ? 'Đăng nhập để đánh giá' : reviewEligibility.value.reviewed ? 'Bạn đã đánh giá sản phẩm này' : !reviewEligibility.value.purchased ? 'Chỉ khách đã nhận hàng mới được đánh giá' : 'Chưa thể đánh giá')
const reviewGateMessage = computed(() => !authStore.isAuthenticated ? 'Bạn cần đăng nhập; sản phẩm phải thuộc đơn đã giao và đã thanh toán.' : reviewEligibility.value.reviewed ? 'Mỗi tài khoản chỉ gửi một đánh giá cho mỗi sản phẩm.' : 'Bạn có thể đánh giá sau khi đơn hàng chứa sản phẩm này đã được giao và thanh toán thành công.')
const galleryImages = computed(() => {
  const images = [{ id: 'main', imageUrl: product.value?.imageUrl }, ...(product.value?.images || [])]
  return images.filter((item, index, list) => item.imageUrl && list.findIndex((entry) => entry.imageUrl === item.imageUrl) === index)
})
const mainImage = computed(() => selectedImage.value || galleryImages.value[0]?.imageUrl || '/favicon.svg')
const fullDescription = computed(() => product.value?.description || 'Sản phẩm hiện chưa có mô tả chi tiết từ backend.')
const hasLongDescription = computed(() => fullDescription.value.length > 180)
const descriptionPreview = computed(() => fullDescription.value.slice(0, 180).trim())
const relatedMaxOffset = computed(() => Math.max(relatedProducts.value.length - relatedVisibleCount, 0))
const relatedTrackStyle = computed(() => ({ transform: `translateX(calc(-${relatedOffset.value} * ((100% + 1rem) / ${relatedVisibleCount})))` }))

watch(product, () => {
  selectedImage.value = galleryImages.value[0]?.imageUrl || '/favicon.svg'
  selectedColor.value = colors.value[0] || ''
  selectedSize.value = sizes.value[0] || ''
  quantity.value = 1
})

onMounted(async () => {
  await fetchProduct()
  await Promise.all([fetchReviews(), fetchRelatedProducts(), fetchReviewEligibility()])
})

async function fetchProduct() {
  loading.value = true
  try {
    try {
      const { data } = await axios.get(`/api/products/slug/${encodeURIComponent(route.params.slug)}`)
      product.value = normalizeProduct(data)
    } catch {
      // Fallback keeps old backend processes compatible until they are restarted.
      const { data } = await axios.get('/api/products')
      const matched = (data || []).find((item) => slugifyProduct(item.name || item.productName) === route.params.slug)
      if (!matched) throw new Error('Product not found')
      product.value = normalizeProduct(matched)
    }
  } catch {
    toast.error('Không thể tải sản phẩm này.')
    router.push('/shop')
  } finally {
    loading.value = false
  }
}

async function fetchReviews() {
  if (!productId.value) return
  try { reviews.value = (await axios.get(`/api/products/${productId.value}/reviews`)).data || [] } catch { reviews.value = [] }
}

async function fetchRelatedProducts() {
  try {
    relatedOffset.value = 0
    const categoryId = product.value?.categoryId
    const { data } = await axios.get(categoryId ? '/api/products' : '/api/products/featured', categoryId ? { params: { categoryId } } : undefined)
    relatedProducts.value = (data || []).map(normalizeProduct).filter((item) => item.id !== productId.value)
  } catch { relatedProducts.value = [] }
}

function moveRelated(direction) {
  relatedOffset.value = Math.min(Math.max(relatedOffset.value + direction, 0), relatedMaxOffset.value)
}

async function fetchReviewEligibility() {
  if (!authStore.isAuthenticated || !productId.value) return
  try {
    const { data } = await axios.get(`/api/products/${productId.value}/review-eligibility`)
    reviewEligibility.value = { canReview: Boolean(data?.canReview), purchased: Boolean(data?.purchased), reviewed: Boolean(data?.reviewed) }
  } catch { reviewEligibility.value = { canReview: false, purchased: false, reviewed: false } }
}

async function submitReview() {
  if (!productId.value) return
  submittingReview.value = true
  try {
    await axios.post(`/api/products/${productId.value}/reviews`, { rating: reviewForm.rating, comment: reviewForm.comment, imageUrls: reviewForm.imageUrls, videoUrls: reviewForm.videoUrls })
    reviewForm.rating = 5
    reviewForm.comment = ''
    reviewForm.imageUrls = []
    reviewForm.videoUrls = []
    toast.success('Đã gửi đánh giá của bạn.')
    await Promise.all([fetchProduct(), fetchReviews(), fetchReviewEligibility()])
  } catch (error) { toast.error(error.response?.data?.message || 'Không thể gửi đánh giá.') } finally { submittingReview.value = false }
}

async function handleReviewMediaUpload(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  for (const file of files) {
    const isImage = file.type.startsWith('image/')
    const isVideo = file.type.startsWith('video/')
    if (!isImage && !isVideo) continue
    if (file.size > 12 * 1024 * 1024) {
      toast.warning(`${file.name} vượt quá 12MB.`)
      continue
    }
    if (isImage && reviewForm.imageUrls.length >= 5) {
      toast.warning('Bạn chỉ có thể thêm tối đa 5 ảnh.')
      continue
    }
    if (isVideo && reviewForm.videoUrls.length >= 2) {
      toast.warning('Bạn chỉ có thể thêm tối đa 2 video.')
      continue
    }
    const dataUrl = await readFileAsDataUrl(file)
    if (isImage) reviewForm.imageUrls.push(dataUrl)
    else reviewForm.videoUrls.push(dataUrl)
  }
}

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result || '')
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

function removeReviewMedia(field, index) {
  reviewForm[field].splice(index, 1)
}

function reviewMedia(review) {
  return [
    ...(review?.imageUrls || []).map((src) => ({ type: 'image', src })),
    ...(review?.videoUrls || []).map((src) => ({ type: 'video', src }))
  ].filter((item) => item.src)
}

function changeQty(step) {
  const next = quantity.value + step
  if (next >= 1 && next <= Math.max(Number(stockAvailable.value), 1)) quantity.value = next
}

function addToCart(goToCart) {
  if (!selectedVariant.value) return toast.warning('Sản phẩm hiện chưa có biến thể hoạt động.')
  cartStore.addItem(selectedVariant.value, quantity.value).then((success) => { if (success && goToCart) router.push('/cart') })
}

function reviewerInitials(review) {
  return (review?.userFullName || 'KH').split(/\s+/).filter(Boolean).slice(-2).map((part) => part[0]).join('').toUpperCase()
}
</script>

<style scoped>
.product-detail-v2 { max-width: 1240px; margin: 0 auto; padding: 1.5rem 1rem 5rem; color: #18181b; }
.product-detail-v2__loading { min-height: 280px; display: grid; place-items: center; gap: .7rem; }
.product-detail-v2__breadcrumb { display: flex; align-items: center; gap: .65rem; margin-bottom: 1.35rem; color: #71717a; font-size: .86rem; }
.product-detail-v2__breadcrumb a { color: inherit; text-decoration: none; font-weight: 700; }
.product-detail-v2__breadcrumb a:hover { color: #18181b; }
.product-detail-v2__hero { display: grid; grid-template-columns: minmax(0, 1.08fr) minmax(390px, .92fr); gap: clamp(1.5rem, 4vw, 4rem); align-items: start; }
.product-detail-v2__gallery { min-width: 0; }
.product-detail-v2__main-image { position: relative; aspect-ratio: 1 / 1.08; overflow: hidden; background: #f4f4f5; border-radius: 1.35rem; }
.product-detail-v2__main-image img { width: 100%; height: 100%; object-fit: cover; display: block; }
.product-detail-v2__sale { position: absolute; top: 1rem; left: 1rem; padding: .5rem .75rem; background: #18181b; color: #fff; border-radius: 999px; font-size: .76rem; font-weight: 800; }
.product-detail-v2__image-count { position: absolute; right: 1rem; bottom: 1rem; padding: .45rem .7rem; background: rgba(255,255,255,.9); border-radius: 999px; font-size: .76rem; font-weight: 800; }
.product-detail-v2__thumbs { display: flex; gap: .7rem; margin-top: .85rem; overflow-x: auto; }
.product-detail-v2__thumb { width: 72px; height: 82px; flex: 0 0 auto; padding: 0; overflow: hidden; border: 1px solid #e4e4e7; border-radius: .75rem; background: #fafafa; cursor: pointer; }
.product-detail-v2__thumb.is-active { border: 2px solid #18181b; }
.product-detail-v2__thumb img { width: 100%; height: 100%; object-fit: cover; }
.product-detail-v2__gallery-note { display: flex; gap: .55rem; align-items: center; margin-top: .9rem; color: #71717a; font-size: .78rem; }
.product-detail-v2__gallery-note i { color: #0891b2; }
.product-detail-v2__summary { padding: .35rem 0; }
.product-detail-v2__eyebrow-row, .product-detail-v2__option-head, .product-detail-v2__price-row, .product-detail-v2__buy-row { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.product-detail-v2__chips { display: flex; flex-wrap: wrap; gap: .4rem; }
.product-detail-v2__chips span { padding: .35rem .65rem; background: #e0f7fa; border-radius: 999px; color: #0e7490; font-size: .72rem; font-weight: 800; }
.product-detail-v2__rating { color: #71717a; font-size: .8rem; white-space: nowrap; }
.product-detail-v2__rating i, .product-detail-v2__stars .is-active, .product-detail-v2__review-stars .is-active { color: #f59e0b; }
.product-detail-v2__summary h1 { max-width: 640px; margin: 1.25rem 0 .7rem; font-size: clamp(2.25rem, 5vw, 4.5rem); line-height: .98; letter-spacing: -.065em; }
.product-detail-v2__intro { max-width: 560px; margin: 0 0 1.5rem; color: #71717a; line-height: 1.7; }
.product-detail-v2__price-row { padding: 1.15rem 0; border-top: 1px solid #e4e4e7; border-bottom: 1px solid #e4e4e7; }
.product-detail-v2__price-label { display: block; color: #71717a; font-size: .75rem; margin-bottom: .25rem; }
.product-detail-v2__price { display: flex; align-items: baseline; gap: .75rem; }
.product-detail-v2__price strong { font-size: 1.9rem; letter-spacing: -.04em; }
.product-detail-v2__price del { color: #a1a1aa; }
.product-detail-v2__saving { padding: .45rem .65rem; border: 1px solid #f0abfc; border-radius: .5rem; color: #c026d3; font-size: .75rem; font-weight: 800; }
.product-detail-v2__facts { display: grid; grid-template-columns: repeat(3, 1fr); gap: .55rem; margin: 1rem 0 1.45rem; }
.product-detail-v2__facts div { display: grid; gap: .2rem; padding: .75rem; background: #fafafa; border-radius: .7rem; font-size: .72rem; color: #71717a; }
.product-detail-v2__facts i { color: #0891b2; font-size: 1rem; }
.product-detail-v2__facts b { color: #18181b; }
.product-detail-v2__option { margin-top: 1.15rem; }
.product-detail-v2__option-head { margin-bottom: .6rem; font-size: .82rem; }.product-detail-v2__option-head span { color: #71717a; }.product-detail-v2__choices { display: flex; flex-wrap: wrap; gap: .5rem; }
.product-detail-v2__choices button { padding: .6rem .85rem; background: #fff; border: 1px solid #d4d4d8; border-radius: .55rem; color: #3f3f46; cursor: pointer; }.product-detail-v2__choices button.is-active { border-color: #18181b; background: #18181b; color: #fff; }
.product-detail-v2__buy-row { margin: 1.5rem 0 .75rem; color: #71717a; font-size: .78rem; }.product-detail-v2__quantity { display: flex; align-items: center; border: 1px solid #d4d4d8; border-radius: .6rem; overflow: hidden; }.product-detail-v2__quantity button { width: 34px; height: 34px; border: 0; background: #fff; font-size: 1.2rem; cursor: pointer; }.product-detail-v2__quantity strong { min-width: 30px; text-align: center; }
.product-detail-v2__actions { display: grid; grid-template-columns: 1fr .75fr; gap: .65rem; }.product-detail-v2__actions button { min-height: 50px; }.product-detail-v2__buy-now { border: 1px solid #18181b; background: #fff; color: #18181b; font-weight: 800; }.product-detail-v2__buy-now:hover { background: #18181b; color: #fff; }
.product-detail-v2__shipping { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1.35rem; padding-top: 1rem; border-top: 1px solid #e4e4e7; }.product-detail-v2__shipping div { display: flex; gap: .6rem; }.product-detail-v2__shipping i { color: #0891b2; }.product-detail-v2__shipping span { display: grid; gap: .2rem; font-size: .75rem; }.product-detail-v2__shipping small { color: #71717a; }
.product-detail-v2__below { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 1.5rem; margin-top: 4rem; align-items: start; }.product-detail-v2__content { padding: clamp(1.25rem, 3vw, 2.5rem); }.product-detail-v2__section-head span, .product-detail-v2__review-head span, .product-detail-v2__side-note span { color: #0891b2; text-transform: uppercase; letter-spacing: .12em; font-size: .7rem; font-weight: 900; }.product-detail-v2__section-head h2, .product-detail-v2__review-head h2 { margin: .45rem 0 0; font-size: 1.7rem; letter-spacing: -.04em; }.product-detail-v2__description { max-width: 760px; margin: 1.2rem 0; color: #52525b; line-height: 1.8; }.product-detail-v2__bullets { display: grid; grid-template-columns: repeat(3, 1fr); gap: .75rem; padding-bottom: 2rem; border-bottom: 1px solid #e4e4e7; }.product-detail-v2__bullets div { display: flex; gap: .45rem; color: #52525b; font-size: .8rem; line-height: 1.5; }.product-detail-v2__bullets i { color: #0891b2; }
.product-detail-v2__reviews { padding-top: 2.3rem; }.product-detail-v2__review-head { display: flex; justify-content: space-between; gap: 1rem; }.product-detail-v2__review-score { display: grid; justify-items: end; gap: .2rem; }.product-detail-v2__review-score strong { font-size: 1.4rem; }.product-detail-v2__review-score small { color: #71717a; }.product-detail-v2__stars { display: flex; gap: .15rem; color: #d4d4d8; }.product-detail-v2__stars--small { margin-top: .45rem; font-size: .75rem; }.product-detail-v2__review-form { display: grid; gap: .75rem; margin: 1.5rem 0; padding: 1rem; background: #fafafa; border-radius: .85rem; }.product-detail-v2__review-stars { display: flex; align-items: center; gap: .35rem; font-size: .78rem; }.product-detail-v2__review-stars span { margin-right: .4rem; }.product-detail-v2__review-stars button { padding: 0; border: 0; background: transparent; color: #d4d4d8; cursor: pointer; }.product-detail-v2__review-stars button.is-active { color: #f59e0b; }.product-detail-v2__review-gate { display: flex; gap: .75rem; margin: 1.5rem 0; padding: 1rem; background: #f4f4f5; border-radius: .75rem; }.product-detail-v2__review-gate i { color: #0891b2; }.product-detail-v2__review-gate p { margin: .25rem 0 0; color: #71717a; font-size: .82rem; }.product-detail-v2__review-list { display: grid; gap: .75rem; }.product-detail-v2__review-card { padding: 1rem 0; border-top: 1px solid #e4e4e7; }.product-detail-v2__review-author { display: flex; align-items: center; gap: .6rem; }.product-detail-v2__review-author div { display: grid; gap: .15rem; }.product-detail-v2__review-author small { color: #71717a; }.product-detail-v2__avatar { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 50%; background: #e0f7fa; color: #0e7490; font-size: .72rem; font-weight: 900; }.product-detail-v2__review-card p, .product-detail-v2__empty-review { margin: .65rem 0 0; color: #52525b; font-size: .88rem; line-height: 1.6; }
.product-detail-v2__review-upload { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: .15rem .65rem; align-items: center; padding: .85rem; border: 1px dashed #0891b2; border-radius: .8rem; background: #f0f9ff; cursor: pointer; }
.product-detail-v2__review-upload i { grid-row: span 2; color: #0891b2; font-size: 1.25rem; }
.product-detail-v2__review-upload span { color: #18181b; font-size: .86rem; font-weight: 900; }
.product-detail-v2__review-upload small { color: #71717a; font-size: .74rem; }
.product-detail-v2__review-upload input { display: none; }
.product-detail-v2__review-media-preview, .product-detail-v2__review-media-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: .55rem; }
.product-detail-v2__review-media-preview button { position: relative; padding: 0; overflow: hidden; border: 1px solid #e2e8f0; border-radius: .65rem; background: #e5e7eb; aspect-ratio: 1; cursor: pointer; }
.product-detail-v2__review-media-preview img, .product-detail-v2__review-media-preview video, .product-detail-v2__review-media-grid img, .product-detail-v2__review-media-grid video { width: 100%; height: 100%; object-fit: cover; border-radius: .65rem; background: #f4f4f5; }
.product-detail-v2__review-media-preview i { position: absolute; top: .35rem; right: .35rem; color: #fff; filter: drop-shadow(0 1px 4px rgba(0,0,0,.45)); }
.product-detail-v2__review-media-grid { margin-top: .75rem; }
.product-detail-v2__review-media-grid img, .product-detail-v2__review-media-grid video { aspect-ratio: 1; border: 1px solid #e4e4e7; }
.product-detail-v2__side-note { position: sticky; top: 1.5rem; padding: 1.4rem; border-radius: 1rem; background: #ecfeff; }.product-detail-v2__side-note h3 { margin: .55rem 0; font-size: 1.35rem; letter-spacing: -.04em; }.product-detail-v2__side-note p { color: #52525b; font-size: .86rem; line-height: 1.65; }.product-detail-v2__related { margin-top: 4rem; }.product-detail-v2__related-head { display: flex; align-items: end; justify-content: space-between; gap: 1rem; }.product-detail-v2__related-controls { display: flex; gap: .55rem; padding-bottom: .15rem; }.product-detail-v2__related-controls button { width: 44px; height: 44px; border: 1px solid #d4d4d8; border-radius: 50%; background: #fff; color: #18181b; cursor: pointer; transition: .2s ease; }.product-detail-v2__related-controls button:hover:not(:disabled) { border-color: #0891b2; color: #0891b2; transform: translateY(-2px); }.product-detail-v2__related-controls button:disabled { opacity: .35; cursor: not-allowed; }.product-detail-v2__related-viewport { overflow: hidden; margin-top: 1.2rem; padding: .1rem .1rem .5rem; }.product-detail-v2__related-track { display: flex; gap: 1rem; transition: transform .3s ease; will-change: transform; }.product-detail-v2__related-item { flex: 0 0 calc((100% - 4rem) / 5); min-width: 0; }.product-detail-v2__related-item .product-card { height: 100%; }
@media (max-width: 900px) { .product-detail-v2__hero, .product-detail-v2__below { grid-template-columns: 1fr; }.product-detail-v2__side-note { position: static; }.product-detail-v2__summary h1 { font-size: clamp(2.3rem, 10vw, 4rem); } }
@media (max-width: 900px) { .product-detail-v2__related-item { flex-basis: calc((100% - 1rem) / 2); } }
@media (max-width: 560px) { .product-detail-v2 { padding-inline: .85rem; }.product-detail-v2__facts, .product-detail-v2__bullets, .product-detail-v2__shipping { grid-template-columns: 1fr; }.product-detail-v2__actions { grid-template-columns: 1fr; }.product-detail-v2__review-head { align-items: flex-start; flex-direction: column; }.product-detail-v2__review-score { justify-items: start; }.product-detail-v2__related-head { align-items: flex-start; }.product-detail-v2__related-item { flex-basis: 82%; } }
.product-detail-v2__description-wrap { max-width: 780px; }
.product-detail-v2__description { margin-bottom: .35rem; }
.product-detail-v2__more { display: inline-flex; align-items: center; gap: .35rem; padding: 0; border: 0; background: transparent; color: #0891b2; font-size: .8rem; font-weight: 800; cursor: pointer; }
.product-detail-v2__more:hover { color: #c026d3; }
.product-detail-v2__experience, .product-detail-v2__guide, .product-detail-v2__faq { margin-top: 4rem; }
.product-detail-v2__experience-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: .8rem; margin-top: 1.25rem; }
.product-detail-v2__experience-grid article { min-height: 145px; padding: 1.2rem; border: 1px solid #e4e4e7; border-radius: 1rem; background: #fff; transition: transform .2s ease, box-shadow .2s ease; }
.product-detail-v2__experience-grid article:hover { transform: translateY(-5px); box-shadow: 5px 5px 0 #a5f3fc; }
.product-detail-v2__experience-grid i { display: block; margin-bottom: 1.4rem; color: #0891b2; font-size: 1.3rem; }
.product-detail-v2__experience-grid b { display: block; font-family: var(--font-display); }
.product-detail-v2__experience-grid p { margin: .45rem 0 0; color: #71717a; font-size: .77rem; line-height: 1.5; }
.product-detail-v2__guide { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.product-detail-v2__guide-card { min-height: 220px; padding: clamp(1.3rem, 4vw, 2.3rem); border-radius: 1.2rem; }
.product-detail-v2__guide-card--pink { background: linear-gradient(135deg, #fdf2f8, #f5d0fe); }
.product-detail-v2__guide-card--cyan { background: linear-gradient(135deg, #cffafe, #e0f2fe); }
.product-detail-v2__guide-card h3 { margin: .65rem 0; font-family: var(--font-display); font-size: 1.8rem; letter-spacing: -.06em; }
.product-detail-v2__guide-card p { max-width: 440px; color: #52525b; line-height: 1.65; }
.product-detail-v2__text-link { color: #18181b; font-size: .8rem; font-weight: 800; text-decoration: none; }
.product-detail-v2__size-line { display: flex; justify-content: space-between; gap: 1rem; padding: .65rem 0; border-bottom: 1px solid rgba(24,24,27,.15); font-size: .8rem; }
.product-detail-v2__size-line span, .product-detail-v2__guide-card small { color: #52525b; }
.product-detail-v2__guide-card small { display: block; margin-top: .85rem; line-height: 1.5; }
.product-detail-v2__faq { padding: clamp(1.25rem, 3vw, 2.2rem); }
.product-detail-v2__faq-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: .75rem; margin-top: 1.3rem; }
.product-detail-v2__faq details { padding: 1rem; border: 1px solid #e4e4e7; border-radius: .75rem; background: #fafafa; }
.product-detail-v2__faq summary { cursor: pointer; font-size: .8rem; font-weight: 800; }
.product-detail-v2__faq p { margin: .7rem 0 0; color: #71717a; font-size: .78rem; line-height: 1.55; }
@media (max-width: 900px) { .product-detail-v2__experience-grid, .product-detail-v2__faq-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .product-detail-v2__guide, .product-detail-v2__experience-grid, .product-detail-v2__faq-grid { grid-template-columns: 1fr; } }
</style>
