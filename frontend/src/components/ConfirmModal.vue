<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="confirmStore.visible" class="y2k-modal-backdrop" @click.self="confirmStore.cancel()">
        <div class="y2k-modal" role="dialog" aria-modal="true">
          <div class="y2k-modal__icon" :class="`y2k-modal__icon--${confirmStore.variant}`">
            <i class="bi bi-exclamation-lg"></i>
          </div>
          <h5 class="y2k-modal__title">{{ confirmStore.title }}</h5>
          <p class="y2k-modal__message">{{ confirmStore.message }}</p>
          <div class="y2k-modal__actions">
            <button class="btn btn-y2k-outline py-2 px-4" @click="confirmStore.cancel()">
              {{ confirmStore.cancelText }}
            </button>
            <button
              class="btn py-2 px-4"
              :class="confirmStore.variant === 'danger' ? 'btn-y2k-danger' : 'btn-y2k-primary'"
              @click="confirmStore.accept()"
            >
              {{ confirmStore.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useConfirmStore } from '../stores/confirm'

const confirmStore = useConfirmStore()
</script>

<style scoped>
.y2k-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(20, 20, 20, 0.45);
  backdrop-filter: blur(4px);
}

.y2k-modal {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  max-width: 420px;
  width: 100%;
  text-align: center;
  border: 2px solid var(--y2k-dark);
  box-shadow: 8px 8px 0 var(--y2k-dark);
}

.y2k-modal__icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  font-size: 1.5rem;
}

.y2k-modal__icon--danger {
  background: #fef2f2;
  color: var(--y2k-sale);
}

.y2k-modal__icon--primary {
  background: var(--y2k-accent-bg);
  color: var(--y2k-accent);
}

.y2k-modal__title {
  font-family: var(--font-primary);
  font-weight: 800;
  font-size: 1.15rem;
  margin-bottom: 0.5rem;
  color: var(--y2k-dark);
}

.y2k-modal__message {
  color: var(--y2k-muted);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.y2k-modal__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.y2k-modal__actions .btn {
  flex: 1;
  font-size: 0.85rem;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .y2k-modal,
.modal-leave-active .y2k-modal {
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .y2k-modal {
  transform: scale(0.85) translateY(20px);
  opacity: 0;
}

.modal-leave-to .y2k-modal {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}
</style>
