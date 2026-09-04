<template>
  <main class="not-found-page y2k-page-container">
    <section class="not-found-card">
      <div class="not-found-card__orb not-found-card__orb--one"></div>
      <div class="not-found-card__orb not-found-card__orb--two"></div>
      <div class="not-found-card__content">
        <span class="not-found-card__code">404 / OUT OF STYLE</span>
        <div class="not-found-card__number">404</div>
        <h1>Trang này đã rời khỏi catalog.</h1>
        <p>Có vẻ đường dẫn bạn tìm không còn ở đây. Quay lại shop để tiếp tục tìm item hợp mood nhé.</p>
        <div class="not-found-card__actions">
          <router-link to="/" class="btn btn-y2k-primary">Về trang chủ <i class="bi bi-arrow-up-right ms-2"></i></router-link>
          <router-link to="/shop" class="btn btn-y2k-outline">Mở cửa hàng</router-link>
        </div>
      </div>
      <div class="not-found-card__sticker"><i class="bi bi-stars"></i><span>Keep<br>exploring</span></div>
    </section>
  </main>
</template>
