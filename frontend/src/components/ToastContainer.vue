<template>
  <div class="y2k-toast-container" aria-live="polite" aria-atomic="true">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        class="y2k-toast"
        :class="`y2k-toast--${toast.type}`"
        role="alert"
      >
        <div class="y2k-toast__icon" aria-hidden="true">
          <i :class="iconMap[toast.type]"></i>
        </div>
        <div class="y2k-toast__body">
          <span class="y2k-toast__eyebrow">{{ typeLabel[toast.type] }}</span>
          <p class="y2k-toast__message">{{ toast.message }}</p>
          <router-link
            v-if="toast.action?.to"
            :to="toast.action.to"
            class="y2k-toast__action"
            @click="toastStore.remove(toast.id)"
          >
            {{ toast.action.label }} <i class="bi bi-arrow-right"></i>
          </router-link>
        </div>
        <button class="y2k-toast__close" @click="toastStore.remove(toast.id)" aria-label="Đóng thông báo">
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
        <div class="y2k-toast__progress"></div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToastStore } from '../stores/toast'

const toastStore = useToastStore()

const iconMap = {
  success: 'bi bi-check-circle-fill',
  error: 'bi bi-x-circle-fill',
  warning: 'bi bi-exclamation-triangle-fill',
  info: 'bi bi-info-circle-fill'
}

const typeLabel = {
  success: 'Đã cập nhật',
  error: 'Có lỗi xảy ra',
  warning: 'Lưu ý',
  info: 'Thông tin'
}
</script>

<style scoped>
.y2k-toast-container {
  position: fixed;
  top: auto;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: min(380px, calc(100vw - 32px));
  max-width: none;
  pointer-events: none;
}

.y2k-toast {
  pointer-events: auto;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-start;
  gap: 11px;
  min-height: 84px;
  padding: 14px 14px 17px;
  background: #fff;
  border: 1px solid #e4e4e7;
  border-left: 5px solid var(--toast-accent, var(--y2k-accent));
  border-radius: 14px;
  box-shadow: 0 18px 44px rgba(15, 23, 42, .14), 0 2px 10px rgba(15, 23, 42, .06);
  color: #18181b;
}

.y2k-toast__icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  margin-top: 1px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--toast-accent, var(--y2k-accent)) 12%, #fff);
  color: var(--toast-accent, var(--y2k-accent));
  font-size: 1.18rem;
}

.y2k-toast--success {
  --toast-accent: #059669;
}

.y2k-toast--error {
  --toast-accent: #dc2626;
}

.y2k-toast--warning {
  --toast-accent: #d97706;
}

.y2k-toast--info {
  --toast-accent: var(--y2k-accent);
}

.y2k-toast__body {
  flex: 1;
  min-width: 0;
  padding-top: 1px;
}

.y2k-toast__eyebrow {
  display: block;
  margin-bottom: 2px;
  color: var(--toast-accent, var(--y2k-accent));
  font-size: .68rem;
  font-weight: 900;
  text-transform: uppercase;
}

.y2k-toast__message {
  margin: 0;
  color: #18181b;
  font-size: .92rem;
  font-weight: 800;
  line-height: 1.42;
}

.y2k-toast__action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  margin-top: 10px;
  padding: 0 .72rem;
  border: 1px solid color-mix(in srgb, var(--toast-accent, var(--y2k-accent)) 55%, #fff);
  border-radius: 999px;
  background: #fff;
  color: #0f172a;
  font-size: .8rem;
  font-weight: 900;
  text-decoration: none;
  transition: border-color .18s ease, color .18s ease, transform .18s ease;
}

.y2k-toast__action:hover {
  border-color: var(--toast-accent, var(--y2k-accent));
  color: var(--toast-accent, var(--y2k-accent));
  transform: translateY(-1px);
}

.y2k-toast__close {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: #f4f4f5;
  color: #52525b;
  cursor: pointer;
  transition: background .18s ease, color .18s ease;
}

.y2k-toast__close:hover {
  background: #e4e4e7;
  color: #18181b;
}

.y2k-toast__progress {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 5px;
  height: 3px;
  background: var(--toast-accent, var(--y2k-accent));
  transform-origin: left;
  animation: toast-progress 3.8s linear forwards;
}

.y2k-toast--error .y2k-toast__progress {
  animation-duration: 5s;
}

.y2k-toast--warning .y2k-toast__progress {
  animation-duration: 4.5s;
}

@keyframes toast-progress {
  from { transform: scaleX(1); }
  to { transform: scaleX(0); }
}

.toast-enter-active {
  transition: opacity .18s ease-out, transform .18s ease-out;
}
.toast-leave-active {
  transition: opacity .16s ease, transform .16s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
.toast-move {
  transition: transform .18s ease;
}

@media (max-width: 560px) {
  .y2k-toast-container {
    right: 16px;
    bottom: 16px;
    width: calc(100vw - 32px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .y2k-toast__progress,
  .toast-enter-active,
  .toast-leave-active,
  .toast-move {
    animation: none;
    transition: none;
  }
}
</style>
