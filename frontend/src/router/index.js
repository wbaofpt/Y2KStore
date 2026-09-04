import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import HomeView from '../views/HomeView.vue'
import ShopView from '../views/ShopView.vue'
import NewArrivalsView from '../views/NewArrivalsView.vue'
import ProductDetailView from '../views/ProductDetailView.vue'
import CartView from '../views/CartView.vue'
import CheckoutView from '../views/CheckoutView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import AccountView from '../views/AccountView.vue'
import OrderHistoryView from '../views/OrderHistoryView.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import AboutView from '../views/AboutView.vue'
import PromotionPolicyView from '../views/PromotionPolicyView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/shop', name: 'shop', component: ShopView },
  { path: '/new-arrivals', name: 'new-arrivals', component: NewArrivalsView },
  { path: '/about', name: 'about', component: AboutView },
  { path: '/chinh-sach-khuyen-mai', name: 'promotion-policy', component: PromotionPolicyView },
  { path: '/product/:slug', name: 'product-detail', component: ProductDetailView },
  { path: '/cart', name: 'cart', component: CartView, meta: { requiresAuth: true } },
  { path: '/checkout', name: 'checkout', component: CheckoutView, meta: { requiresAuth: true } },
  { path: '/account', name: 'account', component: AccountView, meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: OrderHistoryView, meta: { requiresAuth: true } },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
  { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView, meta: { guestOnly: true } },
  { path: '/admin', name: 'admin', component: AdminDashboardView, meta: { requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.path === from.path && to.path === '/shop') {
      return false
    }
    return { top: 0, behavior: 'smooth' }
  }
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAdmin) {
    if (!authStore.isAuthenticated) return next('/login')
    if (!authStore.isAdmin) return next('/account')
    return next()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next('/login')
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return next(authStore.isAdmin ? '/admin' : '/account')
  }

  return next()
})

export default router
