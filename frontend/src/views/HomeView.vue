<template>
  <div class="section-shell home-page">
    <div class="y2k-page-container">
      <div class="home-layout">
        <section class="home-main">
          <div v-if="bannerSlides.length" id="homeCarousel" class="carousel slide home-hero-card" data-bs-ride="carousel" data-bs-interval="4800">
            <div class="carousel-inner">
              <div
                v-for="(banner, index) in bannerSlides"
                :key="banner.id || index"
                class="carousel-item"
                :class="{ active: index === 0 }"
              >
                <router-link :to="banner.linkUrl || '/shop'" class="d-block text-decoration-none">
                  <img :src="banner.imageUrl || '/y2k_banner.png'" class="d-block w-100" :alt="banner.title || 'Y2K Store banner'">
                </router-link>
                <div class="home-hero-card__overlay">
                  <div>
                    <h1>{{ banner.title || 'Y2K VIBES' }}</h1>
                    <p>{{ banner.subtitle || 'Streetwear nổi bật, dễ phối, lên outfit nhanh mỗi ngày.' }}</p>
                  </div>
                  <router-link :to="banner.linkUrl || '/shop'" class="btn btn-y2k-primary">{{ banner.buttonText || 'Mua ngay' }}</router-link>
                </div>
              </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#homeCarousel" data-bs-slide="prev">
              <span class="carousel-control-prev-icon"></span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#homeCarousel" data-bs-slide="next">
              <span class="carousel-control-next-icon"></span>
            </button>
          </div>

          <div class="home-service-strip">
            <div class="home-service-item">
              <i class="bi bi-truck"></i>
              <div>
                <strong>Free ship</strong>
                <span>Đơn từ 499k</span>
              </div>
            </div>
            <div class="home-service-item">
              <i class="bi bi-arrow-left-right"></i>
              <div>
                <strong>Đổi trả 7 ngày</strong>
                <span>Hỗ trợ đổi size</span>
              </div>
            </div>
            <div class="home-service-item">
              <i class="bi bi-patch-check"></i>
              <div>
                <strong>Hàng chọn lọc</strong>
                <span>Kiểm tra trước khi gửi</span>
              </div>
            </div>
            <div class="home-service-item">
              <i class="bi bi-headset"></i>
              <div>
                <strong>Tư vấn nhanh</strong>
                <span>Inbox là có outfit</span>
              </div>
            </div>
          </div>

          <section class="home-section">
            <div class="home-section__heading">
              <div>
                <h2>Bán chạy nhất</h2>
                <p>Những item được mua nhiều nhất từ các đơn không bị hủy.</p>
              </div>
              <router-link to="/shop?sort=best-selling" class="home-section__all-link">Xem tất cả <i class="bi bi-arrow-up-right"></i></router-link>
            </div>
            <div v-if="loadingFeatured" class="surface-card">Đang tải sản phẩm...</div>
            <div v-else-if="featuredProducts.length === 0" class="empty-state">
              <h4>Chưa có sản phẩm nổi bật</h4>
              <p class="muted-copy">Khi backend trả dữ liệu, các item nổi bật sẽ xuất hiện tại đây.</p>
              <router-link to="/shop" class="btn btn-y2k-outline">Mở cửa hàng</router-link>
            </div>
            <div v-else class="product-grid product-grid--home">
              <ProductCard v-for="product in featuredProducts.slice(0, 10)" :key="product.id" :product="product" />
            </div>
          </section>

          <section class="home-section home-featured-section home-featured-section--rated">
            <div class="home-section__heading">
              <div>
                <span class="section-heading__eyebrow">Community picks</span>
                <h2>Được đánh giá cao</h2>
                <p>Những item có điểm đánh giá cao và nhiều lượt phản hồi nhất.</p>
              </div>
              <router-link to="/shop?sort=rating" class="home-section__all-link">Khám phá thêm <i class="bi bi-arrow-up-right"></i></router-link>
            </div>
            <div v-if="loadingTopRated" class="surface-card">Đang tải sản phẩm...</div>
            <div v-else-if="topRatedProducts.length === 0" class="empty-state">
              <h4>Chưa có đánh giá nổi bật</h4>
              <p class="muted-copy">Các sản phẩm được đánh giá cao sẽ xuất hiện tại đây.</p>
              <router-link to="/shop" class="btn btn-y2k-outline">Mở cửa hàng</router-link>
            </div>
            <div v-else class="product-grid product-grid--home">
              <ProductCard v-for="product in topRatedProducts.slice(0, 10)" :key="product.id" :product="product" />
            </div>
          </section>

          <section v-for="(row, rowIndex) in categoryRows" :key="row.id" class="home-section home-category-row" :class="{ 'home-category-row--reverse': rowIndex % 2 === 1, [`home-category-row--${(rowIndex % 4) + 1}`]: true }">
            <div class="home-category-row__intro">
              <div class="home-category-row__number">0{{ rowIndex + 1 }}</div>
              <span class="section-heading__eyebrow">Shop by category</span>
              <h2>{{ row.name }}</h2>
              <p>{{ row.products.length }} item được chọn riêng cho vibe này.</p>
              <router-link :to="`/shop?category=${row.id}`" class="home-category-row__link">Khám phá danh mục <i class="bi bi-arrow-up-right"></i></router-link>
            </div>
            <div class="home-category-row__products">
              <div class="home-rail-controls">
                <span>{{ row.products.length }} sản phẩm</span>
                <button type="button" aria-label="Xem sản phẩm trước" @click="scrollRail(rowIndex, -1)"><i class="bi bi-arrow-left"></i></button>
                <button type="button" aria-label="Xem sản phẩm tiếp" @click="scrollRail(rowIndex, 1)"><i class="bi bi-arrow-right"></i></button>
              </div>
              <div :ref="(element) => setRailRef(element, rowIndex)" class="home-product-rail">
                <ProductCard v-for="product in row.products" :key="product.id" :product="product" />
              </div>
            </div>
          </section>

          <section v-if="generalCategories.length" class="home-section home-category-showcase">
            <div class="home-section__heading">
              <div>
                <span class="section-heading__eyebrow">Khám phá theo phong cách</span>
                <h2>Chọn vibe của bạn</h2>
                <p>Từ basic hằng ngày đến những item nổi bật cho outfit cuối tuần.</p>
              </div>
              <router-link to="/shop" class="home-section__all-link">Mở toàn bộ shop <i class="bi bi-arrow-up-right"></i></router-link>
            </div>
            <div class="home-category-grid">
              <router-link
                v-for="(category, index) in generalCategories.slice(0, 8)"
                :key="category.id"
                :to="`/shop?category=${category.id}`"
                class="home-category-tile"
                :class="`home-category-tile--${(index % 4) + 1}`"
              >
                <span class="home-category-tile__number">0{{ index + 1 }}</span>
                <i class="bi bi-arrow-up-right"></i>
                <strong>{{ category.name }}</strong>
                <small>{{ category.productCount || 'Nhiều' }} sản phẩm để chọn</small>
              </router-link>
            </div>
          </section>

          <section class="home-editorial-grid">
            <article class="home-editorial-card home-editorial-card--dark">
              <span class="section-heading__eyebrow">Y2K editorial / 01</span>
              <h2>Đẹp theo cách của bạn.</h2>
              <p>Những phom dáng dễ mặc, màu sắc có điểm nhấn và cảm hứng 2000s được chọn lọc cho tủ đồ hiện đại.</p>
              <router-link to="/shop" class="btn btn-y2k-primary">Khám phá bộ sưu tập</router-link>
              <span class="home-editorial-card__orb" aria-hidden="true"></span>
            </article>
            <article class="home-editorial-card home-editorial-card--light">
              <div class="home-editorial-card__topline">
                <span class="section-heading__eyebrow">Style guide / 02</span>
                <i class="bi bi-stars" aria-hidden="true"></i>
              </div>
              <h3>Mix & match trong 2 phút</h3>
              <div class="home-style-steps">
                <div><strong>01</strong><span>Chọn vibe</span></div>
                <div><strong>02</strong><span>Chọn item</span></div>
                <div><strong>03</strong><span>Lên outfit</span></div>
              </div>
              <router-link to="/shop" class="home-text-link">Bắt đầu phối đồ <i class="bi bi-arrow-right"></i></router-link>
            </article>
          </section>

          <section class="home-benefit-section">
            <div class="home-section__heading">
              <div>
                <span class="section-heading__eyebrow">Shopping, made easy</span>
                <h2>Mua sắm nhẹ nhàng hơn</h2>
              </div>
            </div>
            <div class="home-benefit-grid">
              <article><i class="bi bi-box-seam"></i><strong>Đóng gói chỉn chu</strong><p>Kiểm tra kỹ trước khi đơn hàng rời shop.</p></article>
              <article><i class="bi bi-arrow-repeat"></i><strong>Đổi size dễ dàng</strong><p>Hỗ trợ đổi trả trong 7 ngày đầu tiên.</p></article>
              <article><i class="bi bi-chat-heart"></i><strong>Tư vấn có gu</strong><p>Nhắn tin để nhận gợi ý outfit đúng vibe.</p></article>
              <article><i class="bi bi-shield-check"></i><strong>Thanh toán an tâm</strong><p>COD và MB Bank tiện lợi.</p></article>
            </div>
          </section>

          <section class="home-final-cta">
            <div>
              <span class="section-heading__eyebrow">Your next favourite</span>
              <h2>Sẵn sàng nâng cấp outfit?</h2>
              <p>Đi tìm món đồ khiến bạn muốn mặc ngay hôm nay.</p>
            </div>
            <router-link to="/shop" class="btn btn-y2k-primary">Xem tất cả sản phẩm <i class="bi bi-arrow-up-right ms-2"></i></router-link>
          </section>

          <section class="home-about-teaser">
            <div>
              <span class="section-heading__eyebrow">About Y2K / 03</span>
              <h2>Không chỉ là quần áo. Đây là mood của bạn.</h2>
              <p>Y2K Store chọn những item có cá tính, dễ phối và đủ linh hoạt để mỗi ngày bạn mặc một phiên bản khác của chính mình.</p>
            </div>
            <router-link to="/about" class="btn btn-y2k-outline">Về chúng mình <i class="bi bi-arrow-up-right ms-2"></i></router-link>
          </section>
        </section>

        <aside class="home-sidebar">
          <div class="home-sidebar-card home-sidebar-card--categories">
            <div class="home-sidebar-card__title">
              <i class="bi bi-grid"></i>
              <span>Danh mục</span>
            </div>
            <router-link v-for="category in generalCategories" :key="category.id" :to="`/shop?category=${category.id}`" class="home-category-link">
              <i class="bi bi-tag"></i>
              <span>{{ category.name }}</span>
            </router-link>
            <router-link to="/shop?sale=true" class="home-category-link home-category-link--sale">
              <i class="bi bi-fire"></i>
              <span>Sale off</span>
            </router-link>
          </div>

          <div class="home-sidebar-card home-sidebar-card--promo">
            <div class="home-promo-visual" aria-hidden="true">
              <span>Y2K</span>
            </div>
            <div class="home-promo-copy">
              <strong>Summer drop</strong>
              <span>Nhẹ, sáng, dễ phối cho ngày nóng.</span>
              <router-link to="/new-arrivals">Xem hàng mới</router-link>
            </div>
          </div>

          <div class="home-sidebar-card home-sidebar-card--mini">
            <i class="bi bi-stars"></i>
            <strong>Phối nhanh trong 2 phút</strong>
            <p>Chọn danh mục, lọc theo giá, thêm vào giỏ và checkout trong một luồng gọn hơn.</p>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import ProductCard from '../components/ProductCard.vue'
import { normalizeBanner, normalizeCategory, normalizeProduct } from '../composables/useCatalog'

const banners = ref([])
const categories = ref([])
const featuredProducts = ref([])
const topRatedProducts = ref([])
const allProducts = ref([])
const railRefs = ref([])
const loadingFeatured = ref(false)
const loadingTopRated = ref(false)
const generalCategories = computed(() => categories.value.filter((category) => category.categoryType !== 'TAG'))
const categoryRows = computed(() => generalCategories.value.slice(0, 5).map((category) => ({
  ...category,
  products: allProducts.value.filter((product) => Number(product.categoryId) === Number(category.id) || (product.categoryIds || []).map(Number).includes(Number(category.id))).slice(0, 5)
})).filter((row) => row.products.length))

const bannerSlides = computed(() => banners.value.length ? banners.value : [{ imageUrl: '/y2k_banner.png', title: 'Y2K VIBES', subtitle: 'Streetwear nổi bật, dễ phối, lên outfit nhanh mỗi ngày.', linkUrl: '/shop' }])

onMounted(async () => {
  await Promise.all([
    fetchBanners(),
    fetchCategories(),
    fetchFeaturedProducts(),
    fetchTopRatedProducts(),
    fetchAllProducts()])
})

async function fetchBanners() {
  try {
    const { data } = await axios.get('/api/banners')
    banners.value = (data || []).map(normalizeBanner)
  } catch {
    banners.value = []
  }
}

async function fetchCategories() {
  try {
    const { data } = await axios.get('/api/categories')
    categories.value = (data || []).map(normalizeCategory)
  } catch {
    categories.value = []
  }
}

async function fetchFeaturedProducts() {
  loadingFeatured.value = true
  try {
    const { data } = await axios.get('/api/products/featured', { params: { type: 'best-selling', limit: 10 } })
    featuredProducts.value = (data || []).map(normalizeProduct)
  } catch {
    featuredProducts.value = []
  } finally {
    loadingFeatured.value = false
  }
}

async function fetchTopRatedProducts() {
  loadingTopRated.value = true
  try {
    const { data } = await axios.get('/api/products/featured', { params: { type: 'top-rated', limit: 10 } })
    topRatedProducts.value = (data || []).map(normalizeProduct)
  } catch {
    topRatedProducts.value = []
  } finally {
    loadingTopRated.value = false
  }
}

async function fetchAllProducts() {
  try {
    const { data } = await axios.get('/api/products')
    allProducts.value = (data || []).map(normalizeProduct)
  } catch {
    allProducts.value = []
  }
}

function setRailRef(element, index) {
  if (element) railRefs.value[index] = element
}

function scrollRail(index, direction) {
  railRefs.value[index]?.scrollBy({ left: direction * 300, behavior: 'smooth' })
}
</script>
