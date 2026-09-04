<template>
  <div class="admin-section reviews-page">
    <div class="admin-section-head">
      <div>
        <span class="admin-kicker">Customer voice</span>
        <h4 class="admin-page-title mb-0">Đánh giá khách hàng</h4>
        <p>Đọc phản hồi thật, theo dõi chất lượng sản phẩm và kiểm duyệt nội dung nghi vấn từ AI.</p>
      </div>
    </div>

    <!-- Tổng quan thống kê & Thẻ bấm để lọc nhanh danh sách nghi vấn -->
    <div class="reviews-overview">
      <div class="review-summary-card review-summary-card--primary" @click="setFilter('ALL')" style="cursor: pointer;" title="Xem tất cả">
        <span class="review-summary-icon"><i class="bi bi-chat-heart"></i></span>
        <div><span class="review-summary-label">Tổng đánh giá</span><strong>{{ reviews.length }}</strong><small>phản hồi từ khách hàng</small></div>
      </div>
      <div class="review-summary-card">
        <span class="review-summary-icon review-summary-icon--yellow"><i class="bi bi-star-fill"></i></span>
        <div><span class="review-summary-label">Điểm trung bình</span><strong>{{ averageRating }}<em>/5</em></strong><small class="review-stars">{{ starText(Math.round(Number(averageRating))) }}</small></div>
      </div>
      <!-- Bấm vào thẻ này để lọc nhanh các bài bị FLAGGED -->
      <div class="review-summary-card clickable-card" :class="{ 'border-danger bg-light-danger active-filter': statusFilter === 'FLAGGED', 'border-danger': flaggedCount > 0 }" @click="setFilter('FLAGGED')" title="Bấm để lọc các đánh giá cần kiểm duyệt">
        <span class="review-summary-icon review-summary-icon--red"><i class="bi bi-exclamation-triangle-fill"></i></span>
        <div>
          <span class="review-summary-label">Cần kiểm duyệt <i class="bi bi-funnel-fill text-danger ms-1"></i></span>
          <strong class="text-danger">{{ flaggedCount }}</strong>
          <small>bình luận nghi vấn vi phạm</small>
        </div>
      </div>
      <div class="review-distribution-card">
        <div class="review-distribution-head"><strong>Phân bố đánh giá</strong><span>{{ reviewedProductsCount }} sản phẩm đã phản hồi</span></div>
        <div v-for="item in ratingDistribution" :key="item.star" class="review-distribution-row">
          <span>{{ item.star }} <i class="bi bi-star-fill"></i></span><div class="review-progress"><span :style="{ width: `${item.percent}%` }"></span></div><b>{{ item.count }}</b>
        </div>
      </div>
    </div>

    <div class="admin-toolbar reviews-toolbar">
      <div class="reviews-search"><i class="bi bi-search"></i><input v-model="search" type="text" placeholder="Tìm người dùng, sản phẩm, bình luận..."></div>
      <div class="d-flex gap-2">
        <!-- Bộ lọc trạng thái hiển thị -->
        <select v-model="statusFilter" class="form-select form-select-sm reviews-rating-filter">
          <option value="ALL">Tất cả trạng thái</option>
          <option value="FLAGGED">⚠️ Cần kiểm duyệt (Flagged)</option>
          <option value="VISIBLE">Đang công khai (Visible)</option>
          <option value="HIDDEN">Đã ẩn (Hidden)</option>
        </select>
        <select v-model.number="ratingFilter" class="form-select form-select-sm reviews-rating-filter">
          <option :value="0">Tất cả sao</option>
          <option v-for="star in [5,4,3,2,1]" :key="star" :value="star">{{ star }} sao</option>
        </select>
      </div>
    </div>

    <div class="admin-list-card reviews-list-card">
      <div class="admin-list-header">
        <div><strong>Danh sách phản hồi</strong><span>{{ filteredReviews.length }} kết quả phù hợp</span></div>
        <span class="reviews-list-hint"><i class="bi bi-shield-check"></i> Kiểm duyệt an toàn tự động bởi AI</span>
      </div>
      <div class="admin-entity-list">
        <article v-for="item in filteredReviews" :key="item.id" class="review-item-card">
          <div class="review-item-avatar">
            <img v-if="item.userAvatarUrl" :src="item.userAvatarUrl" :alt="item.userFullName || 'Avatar người dùng'">
            <span v-else>{{ initials(item.userFullName || `User ${item.userId || ''}`) }}</span>
          </div>
          <div class="review-item-content">
            <div class="review-item-topline">
              <div>
                <strong>{{ item.userFullName || `User #${item.userId || '-'}` }}</strong>
                <span class="review-verified"><i class="bi bi-check-circle-fill"></i> Khách hàng</span>

                <span v-if="item.status === 'FLAGGED'" class="badge bg-danger ms-2">⚠️ Nghi vấn vi phạm</span>
                <span v-else-if="item.status === 'VISIBLE'" class="badge bg-success ms-2">Công khai</span>
                <span v-else class="badge bg-secondary ms-2">Đã ẩn</span>
              </div>
              <time>{{ formatDateLabel(item.createdAt) || 'Chưa có ngày' }}</time>
            </div>


            <div v-if="item.aiReason" class="small mt-1 mb-1" :class="item.status === 'VISIBLE' || item.status === 'SAFE' ? 'text-success' : 'text-danger'">
              <i class="bi" :class="item.status === 'VISIBLE' || item.status === 'SAFE' ? 'bi-shield-check' : 'bi-robot'"></i>
              <b>{{ item.status === 'VISIBLE' || item.status === 'SAFE' ? 'Đã kiểm duyệt AI:' : 'AI cảnh báo:' }}</b>
              {{ item.aiReason }}
            </div>

            <div class="review-rating-line"><span class="review-stars">{{ starText(item.rating) }}</span><b>{{ item.rating }}/5</b></div>
            <p class="review-comment">{{ item.comment || 'Khách hàng chưa để lại nội dung.' }}</p>

            <div v-if="item.imageUrls && item.imageUrls.length" class="admin-review-media-grid mt-2">
              <img v-for="(img, idx) in item.imageUrls" :key="`img-${idx}`" :src="img" alt="Ảnh đánh giá" class="admin-review-thumb" />
            </div>
            <div v-if="item.videoUrls && item.videoUrls.length" class="admin-review-media-grid mt-2">
              <video v-for="(vid, idx) in item.videoUrls" :key="`vid-${idx}`" :src="vid" controls class="admin-review-thumb"></video>
            </div>

            <div class="review-product-line mt-2"><i class="bi bi-bag"></i><span>Sản phẩm được đánh giá</span><strong>{{ item.productName || `Sản phẩm #${item.productId || '-'}` }}</strong></div>
          </div>

          <div class="review-item-actions">
            <button v-if="item.status !== 'VISIBLE'" class="review-action-btn review-action-btn--success" title="Duyệt / Hiện bình luận" @click="updateStatus(item, 'VISIBLE')">
              <i class="bi bi-check-lg"></i><span>Hiện</span>
            </button>
            <button v-if="item.status !== 'HIDDEN'" class="review-action-btn review-action-btn--warning" title="Ẩn bình luận" @click="updateStatus(item, 'HIDDEN')">
              <i class="bi bi-eye-slash"></i><span>Ẩn</span>
            </button>
          </div>
        </article>

        <div v-if="!filteredReviews.length" class="admin-empty-state">
          <i class="bi bi-chat-square-text"></i>
          <strong>Không có đánh giá phù hợp</strong>
          <span>Thử đổi từ khóa hoặc bộ lọc trạng thái.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useToastStore } from '../../stores/toast'

const toast = useToastStore()
const reviews = ref([])
const search = ref('')
const ratingFilter = ref(0)
const statusFilter = ref('ALL') // Bộ lọc trạng thái: ALL, FLAGGED, VISIBLE, HIDDEN

const averageRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  const total = reviews.value.reduce((sum, item) => sum + Number(item.rating || 0), 0)
  return (total / reviews.value.length).toFixed(1)
})
const fiveStarCount = computed(() => reviews.value.filter((item) => Number(item.rating) === 5).length)
const reviewedProductsCount = computed(() => new Set(reviews.value.map((item) => item.productId).filter(Boolean)).size)

const flaggedCount = computed(() => reviews.value.filter((item) => item.status === 'FLAGGED').length)

const ratingDistribution = computed(() => {
  const total = reviews.value.length
  return [5, 4, 3, 2, 1].map((star) => {
    const count = reviews.value.filter((item) => Number(item.rating) === star).length
    return { star, count, percent: total ? Math.round((count / total) * 100) : 0 }
  })
})

const filteredReviews = computed(() => {
  const q = search.value.trim().toLowerCase()
  return reviews.value.filter((item) => {
    const matchSearch = !q ||
        item.userFullName?.toLowerCase().includes(q) ||
        item.productName?.toLowerCase().includes(q) ||
        item.comment?.toLowerCase().includes(q)
    const matchRating = !ratingFilter.value || Number(item.rating) === ratingFilter.value
    const matchStatus = statusFilter.value === 'ALL' || item.status === statusFilter.value
    return matchSearch && matchRating && matchStatus
  })
})

function setFilter(status) {
  statusFilter.value = status
}

function formatDateLabel(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}

function starText(value) {
  const count = Math.max(1, Math.min(5, Number(value || 0)))
  return '★'.repeat(count) + '☆'.repeat(5 - count)
}

function initials(value) {
  return String(value || '?').trim().split(/\s+/).slice(-2).map((part) => part.charAt(0)).join('').toUpperCase() || '?'
}

onMounted(() => {
  fetchReviews()
})

async function fetchReviews() {
  try {
    const { data } = await axios.get('/api/admin/reviews')
    reviews.value = data
  } catch (error) {
    toast.error('Không thể tải danh sách đánh giá.')
  }
}

async function updateStatus(item, newStatus) {
  try {
    await axios.put(`/api/admin/reviews/${item.id}`, {
      ...item,
      status: newStatus
    })
    toast.success(newStatus === 'VISIBLE' ? 'Đã duyệt hiển thị bình luận!' : 'Đã ẩn bình luận thành công!')
    fetchReviews()
  } catch (error) {
    toast.error('Không thể cập nhật trạng thái đánh giá!')
  }
}

defineExpose({ refresh: fetchReviews })
</script>

<style scoped>
.reviews-page { --review-ink:#17202b; --review-muted:#7d8795; --review-line:#e6ebf2; --review-cyan:#06a9c8; }
.reviews-overview { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:14px; margin:20px 0; }
.review-summary-card,.review-distribution-card { min-height:116px; padding:18px; border:1px solid var(--review-line); border-radius:18px; background:#fff; box-shadow:0 10px 24px rgba(17,24,39,.045); transition: all 0.2s ease; }
.review-summary-card:hover { transform: translateY(-2px); box-shadow: 0 14px 28px rgba(17,24,39,.08); }
.review-summary-card.active-filter { border-color: #dc2626 !important; background: #fff5f5 !important; }
.review-summary-card { display:flex; align-items:center; gap:13px; }
.review-summary-card--primary { background:linear-gradient(135deg,#eefcff,#fff); border-color:#bfeef5; }
.review-summary-icon { display:grid; place-items:center; width:44px; height:44px; flex:0 0 44px; border-radius:14px; background:#dff8fc; color:#0797b5; font-size:19px; }
.review-summary-icon--yellow { background:#fff5cf; color:#d89500; }
.review-summary-icon--red { background:#fee2e2; color:#dc2626; }
.review-summary-label,.review-summary-card small { display:block; color:var(--review-muted); font-size:12px; font-weight:700; }
.review-summary-card strong { display:block; color:var(--review-ink); font-size:28px; line-height:1.1; margin:4px 0; }
.review-summary-card strong em { font-style:normal; color:#98a1af; font-size:13px; font-weight:700; }
.review-stars { color:#f5a900; letter-spacing:1px; }

.review-distribution-head { display:flex; justify-content:space-between; align-items:center; gap:10px; margin-bottom:8px; }
.review-distribution-head strong { color:var(--review-ink); }
.review-distribution-head span { color:var(--review-muted); font-size:11px; }
.review-distribution-row { display:grid; grid-template-columns:32px 1fr 24px; gap:8px; align-items:center; height:16px; color:#697383; font-size:11px; }
.review-distribution-row i { color:#f5a900; font-size:10px; }
.review-distribution-row b { text-align:right; font-size:11px; }
.review-progress { height:6px; overflow:hidden; border-radius:999px; background:#edf1f5; }
.review-progress span { display:block; height:100%; border-radius:inherit; background:linear-gradient(90deg,#ffb615,#ffdd65); }

.reviews-toolbar { justify-content:space-between; gap:12px; padding:12px 14px; border:1px solid var(--review-line); border-radius:16px; background:#fbfcff; }
.reviews-search { display:flex; align-items:center; gap:9px; width:min(400px,100%); padding:0 12px; border:1px solid #dce3ec; border-radius:11px; background:#fff; }
.reviews-search i { color:#8e99a8; }
.reviews-search input { width:100%; height:38px; border:0; outline:0; color:var(--review-ink); background:transparent; }
.reviews-rating-filter { width:170px; border-radius:11px; }
.reviews-list-card { margin-top:16px; overflow:hidden; }
.reviews-list-hint { color:#7f8997; font-size:12px; }
.reviews-list-hint i { color:#0aab79; margin-right:5px; }

.review-item-card { display:grid; grid-template-columns:48px minmax(0,1fr) auto; gap:15px; align-items:start; padding:20px 22px; border-bottom:1px solid var(--review-line); transition:background .2s ease; }
.review-item-card:last-child { border-bottom:0; }
.review-item-card:hover { background:linear-gradient(90deg,#fbfdff,#f8fdff); }
.review-item-avatar { width:48px; height:48px; overflow:hidden; display:grid; place-items:center; border:3px solid #d8f6fb; border-radius:50%; background:#eaf9fb; color:#078fae; font-weight:800; }
.review-item-avatar img { width:100%; height:100%; object-fit:cover; }
.review-item-content { min-width:0; }
.review-item-topline { display:flex; justify-content:space-between; gap:18px; }
.review-item-topline strong { color:var(--review-ink); font-size:15px; }
.review-item-topline time { color:#97a1af; white-space:nowrap; font-size:12px; }
.review-verified { margin-left:9px; color:#0c9d6d; font-size:11px; font-weight:700; }
.review-rating-line { display:flex; align-items:center; gap:8px; margin:5px 0 8px; }
.review-rating-line b { color:#7d8795; font-size:12px; }
.review-comment { margin:0 0 10px; color:#536172; font-size:13px; line-height:1.55; }

.admin-review-media-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.admin-review-thumb { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; border: 1px solid #cbd5e1; background: #f8fafc; }

.review-product-line { display:flex; align-items:center; flex-wrap:wrap; gap:6px; color:#8b95a2; font-size:11px; }
.review-product-line i { color:#079fbd; }
.review-product-line strong { color:#287089; font-size:12px; }
.review-item-actions { display:flex; gap:8px; align-self:center; }
.review-action-btn { display:inline-flex; align-items:center; gap:6px; min-height:34px; padding:0 12px; border:1px solid #cbd5e1; border-radius:10px; background:#fff; color:#263342; font-size:12px; font-weight:800; }
.review-action-btn:hover { border-color:#079fbd; color:#078ca8; }
.review-action-btn--success:hover { border-color:#10b981; color:#059669; }
.review-action-btn--warning:hover { border-color:#f59e0b; color:#d97706; }
.reviews-page .admin-empty-state { padding:55px 20px; }

@media (max-width:1100px) { .reviews-overview { grid-template-columns:repeat(2,minmax(0,1fr)); }.review-distribution-card { grid-column:span 2; } }
@media (max-width:680px) { .reviews-overview { grid-template-columns:1fr; }.review-distribution-card { grid-column:auto; }.reviews-toolbar { align-items:stretch; flex-direction:column; }.reviews-search,.reviews-rating-filter { width:100%; }.review-item-card { grid-template-columns:40px minmax(0,1fr); padding:16px 14px; }.review-item-avatar { width:40px; height:40px; }.review-item-actions { grid-column:2; justify-content:flex-start; }.review-item-topline { display:block; }.review-item-topline time { display:block; margin-top:4px; } }
</style>