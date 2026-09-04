<template>
  <main class="promotion-page y2k-page-container">
    <section class="promotion-hero">
      <div class="promotion-hero__copy">
        <span class="section-heading__eyebrow">Y2K PROMO GUIDE</span>
        <h1>Ưu đãi rõ ràng.<br><em>Outfit vui hơn.</em></h1>
        <p>Toàn bộ thông tin về mã giảm giá, chương trình sale và cách áp dụng ưu đãi tại Y2K Store.</p>
        <div class="promotion-hero__actions">
          <router-link to="/shop?sale=true" class="btn btn-y2k-primary">Xem sản phẩm sale <i class="bi bi-arrow-up-right"></i></router-link>
          <a href="#how-to-use" class="btn btn-y2k-outline">Cách dùng mã</a>
        </div>
      </div>
      <div class="promotion-hero__visual" aria-hidden="true">
        <span class="promotion-hero__sticker">SAVE<br>MORE</span>
        <strong>-30%</strong>
        <small>Y2K<br>DROP</small>
        <i class="bi bi-lightning-charge-fill"></i>
      </div>
    </section>

    <section class="promotion-highlights" aria-label="Điểm nổi bật của chương trình">
      <article><i class="bi bi-ticket-perforated"></i><div><span>MÃ ƯU ĐÃI</span><h2>Nhập mã ở checkout</h2><p>Kiểm tra điều kiện trước khi thanh toán.</p></div></article>
      <article><i class="bi bi-box-seam"></i><div><span>SALE TỰ ĐỘNG</span><h2>Giá đã giảm sẵn</h2><p>Sản phẩm sale hiển thị giá sau giảm.</p></div></article>
      <article><i class="bi bi-shield-check"></i><div><span>MINH BẠCH</span><h2>Không phí ẩn</h2><p>Tổng tiền được cập nhật trước khi đặt hàng.</p></div></article>
    </section>

    <section class="promotion-section" id="how-to-use">
      <div class="promotion-section__heading">
        <span class="section-heading__eyebrow">HOW IT WORKS</span>
        <h2>Dùng ưu đãi trong 3 bước</h2>
        <p>Không cần nhớ nhiều. Chỉ cần chọn item bạn thích và để Y2K xử lý phần còn lại.</p>
      </div>
      <div class="promotion-steps">
        <article><b>01</b><i class="bi bi-bag-heart"></i><h3>Chọn sản phẩm</h3><p>Thêm sản phẩm đang có ưu đãi vào giỏ hàng.</p></article>
        <article><b>02</b><i class="bi bi-ticket-perforated"></i><h3>Nhập mã giảm giá</h3><p>Điền mã tại ô ưu đãi trong trang thanh toán.</p></article>
        <article><b>03</b><i class="bi bi-check2-circle"></i><h3>Kiểm tra & đặt hàng</h3><p>Chỉ thanh toán khi tổng tiền đã hiển thị đúng.</p></article>
      </div>
    </section>

    <section class="promotion-rules">
      <div class="promotion-rules__intro">
        <span class="section-heading__eyebrow">PROMO RULES</span>
        <h2>Điều kiện áp dụng</h2>
        <p>Một vài nguyên tắc nhỏ để mọi ưu đãi được áp dụng chính xác và công bằng.</p>
        <router-link to="/shop" class="promotion-text-link">Khám phá toàn bộ sản phẩm <i class="bi bi-arrow-right"></i></router-link>
      </div>
      <div class="promotion-rule-list">
        <article><i class="bi bi-check-lg"></i><div><h3>Mỗi đơn hàng dùng một mã</h3><p>Không cộng dồn nhiều mã giảm giá trong cùng một đơn, trừ khi chương trình có ghi rõ.</p></div></article>
        <article><i class="bi bi-check-lg"></i><div><h3>Giá sale có thể thay đổi</h3><p>Ưu đãi phụ thuộc thời gian và số lượng. Giá hiển thị tại checkout là giá cuối cùng.</p></div></article>
        <article><i class="bi bi-check-lg"></i><div><h3>Không quy đổi thành tiền mặt</h3><p>Mã giảm giá chỉ dùng cho mua sắm trên Y2K Store và không hoàn lại phần chênh lệch.</p></div></article>
        <article><i class="bi bi-check-lg"></i><div><h3>Đơn hủy không hoàn mã</h3><p>Mã đã dùng cho đơn bị hủy có thể không được cấp lại tùy điều kiện chương trình.</p></div></article>
      </div>
    </section>

    <section class="promotion-faq">
      <div class="promotion-section__heading">
        <span class="section-heading__eyebrow">NEED TO KNOW</span>
        <h2>Câu hỏi thường gặp</h2>
      </div>
      <div class="promotion-faq__list">
        <details open><summary>Làm sao biết mã đã được giảm thành công?</summary><p>Sau khi áp dụng, hệ thống sẽ hiển thị dòng giảm giá và cập nhật lại tổng tiền ngay trong phần tóm tắt đơn hàng.</p></details>
        <details><summary>Mã giảm giá có dùng cùng sản phẩm sale không?</summary><p>Tùy từng chương trình. Nếu không dùng chung được, hệ thống sẽ thông báo rõ trước khi bạn hoàn tất đặt hàng.</p></details>
        <details><summary>Nếu mã không hoạt động thì phải làm gì?</summary><p>Kiểm tra hạn sử dụng, giá trị đơn tối thiểu và danh mục áp dụng. Bạn cũng có thể liên hệ support@y2kstore.vn để được hỗ trợ.</p></details>
      </div>
    </section>

    <section class="promotion-cta">
      <div><span class="section-heading__eyebrow">READY TO SAVE?</span><h2>Chọn item. Nhập mã. Lên outfit.</h2></div>
      <router-link to="/shop" class="btn btn-y2k-primary">Mua sắm ngay <i class="bi bi-arrow-up-right"></i></router-link>
    </section>
  </main>
</template>
