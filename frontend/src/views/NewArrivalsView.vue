<template>
  <div class="section-shell new-arrivals-page">
    <div class="y2k-page-container">
      <header class="new-drop-hero">
        <div class="new-drop-hero__main">
          <div class="new-drop-kicker">
            <span class="new-drop-kicker__index">01</span>
            <span>Y2K STORE / FRESH EDIT</span>
          </div>
          <h1>Mới lên kệ.<br><em>Đúng vibe.</em></h1>
          <p>Những item được thêm trong 14 ngày gần nhất. Lướt nhanh, chọn đúng mood và cập nhật tủ đồ theo cách của bạn.</p>
          <div class="new-drop-hero__actions">
            <a href="#new-drop-products" class="btn btn-y2k-primary"><i class="bi bi-arrow-down me-2"></i>Khám phá drop</a>
            <router-link to="/shop" class="new-drop-text-link">Xem toàn bộ shop <i class="bi bi-arrow-up-right"></i></router-link>
          </div>
        </div>

        <div class="new-drop-hero__visual" aria-label="Bộ sưu tập hàng mới Y2K">
          <div class="new-drop-hero__shape new-drop-hero__shape--cyan"></div>
          <div class="new-drop-hero__shape new-drop-hero__shape--pink"></div>
          <div class="new-drop-hero__ticket">
            <span>DROP</span>
            <strong>NEW</strong>
            <small>NO. 014 / 2026</small>
          </div>
          <span class="new-drop-hero__side-label">JUST<br>ADDED</span>
        </div>
      </header>

      <nav class="new-drop-nav" aria-label="Điều hướng mua sắm">
        <div class="new-drop-nav__intro">
          <span>Explore edit</span>
          <strong>Chọn theo mood</strong>
        </div>
        <div class="new-drop-nav__links">
          <router-link to="/new-arrivals" class="is-active" aria-current="page"><i class="bi bi-stars"></i><span>Hàng mới</span></router-link>
          <router-link to="/shop?sort=best-selling"><i class="bi bi-fire"></i><span>Bán chạy nhất</span></router-link>
          <router-link to="/shop?sort=rating"><i class="bi bi-star"></i><span>Đánh giá cao</span></router-link>
          <router-link to="/shop?sale=true" class="new-drop-nav__sale"><i class="bi bi-lightning-charge"></i><span>Sale</span></router-link>
        </div>
      </nav>

      <main id="new-drop-products" class="new-drop-products">
        <div class="new-drop-section-heading">
          <div>
            <span class="section-heading__eyebrow">The latest edit</span>
            <h2>Vừa lên kệ</h2>
          </div>
          <p>Được sắp xếp từ mới nhất đến cũ hơn.</p>
        </div>

        <div v-if="loading" class="new-drop-state surface-card" aria-live="polite">
          <span class="new-drop-loader" aria-hidden="true"></span>
          <span>Đang cập nhật drop mới...</span>
        </div>
        <div v-else-if="products.length === 0" class="new-drop-state empty-state">
          <i class="bi bi-stars"></i>
          <h3>Drop đang được chuẩn bị</h3>
          <p class="muted-copy">Sản phẩm mới sẽ xuất hiện ở đây ngay sau khi được thêm vào shop.</p>
          <router-link to="/shop" class="btn btn-y2k-outline">Khám phá shop</router-link>
        </div>
        <div v-else class="new-drop-grid">
          <ProductCard v-for="(product, index) in products" :key="product.id" :product="product" :class="{ 'new-drop-grid__featured': index === 0 }" />
        </div>
      </main>

      <footer class="new-drop-footer">
        <span class="new-drop-footer__line"></span>
        <p><i class="bi bi-stars"></i> New is a feeling, not a season.</p>
        <span class="new-drop-footer__line"></span>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import ProductCard from '../components/ProductCard.vue'
import { normalizeProduct } from '../composables/useCatalog'

const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/products/featured', { params: { type: 'new', limit: 24 } })
    products.value = (data || []).map(normalizeProduct)
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.new-arrivals-page { padding: 2.5rem 0 4.5rem; background: #fafafa; }
.new-drop-hero { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(300px, .95fr); min-height: 445px; border: 1px solid #e4e4e7; background: #fff; box-shadow: 7px 8px 0 #a5f3fc; overflow: hidden; }
.new-drop-hero__main { display: flex; flex-direction: column; justify-content: center; padding: clamp(2rem, 5vw, 4.5rem); }
.new-drop-kicker { display: flex; align-items: center; gap: .7rem; color: #0891b2; font-size: .7rem; font-weight: 900; letter-spacing: .12em; }
.new-drop-kicker__index { display: inline-grid; width: 28px; height: 28px; place-items: center; border-radius: 50%; background: #18181b; color: #fff; letter-spacing: 0; }
.new-drop-hero h1 { margin: 1.25rem 0 1rem; color: #18181b; font-size: 4.7rem; line-height: .92; letter-spacing: 0; }
.new-drop-hero h1 em { color: #ec4899; font-style: normal; }
.new-drop-hero p { max-width: 520px; margin: 0; color: #52525b; font-size: 1rem; line-height: 1.7; }
.new-drop-hero__actions { display: flex; align-items: center; flex-wrap: wrap; gap: 1.25rem; margin-top: 1.75rem; }
.new-drop-text-link { color: #18181b; font-size: .78rem; font-weight: 900; text-decoration: none; }
.new-drop-text-link:hover { color: #0891b2; }
.new-drop-text-link i { margin-left: .25rem; color: #ec4899; }
.new-drop-hero__visual { position: relative; min-height: 445px; background: #18181b; isolation: isolate; overflow: hidden; }
.new-drop-hero__visual::before { content: 'Y2K'; position: absolute; right: -1.2rem; bottom: -1rem; color: #27272a; font-size: 10rem; font-weight: 900; line-height: .8; letter-spacing: 0; transform: rotate(-8deg); }
.new-drop-hero__shape { position: absolute; border: 1px solid rgba(255,255,255,.6); }
.new-drop-hero__shape--cyan { width: 245px; height: 245px; top: 42px; left: 12%; border-radius: 50%; background: #22d3ee; box-shadow: 17px 17px 0 #f0abfc; transform: rotate(-12deg); }
.new-drop-hero__shape--pink { width: 120px; height: 120px; right: 12%; bottom: 54px; border-radius: 50%; background: #f0abfc; transform: rotate(18deg); }
.new-drop-hero__ticket { position: absolute; z-index: 2; top: 50%; left: 50%; display: flex; width: 205px; height: 235px; flex-direction: column; align-items: center; justify-content: center; border: 1px solid #18181b; background: #fff; color: #18181b; box-shadow: 8px 8px 0 #f0abfc; transform: translate(-50%, -50%) rotate(7deg); }
.new-drop-hero__ticket::before, .new-drop-hero__ticket::after { content: ''; position: absolute; width: 22px; height: 22px; border-radius: 50%; background: #18181b; }
.new-drop-hero__ticket::before { top: -12px; left: -12px; }.new-drop-hero__ticket::after { right: -12px; bottom: -12px; }
.new-drop-hero__ticket span { color: #0891b2; font-size: .78rem; font-weight: 900; letter-spacing: .22em; }
.new-drop-hero__ticket strong { margin: .25rem 0 .8rem; font-size: 3.6rem; line-height: 1; letter-spacing: 0; }
.new-drop-hero__ticket small { padding-top: .65rem; border-top: 1px dashed #a1a1aa; font-size: .62rem; font-weight: 900; letter-spacing: .15em; }
.new-drop-hero__side-label { position: absolute; top: 1.25rem; right: 1.25rem; z-index: 3; color: #fff; font-size: .62rem; font-weight: 900; line-height: 1.15; letter-spacing: .17em; text-align: right; }
.new-drop-nav { display: grid; grid-template-columns: 210px minmax(0, 1fr); align-items: center; gap: 1rem; margin: 3rem 0 2.5rem; padding: 1rem; border: 1px solid #e4e4e7; background: #fff; box-shadow: 4px 4px 0 #f0abfc; }.new-drop-nav__intro { display: flex; flex-direction: column; padding: .2rem .75rem; border-right: 1px solid #e4e4e7; }.new-drop-nav__intro span { color: #0891b2; font-size: .63rem; font-weight: 900; letter-spacing: .14em; text-transform: uppercase; }.new-drop-nav__intro strong { margin-top: .3rem; color: #18181b; font-size: .96rem; }.new-drop-nav__links { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: .55rem; }.new-drop-nav a { display: flex; min-height: 48px; align-items: center; justify-content: center; gap: .45rem; border: 1px solid #e4e4e7; color: #52525b; font-size: .76rem; font-weight: 900; text-align: center; text-decoration: none; transition: color .2s ease, border-color .2s ease, background .2s ease, transform .2s ease, box-shadow .2s ease; }.new-drop-nav a i { color: #0891b2; font-size: .9rem; }.new-drop-nav a:hover, .new-drop-nav a.is-active { border-color: #18181b; background: #18181b; color: #fff; box-shadow: 3px 3px 0 #22d3ee; transform: translateY(-2px); }.new-drop-nav a.is-active i { color: #8feefa; }.new-drop-nav a.new-drop-nav__sale { border-color: #f0abfc; color: #be185d; }.new-drop-nav a.new-drop-nav__sale i { color: #ec4899; }.new-drop-nav a.new-drop-nav__sale:hover { border-color: #ec4899; background: #fdf2f8; color: #be185d; box-shadow: 3px 3px 0 #f9a8d4; }
.new-drop-section-heading { display: flex; align-items: end; justify-content: space-between; gap: 1.5rem; margin-bottom: 1.4rem; }.new-drop-section-heading h2 { margin: .35rem 0 0; color: #18181b; font-size: 2.1rem; letter-spacing: 0; }.new-drop-section-heading p { margin: 0 0 .2rem; color: #71717a; font-size: .8rem; }.new-drop-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1.2rem; align-items: stretch; }.new-drop-grid .product-card { min-width: 0; }
.new-drop-state { display: flex; min-height: 240px; align-items: center; justify-content: center; gap: .7rem; padding: 3rem; text-align: center; }.new-drop-state.empty-state { flex-direction: column; }.new-drop-state > i { color: #0891b2; font-size: 2rem; }.new-drop-loader { width: 18px; height: 18px; border: 2px solid #a5f3fc; border-top-color: #18181b; border-radius: 50%; animation: new-drop-spin .75s linear infinite; }@keyframes new-drop-spin { to { transform: rotate(360deg); } }
.new-drop-footer { display: flex; align-items: center; gap: 1rem; margin-top: 4rem; }.new-drop-footer__line { height: 1px; flex: 1; background: #d4d4d8; }.new-drop-footer p { margin: 0; color: #71717a; font-size: .72rem; font-weight: 800; white-space: nowrap; }.new-drop-footer i { margin-right: .25rem; color: #ec4899; }
@media (max-width: 1000px) { .new-drop-hero { grid-template-columns: 1fr 340px; }.new-drop-hero h1 { font-size: 3.8rem; }.new-drop-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } .new-drop-grid .product-card:nth-child(n+7) { display: none; } }
@media (max-width: 760px) { .new-arrivals-page { padding-top: 1rem; }.new-drop-hero { display: block; }.new-drop-hero__main { min-height: 370px; }.new-drop-hero__visual { min-height: 300px; }.new-drop-hero__shape--cyan { width: 160px; height: 160px; }.new-drop-hero__ticket { width: 155px; height: 180px; }.new-drop-hero__ticket strong { font-size: 2.7rem; }.new-drop-nav { display: block; margin: 2rem 0; padding: .8rem; }.new-drop-nav__intro { margin-bottom: .8rem; padding: .2rem .4rem .75rem; border-right: 0; border-bottom: 1px solid #e4e4e7; }.new-drop-nav__links { display: flex; gap: .5rem; overflow-x: auto; scrollbar-width: none; }.new-drop-nav__links::-webkit-scrollbar { display: none; }.new-drop-nav a { flex: 0 0 auto; min-width: 128px; padding: 0 .8rem; }.new-drop-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .75rem; }.new-drop-grid .product-card:nth-child(n+7) { display: block; }.new-drop-section-heading { align-items: start; flex-direction: column; gap: .5rem; } }
@media (max-width: 430px) { .new-drop-hero h1 { font-size: 3rem; }.new-drop-hero__main { padding: 2rem 1.25rem; }.new-drop-nav a { min-width: 116px; font-size: .7rem; }.new-drop-footer p { font-size: .6rem; } }
@media (prefers-reduced-motion: reduce) { .new-drop-loader { animation: none; }.new-drop-nav a { transition: none; } }
</style>
