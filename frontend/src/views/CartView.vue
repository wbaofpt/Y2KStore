<template>
  <div class="y2k-page-container cart-page">
    <section class="cart-hero">
      <div>
        <span class="section-heading__eyebrow">Giỏ hàng</span>
        <h1>Kiểm tra giỏ trước khi chốt đơn</h1>
        <p>Điều chỉnh số lượng, kiểm tra biến thể và xem tổng tiền trước khi thanh toán.</p>
      </div>
      <router-link to="/shop" class="btn btn-y2k-outline">Tiếp tục mua sắm</router-link>
    </section>

    <div class="cart-layout cart-layout--redesign">
      <section class="cart-items-panel">
        <div class="cart-panel-head">
          <div>
            <h2>Sản phẩm trong giỏ</h2>
            <span>{{ cartStore.totalCount }} sản phẩm</span>
          </div>
          <button
            v-if="cartStore.items.length"
            class="btn btn-y2k-outline"
            @click="confirmClearCart"
          >
            Xóa giỏ hàng
          </button>
        </div>

        <div v-if="cartStore.loading" class="surface-card cart-loading">
          <span class="spinner-border spinner-border-sm"></span>
          Đang tải giỏ hàng...
        </div>

        <div v-else-if="!cartStore.items.length" class="empty-state cart-empty-state">
          <i class="bi bi-bag"></i>
          <h4>Giỏ hàng đang trống</h4>
          <p class="muted-copy">Chọn vài item Y2K rồi quay lại đây để thanh toán nhé.</p>
          <router-link to="/shop" class="btn btn-y2k-primary">Mở cửa hàng</router-link>
        </div>

        <div v-else class="cart-item-list">
          <article v-for="item in cartStore.items" :key="item.variantId || item.id" class="cart-line cart-line--redesign">
            <router-link :to="item.productName ? productPath({ name: item.productName }) : '/shop'" class="cart-line__image">
              <img :src="item.productImage || '/favicon.svg'" :alt="item.productName">
            </router-link>

            <div class="cart-line__content">
              <div class="cart-line__top">
                <div>
                  <router-link :to="item.productName ? productPath({ name: item.productName }) : '/shop'" class="cart-line__title">
                    {{ item.productName }}
                  </router-link>
                  <p>{{ item.variantLabel || 'Biến thể mặc định' }}</p>
                </div>
                <strong>{{ formatPrice(item.totalPrice) }}</strong>
              </div>

              <div class="cart-line__bottom">
                <div class="cart-line__price">
                  <span>Đơn giá</span>
                  <strong>{{ formatPrice(item.unitPrice) }}</strong>
                </div>

                <div class="qty-controls cart-line__qty">
                  <button @click="updateQty(item.variantId || item.id, item.quantity - 1)" aria-label="Giảm số lượng">-</button>
                  <span>{{ item.quantity }}</span>
                  <button @click="updateQty(item.variantId || item.id, item.quantity + 1)" aria-label="Tăng số lượng">+</button>
                </div>

                <button class="cart-remove-btn" @click="removeItem(item.variantId || item.id)">
                  <i class="bi bi-trash"></i>
                  Xóa
                </button>
              </div>
            </div>
          </article>
        </div>
      </section>

      <aside class="cart-summary-panel">
        <div class="cart-summary-card">
          <div class="cart-summary-card__head">
            <span class="section-heading__eyebrow">Tóm tắt</span>
            <h3>Đơn hàng của bạn</h3>
          </div>

          <div class="cart-free-ship">
            <div class="cart-free-ship__top">
              <span>{{ freeShipMessage }}</span>
              <strong>{{ Math.min(freeShipProgress, 100) }}%</strong>
            </div>
            <div class="cart-free-ship__bar">
              <span :style="{ width: `${Math.min(freeShipProgress, 100)}%` }"></span>
            </div>
          </div>

          <div class="summary-stack">
            <div class="summary-row">
              <span>Tạm tính</span>
              <strong>{{ formatPrice(cartStore.totalAmount) }}</strong>
            </div>
            <div class="summary-row">
              <span>Số lượng</span>
              <strong>{{ cartStore.totalCount }}</strong>
            </div>
            <div class="summary-row">
              <span>Phí giao hàng</span>
              <strong>{{ shippingFee === 0 ? 'Miễn phí' : formatPrice(shippingFee) }}</strong>
            </div>
            <div class="summary-row summary-row--total">
              <span>Tổng thanh toán</span>
              <strong>{{ formatPrice(totalAmount) }}</strong>
            </div>
          </div>

          <router-link
            to="/checkout"
            class="btn btn-y2k-primary w-100 mt-3"
            :class="{ disabled: !cartStore.items.length }"
          >
            Thanh toán ngay
          </router-link>
          <router-link to="/shop" class="btn btn-y2k-outline w-100 mt-2">Mua thêm sản phẩm</router-link>
        </div>

        <div class="cart-assurance-card">
          <div>
            <i class="bi bi-arrow-left-right"></i>
            <span>Đổi trả 7 ngày</span>
          </div>
          <div>
            <i class="bi bi-shield-check"></i>
            <span>Kiểm tra hàng trước khi gửi</span>
          </div>
          <div>
            <i class="bi bi-headset"></i>
            <span>Hỗ trợ phối đồ nhanh</span>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import { useConfirmStore } from '../stores/confirm'
import { useToastStore } from '../stores/toast'
import { formatPrice } from '../composables/useFormat'
import { productPath } from '../composables/useCatalog'

const cartStore = useCartStore()
const confirmStore = useConfirmStore()
const toast = useToastStore()

const freeShipThreshold = 499000
const shippingFee = computed(() => cartStore.totalAmount >= freeShipThreshold || cartStore.totalAmount === 0 ? 0 : 30000)
const totalAmount = computed(() => cartStore.totalAmount + shippingFee.value)
const freeShipProgress = computed(() => Math.round((cartStore.totalAmount / freeShipThreshold) * 100))
const freeShipMessage = computed(() => {
  if (!cartStore.totalAmount) return 'Thêm sản phẩm để đạt freeship từ 499k'
  if (cartStore.totalAmount >= freeShipThreshold) return 'Bạn đã đạt miễn phí giao hàng'
  return `Mua thêm ${formatPrice(freeShipThreshold - cartStore.totalAmount)} để freeship`
})

onMounted(() => {
  cartStore.fetchCart()
})

function updateQty(id, quantity) {
  if (quantity < 1) return
  cartStore.updateQuantity(id, quantity)
}

async function removeItem(id) {
  const accepted = await confirmStore.open({
    title: 'Xóa sản phẩm',
    message: 'Sản phẩm này sẽ được xóa khỏi giỏ hàng.',
    confirmText: 'Xóa',
    cancelText: 'Giữ lại'
  })
  if (!accepted) return
  await cartStore.removeItem(id)
  toast.success('Đã xóa sản phẩm khỏi giỏ hàng.')
}

async function confirmClearCart() {
  const accepted = await confirmStore.open({
    title: 'Xóa toàn bộ giỏ hàng',
    message: 'Tất cả sản phẩm trong giỏ sẽ bị xóa.',
    confirmText: 'Xóa tất cả',
    cancelText: 'Hủy'
  })
  if (!accepted) return
  await cartStore.clearCart()
  toast.success('Đã xóa toàn bộ giỏ hàng.')
}
</script>
