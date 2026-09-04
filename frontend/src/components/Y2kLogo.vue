<template>
  <div class="y2k-logo" :class="{ 'y2k-logo--compact': compact }">
    <div class="y2k-logo__mark" aria-hidden="true">
      <span class="y2k-logo__mark-text">Y2K</span>
      <span class="y2k-logo__mark-accent"></span>
    </div>
    <div class="y2k-logo__wordmark">
      <span class="y2k-logo__store">STORE</span>
      <span v-if="!compact" class="y2k-logo__tagline">Discover Your Style</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  compact: { type: Boolean, default: false }
})
</script>

<style scoped>
.y2k-logo {
  display: inline-flex;
  align-items: center;
  gap: 13px;
  line-height: 1;
  color: var(--y2k-dark);
  transition: transform .2s ease;
}

.y2k-logo__mark {
  position: relative;
  width: 48px;
  height: 48px;
  border: 1px solid var(--y2k-dark);
  border-radius: 4px;
  background: var(--y2k-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 3px 3px 0 var(--y2k-accent);
  transition: background .2s ease, box-shadow .2s ease, transform .2s ease;
}

.y2k-logo__mark-text {
  font-family: var(--font-brand);
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 0;
  color: #fff;
}

.y2k-logo__mark-accent {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 11px;
  height: 11px;
  border-top: 1px solid var(--y2k-dark);
  border-left: 1px solid var(--y2k-dark);
  background: var(--y2k-accent);
}

.y2k-logo__wordmark {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.y2k-logo__store {
  font-family: var(--font-brand);
  font-weight: 900;
  font-size: 1.75rem;
  letter-spacing: .08em;
  color: var(--y2k-dark);
  text-transform: uppercase;
}

.y2k-logo__tagline {
  font-family: var(--font-sec);
  font-size: 0.65rem;
  font-weight: 800;
  letter-spacing: .24em;
  text-transform: uppercase;
  color: var(--y2k-accent-strong);
}

.y2k-logo:hover {
  transform: translateY(-1px);
}

.y2k-logo:hover .y2k-logo__mark {
  background: #27272a;
  box-shadow: 4px 4px 0 #f0abfc;
  transform: translateY(-1px);
}

.y2k-logo--compact .y2k-logo__mark {
  width: 38px;
  height: 38px;
  box-shadow: 2px 2px 0 var(--y2k-accent);
}

.y2k-logo--compact .y2k-logo__store {
  font-size: 1.35rem;
}

.y2k-logo--compact .y2k-logo__tagline {
  letter-spacing: .16em;
}

@media (prefers-reduced-motion: reduce) {
  .y2k-logo,
  .y2k-logo__mark { transition: none; }
}
</style>
