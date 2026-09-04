# Y2K Store — Hướng dẫn hoạt động của code

Tài liệu này mô tả cách các trang trong dự án hoạt động, cách frontend gọi backend và cách dữ liệu đi qua các lớp của hệ thống.

## 1. Kiến trúc tổng thể

```text
Người dùng
   ↓ thao tác trên Vue component
Vue Router + Pinia store
   ↓ axios /api/...
Spring Boot Controller
   ↓ service/repository/JPA
MySQL
```

### Các thư mục chính

| Thư mục | Vai trò |
|---|---|
| `frontend/src/views` | Các trang có route như trang chủ, shop, checkout, admin |
| `frontend/src/components` | Component dùng chung như header, product card, modal |
| `frontend/src/components/admin` | Các module quản trị theo từng tab |
| `frontend/src/stores` | Trạng thái dùng chung: đăng nhập, giỏ hàng, toast, confirm |
| `frontend/src/composables` | Hàm dùng lại cho format tiền, đường dẫn sản phẩm, catalog |
| `frontend/src/router` | Khai báo URL và chặn quyền truy cập |
| `backend/.../controller` | Nhận request HTTP và trả JSON |
| `backend/.../entity` | Entity ánh xạ bảng database |
| `backend/.../repository` | Truy vấn database bằng Spring Data JPA |
| `backend/.../dto` | Định dạng dữ liệu trả về hoặc nhận vào từ frontend |

## 2. Luồng khởi động frontend

1. `main.js` khởi tạo Vue, Pinia, Bootstrap Icons và router.
2. `App.vue` dựng layout chung:
   - page loader khi chuyển trang;
   - header, tìm kiếm, thông báo, tài khoản, giỏ hàng;
   - thanh điều hướng và cửa sổ danh mục;
   - footer;
   - `router-view` để hiển thị trang hiện tại.
3. `router/index.js` quyết định component nào được hiển thị theo URL.
4. Với route yêu cầu đăng nhập hoặc admin, navigation guard kiểm tra `authStore` trước khi cho truy cập.

## 3. Các route phía người dùng

### `/` — Trang chủ (`HomeView.vue`)

- Gọi API banner, sản phẩm nổi bật, sản phẩm sale và danh mục.
- Hiển thị hero banner, danh mục, sản phẩm nổi bật và các nhóm sản phẩm theo danh mục.
- Mỗi nhóm giới hạn số card hiển thị; nếu có thêm sản phẩm thì dùng nút mũi tên để trượt ngang.
- Dùng `ProductCard.vue` để thống nhất giá, giảm giá, đánh giá và nút thêm giỏ.
- Khi click sản phẩm, `productPath()` tạo URL dạng `/product/:slug`, không đưa ID ra thanh địa chỉ.

### `/shop` — Cửa hàng (`ShopView.vue`)

- Tải toàn bộ sản phẩm từ `ProductController`.
- Đọc query trên URL để lọc:
  - `categoryName` hoặc `category`: danh mục;
  - `sale=true`: sản phẩm đang sale;
  - `sort`: mới nhất, giá tăng/giảm, tên A-Z;
  - từ khóa tìm kiếm.
- Bộ lọc chỉ cập nhật dữ liệu hiển thị, không làm mất danh mục đang chọn.
- Danh sách dùng lại `ProductCard.vue`.
- Khi mở sản phẩm rồi quay lại, router dùng `savedPosition` để khôi phục vị trí cuộn trước đó.

### `/product/:slug` — Chi tiết sản phẩm (`ProductDetailView.vue`)

- Lấy sản phẩm bằng slug qua `GET /api/products/slug/{slug}`.
- Tải reviews qua `GET /api/products/{id}/reviews`.
- Tính điểm trung bình, số lượng đánh giá và hiển thị danh sách feedback.
- Hiển thị mô tả rút gọn; nút “Xem thêm” mở toàn bộ mô tả.
- Kiểm tra quyền đánh giá qua `review-eligibility`.
- Người dùng đã mua hàng có thể gửi review bằng `POST /api/products/{id}/reviews`.
- Hiển thị nhóm sản phẩm liên quan theo danh mục, tối đa 5 card và có điều khiển trượt ngang.

### `/cart` — Giỏ hàng (`CartView.vue`)

- Dữ liệu giỏ được quản lý bởi `stores/cart.js`.
- Khi đã đăng nhập, store đồng bộ với `CartController`.
- Có thể tăng/giảm số lượng, xóa từng item hoặc xóa toàn bộ.
- Tổng tiền được tính từ giá biến thể và số lượng hiện tại.

### `/checkout` — Thanh toán (`CheckoutView.vue`)

- Kiểm tra người dùng đã đăng nhập.
- Tải địa chỉ giao hàng và dữ liệu giỏ hàng.
- Cho chọn COD hoặc MOMO.
- Gửi đơn đến `POST /api/orders`.
- Mã giảm giá được validate trước khi tính tổng; giá sau giảm được gửi vào payload thanh toán để tránh lỗi hiển thị đúng nhưng tính tiền sai.
- Sau khi đặt thành công, chuyển sang lịch sử đơn hàng hoặc trang kết quả thanh toán.

### `/account` — Tài khoản (`AccountView.vue`)

- Tải profile, avatar và danh sách địa chỉ.
- Cho chỉnh sửa thông tin cá nhân, upload/crop avatar và lưu ảnh đại diện.
- Form địa chỉ hỗ trợ gợi ý tỉnh/thành phố, quận/huyện, phường/xã từ `GET /api/categories/areas`.
- Các gợi ý có vùng cuộn riêng, tránh làm vỡ layout.
- Có nút đi đến `/orders`, checkout, mua sắm và thêm địa chỉ.
- Map bên dưới hiển thị vị trí dựa trên thông tin địa chỉ đã nhập.

### `/orders` — Lịch sử đơn hàng (`OrderHistoryView.vue`)

- Tải đơn của tài khoản từ `GET /api/orders`.
- Tải chi tiết item của từng đơn từ `/api/orders/{id}/items`.
- Hiển thị trạng thái, phương thức thanh toán, tổng tiền, thông tin nhận hàng và tên sản phẩm.
- Tên sản phẩm có thể click để mở trang chi tiết sản phẩm.
- Đơn chưa thanh toán vẫn có thể xem hóa đơn; hóa đơn hiển thị phương thức thanh toán và trạng thái đã/chưa thanh toán.

### `/login`, `/register`, `/forgot-password`

- `LoginView.vue`: đăng nhập, lưu user/token vào `authStore`.
- `RegisterView.vue`: đăng ký và xác minh email nếu được yêu cầu.
- `ForgotPasswordView.vue`: gửi mã và đặt lại mật khẩu.
- Các route có `guestOnly`; nếu đã đăng nhập thì chuyển về `/account` hoặc `/admin`.

### `/about` và `/chinh-sach-khuyen-mai`

- `AboutView.vue` là trang giới thiệu thương hiệu, quy trình và trải nghiệm mua hàng.
- `PromotionPolicyView.vue` trình bày cách dùng mã, điều kiện áp dụng, thời hạn và trường hợp không áp dụng.
- Đây là các trang nội dung tĩnh, dùng component và style frontend, không cần API bắt buộc.

### Route không tồn tại

`NotFoundView.vue` hiển thị trang 404 tiếng Việt và nút quay về trang chủ hoặc shop.

## 4. Component dùng chung

### `ProductCard.vue`

Đây là component dùng ở trang chủ, shop và sản phẩm liên quan.

- Tính giá gốc, phần trăm giảm và giá sau giảm.
- Hiển thị ảnh, badge sale, wishlist, tên sản phẩm.
- Hiển thị điểm trung bình và số lượng review từ `ratingAverage`, `reviewCount`.
- Số review lớn được rút gọn: `1.2k`, `12.3k`, `1.2m`.
- Nút “Thêm vào giỏ” chọn variant đang hoạt động rồi gọi `cartStore.addItem()`.

### `App.vue`

- Quản lý trạng thái mở/đóng account menu và notification menu.
- Đóng cửa sổ khi click ra ngoài hoặc chuyển trang.
- Header chỉ hiển thị với trang người dùng; admin dùng layout riêng.
- Badge giỏ hàng lấy từ `cartStore.totalCount`.

### `ToastContainer.vue` và `ConfirmModal.vue`

- Toast hiển thị thông báo thành công/lỗi sau các request.
- Confirm modal yêu cầu xác nhận trước các thao tác xóa.

## 5. Pinia stores

### `stores/auth.js`

- Lưu user hiện tại, token và quyền admin.
- Khôi phục phiên đăng nhập khi tải lại trang.
- Cung cấp `isAuthenticated`, `isAdmin` cho router và component.

### `stores/cart.js`

- Lưu các item trong giỏ.
- Tính `totalCount`, `subtotal` và tổng thanh toán.
- Đồng bộ thêm/sửa/xóa item với backend khi người dùng đã đăng nhập.

### `stores/toast.js`

- Nhận message success/error/info.
- Tự động ẩn toast sau một khoảng thời gian.

### `stores/confirm.js`

- Mở hộp thoại xác nhận và trả về `true/false` cho component gọi.

## 6. Các trang admin

Admin dùng một route duy nhất là `/admin`. `AdminDashboardView.vue` giữ `activeTab` và render đúng module trong `components/admin`.

### Phân quyền admin

`router/index.js` kiểm tra:

```js
if (!authStore.isAuthenticated) return next('/login')
if (!authStore.isAdmin) return next('/account')
```

Vì vậy user thường không thể truy cập dashboard quản trị.

### `AdminOverview.vue` — Tổng quan

- Gọi `/api/admin/stats` và `/api/admin/orders`.
- Hiển thị doanh thu, số sản phẩm, user, khuyến mãi, coupon, review, địa chỉ và thanh toán.
- Tính dữ liệu biểu đồ trạng thái đơn hàng và doanh thu ở frontend.
- Có các nút truy cập nhanh đến từng module.

### `AdminOrders.vue` — Quản lý đơn hàng

- Tải danh sách qua `GET /api/admin/orders`.
- Tải item từng đơn qua `/api/admin/orders/{id}/items`.
- Có tìm kiếm, lọc trạng thái, tổng đơn, đơn đang xử lý, đơn hoàn tất và doanh thu.
- Cho cập nhật trạng thái qua `PUT /api/admin/orders/{id}/status`.
- Hiển thị tên sản phẩm trong đơn; click vào tên mở trang sản phẩm.
- Có nút xem chi tiết, xem hóa đơn và in hóa đơn.
- Hóa đơn hiển thị đầy đủ người nhận, địa chỉ, sản phẩm, số lượng, tổng tiền, coupon, phương thức thanh toán và trạng thái thanh toán.

### `AdminProducts.vue` — Sản phẩm

- CRUD sản phẩm qua `/api/admin/products`.
- Quản lý ảnh chính, ảnh phụ và variant.
- Giá hiển thị lấy từ variant hoặc giá sản phẩm.
- Gắn sản phẩm với danh mục chung/danh mục con.
- Có thể thêm promotion cho sản phẩm.

### `AdminCategories.vue` — Danh mục

- Tải danh mục qua `/api/admin/categories`.
- Phân biệt danh mục chung và danh mục con.
- Danh mục con có thể thêm, sửa, xóa.
- Kiểm tra quan hệ sản phẩm trước khi xóa để tránh dữ liệu mồ côi.

### `AdminBanners.vue` — Banner

- CRUD banner trang chủ qua `/api/admin/banners`.
- Quản lý ảnh, tiêu đề, subtitle, link, thứ tự và trạng thái hoạt động.
- Banner hoạt động được frontend dùng trong hero section.

### `AdminPromotions.vue` — Khuyến mãi

- CRUD chương trình khuyến mãi qua `/api/admin/promotions`.
- Gắn promotion với nhiều sản phẩm.
- Frontend dùng phần trăm giảm để tính giá cuối cùng và hiển thị giá cũ.

### `AdminCoupons.vue` — Mã giảm giá

- CRUD coupon qua `/api/admin/coupons`.
- Có mã, loại giảm, giá trị giảm, thời gian, số lượt dùng và trạng thái.
- Checkout gọi `GET /api/coupons/validate` trước khi áp dụng.

### `AdminReviews.vue` — Đánh giá

- CRUD review qua `/api/admin/reviews`.
- Có thống kê tổng review, điểm trung bình, phản hồi tích cực và phân bố sao.
- Hỗ trợ tìm theo user/sản phẩm/nội dung và lọc theo số sao.
- Hiển thị avatar, người đánh giá, ngày tạo, nội dung và sản phẩm liên quan.

### `AdminUsers.vue` — Người dùng

- Tải user qua `/api/admin/users`.
- Sửa thông tin, role, trạng thái và avatar.
- Avatar được preview trước khi lưu và hiển thị đúng tỉ lệ hình tròn.

### `AdminAddresses.vue` — Địa chỉ

- CRUD địa chỉ qua `/api/admin/addresses`.
- Hiển thị người sở hữu, tỉnh/thành, quận/huyện, phường/xã và địa chỉ chi tiết.

### `AdminPayments.vue` — Thanh toán

- Tải dữ liệu qua `/api/admin/payments`.
- Theo dõi phương thức, trạng thái, mã giao dịch và đơn liên quan.
- Cho cập nhật trạng thái khi backend cho phép.

### `AdminDatabase.vue` — Cấu trúc database

- Hiển thị nhóm bảng và quan hệ dữ liệu ở dạng sơ đồ.
- Dùng để tra cứu nhanh cấu trúc: products, variants, categories, orders, reviews, users, payments...

### Xuất SQL và Excel

`AdminDashboardView.vue` có nút SQL và Excel dùng chung cho các tab có dữ liệu:

1. Xác định endpoint tương ứng với `activeTab`.
2. Gọi API admin để lấy danh sách bản ghi.
3. SQL được tạo thành các câu `INSERT INTO`.
4. Excel được tạo dưới dạng bảng HTML có BOM UTF-8 để mở tốt bằng Excel.
5. Frontend tạo `Blob` và tự tải file xuống máy.

## 7. Backend API chính

| Controller | Chức năng |
|---|---|
| `AuthController` | login, register, Google login, email verification, profile, password |
| `ProductController` | danh sách, chi tiết, slug, featured, sale, reviews |
| `CartController` | đọc/thêm/sửa/xóa giỏ hàng |
| `OrderController` | tạo đơn, danh sách đơn user, item đơn |
| `CategoryController` | danh mục và dữ liệu khu vực |
| `BannerController` | banner public |
| `CouponController` | validate mã giảm giá |
| `AdminController` | thống kê và toàn bộ CRUD quản trị |

## 8. Luồng đặt hàng

```text
Chọn sản phẩm
  → chọn variant
  → cartStore.addItem()
  → CartController lưu giỏ
  → Checkout tải giỏ + địa chỉ
  → validate coupon
  → tính subtotal / discount / shipping / total
  → POST /api/orders
  → tạo order + order items + payment
  → hiển thị lịch sử đơn và hóa đơn
```

## 9. Luồng đánh giá sản phẩm

```text
ProductDetailView tải product
  → tải reviews
  → tải review eligibility
  → user gửi rating + comment
  → ProductController kiểm tra user đã mua hàng
  → lưu review
  → cập nhật ratingAverage và reviewCount
  → ProductCard hiển thị sao + số lượt review
```

## 10. Quy tắc khi chỉnh sửa code

- Route mới: thêm vào `frontend/src/router/index.js`.
- API mới: thêm endpoint ở controller tương ứng và gọi bằng axios trong view/component.
- Dữ liệu dùng ở nhiều nơi: ưu tiên Pinia store hoặc composable.
- Không hard-code ID sản phẩm trong link; dùng `productPath()` để tạo URL theo slug.
- Thao tác xóa phải dùng `confirmStore` và hiển thị kết quả bằng `toast`.
- Các card sản phẩm nên dùng `ProductCard.vue` để giữ đồng nhất giá, rating, button và responsive.
- Khi thay đổi database, cần cập nhật đồng thời entity, DTO, repository, controller và tài liệu này.

## 11. Kiểm tra sau khi chỉnh sửa

Frontend:

```powershell
cd frontend
npm run build
```

Backend:

```powershell
cd backend
./mvnw test
```

Nếu dùng Windows và không có `mvnw`, có thể chạy `mvn test` khi Maven đã được cài trong máy.

## 12. Backend Java — kiến trúc xử lý request

Backend nằm trong `backend/src/main/java/com/y2kstore/backend` và dùng Spring Boot. Một request thường đi theo luồng:

```text
HTTP request → JWT filter → Controller → Repository/JPA → DTO → JSON response
```

Controller hiện đang xử lý trực tiếp nghiệp vụ cùng repository; chưa tách thành các service riêng. Những hàm có `@Transactional` gom nhiều thao tác database thành một giao dịch, giúp rollback nếu có lỗi.

### File khởi động và bảo mật

#### `BackendApplication.java`

- `main(String[] args)`: khởi động Spring Boot, datasource, JPA, controller và security.

#### `config/DatabaseCompatibilityInitializer.java`

- `run(ApplicationArguments)`: chạy khi ứng dụng khởi động để tương thích database cũ.
- `addColumnIfMissing(table, column, definition)`: thêm cột nếu chưa tồn tại.
- `dropColumnIfExists(table, column)`: xóa cột cũ nếu tồn tại.
- `ensureRole(roleName)`: bảo đảm role cơ bản đã có trong database.
- `ensureEmailVerificationTable()`: bảo đảm bảng mã xác minh email tồn tại.
- `quote(identifier)`: escape tên bảng/cột khi tạo SQL.

#### `security/JwtTokenProvider.java`

- `generateToken(username, role)`: tạo JWT chứa email và role.
- `doGenerateToken(claims, subject)`: ký JWT và đặt thời hạn.
- `getUsernameFromToken(token)`: lấy email/subject từ token.
- `getExpirationDateFromToken(token)`: lấy thời điểm hết hạn.
- `getClaimFromToken(...)`: đọc một claim bất kỳ.
- `getAllClaimsFromToken(token)`: giải mã và kiểm tra chữ ký.
- `isTokenExpired(token)`: kiểm tra token hết hạn.
- `validateToken(token, userDetails)`: kiểm tra token đúng user và còn hạn.

#### `security/JwtAuthenticationFilter.java`

- `getJwtFromRequest(request)`: đọc `Authorization: Bearer ...`.
- Filter kiểm tra token, gọi `CustomUserDetailsService` và đặt Authentication vào `SecurityContextHolder`.

#### `security/CustomUserDetailsService.java`

- `loadUserByUsername(email)`: lấy user bằng `UserRepository`, chuyển thành `UserDetails` và gắn quyền theo role.

#### `security/SecurityConfig.java`

- `passwordEncoder()`: tạo BCrypt encoder.
- `authenticationManager(...)`: lấy AuthenticationManager của Spring.
- `securityFilterChain(http)`: cấu hình CORS, CSRF, session stateless, endpoint public/admin và JWT filter.
- `corsConfigurationSource()`: cấu hình domain frontend được phép gọi API.

#### `service/EmailService.java`

- `sendPasswordResetCode(...)`: gửi mã đặt lại mật khẩu.
- `sendEmailVerificationCode(...)`: gửi mã xác minh email.
- `send(...)`: hàm gửi email dùng chung, gồm subject, nội dung và thời hạn mã.

## 13. Controller và hàm nghiệp vụ

### `AuthController.java` — `/api/auth`

- `login(AuthRequest)`: xác thực tài khoản, kiểm tra status, tạo JWT và trả `AuthResponse`.
- `register(RegisterRequest)`: kiểm tra email, mã xác minh, mã hóa password, gán role USER và lưu user.
- `requestRegistrationCode(...)`: tạo mã đăng ký, lưu thời hạn và gửi email.
- `googleLogin(GoogleLoginRequest)`: xác minh credential Google, tìm/tạo user và cấp JWT.
- `forgotPassword(...)`: tạo reset token và gửi mã/email.
- `resetPassword(...)`: kiểm tra token còn hạn, mã hóa password mới và xóa reset token.
- `requestEmailVerification()`: tạo mã xác minh cho tài khoản hiện tại.
- `verifyEmail(...)`: kiểm tra code rồi cập nhật `emailVerified`.
- `getProfile()`: trả profile user hiện tại, không trả password.
- `updateProfile(UserDTO)`: cập nhật tên, điện thoại, avatar và thông tin được phép.
- `getMyAddresses()`: lấy địa chỉ của user hiện tại.
- `createMyAddress(AddressDTO)`: tạo địa chỉ, gắn user và xử lý địa chỉ mặc định.
- `updateMyAddress(id, dto)`: cập nhật địa chỉ thuộc user hiện tại.
- `deleteMyAddress(id)`: xóa địa chỉ thuộc user hiện tại.
- `getAuthenticatedUser()`: lấy email từ SecurityContext rồi tìm user.
- `getOrCreateRole(...)`, `isValidVerification(...)`, `applyAddressFields(...)`, `hasCompleteAddress(...)`, `syncDefaultAddress(...)`: các hàm phụ trợ role, mã xác minh và địa chỉ.
- `toProfilePayload(...)`, `toAuthResponse(...)`: chuyển entity/auth data thành JSON trả frontend.
- `verifyGoogleCredential(...)`: đọc và kiểm tra thông tin credential Google.

### `ProductController.java` — `/api/products`

- `getProducts(categoryId, search, minPrice, maxPrice)`: chọn query theo danh mục/từ khóa, chuyển sang DTO và lọc giá.
- `getProductById(id)`: lấy sản phẩm theo ID.
- `getProductBySlug(slug)`: tạo slug từ tên rồi tìm sản phẩm đang bán.
- `slugify(value)`: bỏ dấu tiếng Việt, đổi `đ` thành `d`, chuẩn hóa thành URL slug.
- `getProductReviews(id)`: lấy review mới nhất của sản phẩm.
- `getReviewEligibility(id)`: kiểm tra user đã mua và chưa đánh giá sản phẩm.
- `createProductReview(id, dto)`: kiểm tra quyền mua, chống review trùng, giới hạn rating 1–5 và lưu review.
- `getFeaturedProducts()`: trả tối đa 10 sản phẩm đang bán.
- `getSaleProducts()`: lọc sản phẩm có promotion hiệu lực và trả tối đa 6 sản phẩm.
- `toDTO(product)`: ghép sản phẩm, ảnh, variant, promotion, rating average, review count và sold count.
- `getAuthenticatedUser()`: lấy user từ JWT.
- `findActivePromotion(product)`: tìm promotion bật và còn trong thời gian hiệu lực.

### `CartController.java` — `/api/cart`

- `getCart()`: lấy giỏ của user và tính lại giá sale cho từng item.
- `toDiscountedCartItem(item)`: tạo DTO, tính đơn giá sau promotion và tổng item.
- `discountedPrice(basePrice, productId)`: tìm promotion đang hoạt động và giảm theo phần trăm.
- `addToCart(request)`: kiểm tra variant/tồn kho, cộng item cũ hoặc tạo item mới.
- `updateQuantity(variantId, request)`: kiểm tra item, số lượng và tồn kho trước khi lưu.
- `removeFromCart(variantId)`: xóa một item.
- `clearCart()`: xóa toàn bộ giỏ của user.
- `getAuthenticatedUser()`: xác định chủ giỏ từ JWT.

### `OrderController.java` — `/api/orders`

- `getMyOrders()`: lấy các đơn của user theo thời gian giảm dần.
- `getOrderItems(id)`: kiểm tra đơn thuộc user hoặc admin rồi trả item.
- `placeOrder(request)`: kiểm tra giỏ, địa chỉ, tồn kho; trừ kho; tính promotion sản phẩm; tính coupon; lưu order, order item, payment; xóa giỏ và trả tổng tiền.
- `resolveAddress(user, addressId)`: chỉ chọn địa chỉ của user, ưu tiên địa chỉ mặc định nếu không truyền ID.
- `resolveCoupon(code)`: kiểm tra mã tồn tại, status, hạn coupon và promotion liên quan.
- `resolveCouponDiscount(coupon, subtotal)`: tính giảm cố định hoặc phần trăm, không vượt subtotal.
- `discountedPrice(basePrice, productId)`: tính giá sau promotion ở bước tạo đơn.
- `getAuthenticatedUser()`: lấy user hiện tại.

### `AdminController.java` — `/api/admin`

#### Thống kê, đơn hàng và sản phẩm

- `getStats()`: tổng hợp doanh thu, đơn, user, sản phẩm, tồn kho thấp, danh mục, banner, promotion, coupon, review, địa chỉ và payment.
- `getAllOrders()`, `getOrderItems(id)`: lấy danh sách đơn và sản phẩm trong từng đơn.
- `updateOrderStatus(id, body)`: cập nhật trạng thái đơn.
- `getAllProducts()`: lấy toàn bộ sản phẩm dạng DTO.
- `createProduct(dto)`, `updateProduct(id, dto)`, `deleteProduct(id)`: CRUD sản phẩm.
- `createProductImage(id, dto)`, `deleteProductImage(productId, imageId)`: CRUD ảnh sản phẩm.
- `createProductVariant(id, dto)`, `deleteProductVariant(productId, variantId)`: CRUD size/màu/giá/tồn kho.
- `updateProductPromotions(id, body)`: đồng bộ danh sách promotion của sản phẩm.

#### User, danh mục, banner, khuyến mãi

- `getAllUsers()`, `updateUser(id, dto)`, `deleteUser(id)`: quản lý tài khoản, role, status và avatar.
- `getAllCategories()`, `createCategory(dto)`, `updateCategory(id, dto)`, `deleteCategory(id)`: quản lý danh mục chung, danh mục con và parent.
- `getAllBanners()`, `createBanner(dto)`, `updateBanner(id, dto)`, `deleteBanner(id)`: CRUD banner.
- `getAllPromotions()`, `createPromotion(dto)`, `updatePromotion(id, dto)`, `deletePromotion(id)`: CRUD promotion.
- `updatePromotionProducts(id, body)`: đồng bộ sản phẩm thuộc promotion.
- `getAllCoupons()`, `createCoupon(dto)`, `updateCoupon(id, dto)`, `deleteCoupon(id)`: CRUD coupon.

#### Review, địa chỉ, thanh toán

- `getAllReviews()`, `createReview(dto)`, `updateReview(id, dto)`, `deleteReview(id)`: quản trị đánh giá.
- `getAllAddresses()`, `createAddress(dto)`, `updateAddress(id, dto)`, `deleteAddress(id)`: quản trị địa chỉ toàn hệ thống.
- `getAllPayments()`, `updatePayment(id, dto)`: xem và cập nhật payment.

#### Hàm private hỗ trợ AdminController

- `getCurrentUser()`: lấy admin hiện tại từ SecurityContext.
- `toDTO(product)`: ghép product với ảnh, variant, category, promotion, rating và giá.
- `applyProductCategories(...)`, `applyCategoryParent(...)`, `normalizeCategoryType(...)`: xử lý quan hệ danh mục.
- `syncProductPromotions(...)`, `syncPromotionProducts(...)`: đồng bộ hai chiều product–promotion.
- `findActivePromotion(...)`: tìm promotion đang hiệu lực.
- `applyBannerFields(...)`, `applyPromotionFields(...)`, `applyCouponFields(...)`, `applyReviewFields(...)`, `applyAddressFields(...)`, `applyPaymentFields(...)`, `applyVariantFields(...)`: copy dữ liệu từ DTO vào entity.
- `syncDefaultAddress(...)`: bảo đảm mỗi user chỉ có một địa chỉ mặc định.

### Các controller nhỏ

#### `CategoryController.java` — `/api/categories`

- `getAllCategories()`: trả danh mục public cho menu và bộ lọc.
- `getVietnameseAreas()`: gọi API khu vực Việt Nam và trả tỉnh/huyện/xã cho form địa chỉ.

#### `BannerController.java` — `/api/banners`

- `getActiveBanners()`: lấy banner bật theo thứ tự/ngày hiển thị.

#### `CouponController.java` — `/api/coupons`

- `validateCoupon(code)`: tìm coupon, kiểm tra trạng thái, hạn và promotion đi kèm rồi trả discount hợp lệ.

#### `AppConfigController.java` — `/api/config`

- `getConfig()`: đọc cấu hình runtime, ví dụ thời gian loader trong `application.properties`, và trả về frontend.

## 14. DTO — công dụng từng file Java

DTO là object trung gian, không trực tiếp đại diện bảng database. Constructor nhận entity dùng để tạo JSON response; getter/setter dùng để đọc ghi field.

| File | Công dụng |
|---|---|
| `AddressDTO` | id, user, tỉnh, huyện, xã, địa chỉ chi tiết, mặc định |
| `AdminStatsDTO` | số liệu dashboard admin |
| `AuthRequest` | email/password đăng nhập |
| `AuthResponse` | JWT, user, role, avatar, trạng thái email |
| `BannerDTO` | ảnh, tiêu đề, subtitle, button, thứ tự, status, sản phẩm |
| `CartAddRequest` | variantId và quantity thêm giỏ |
| `CartUpdateRequest` | quantity mới |
| `CartItemDTO` | product, variant, giá, tổng tiền, tồn kho |
| `CategoryDTO` | category, parent, loại, status, số sản phẩm |
| `CouponDTO` | mã, promotion, giá trị giảm, hạn, status |
| `ForgotPasswordRequest` | email quên mật khẩu |
| `GoogleLoginRequest` | credential Google |
| `OrderDTO` | đơn, user, coupon, shipping, payment, tổng tiền |
| `OrderItemDTO` | product, variant, size, màu, quantity, giá |
| `OrderItemRequest` | một sản phẩm trong request đặt hàng |
| `OrderRequest` | địa chỉ, coupon, phương thức thanh toán |
| `PaymentDTO` | method, amount, ngày và status payment |
| `ProductDTO` | product, ảnh, variant, category, promotion, rating, sold count |
| `ProductImageDTO` | URL ảnh sản phẩm |
| `ProductVariantDTO` | size, color, stock, price, status |
| `PromotionDTO` | tên, phần trăm giảm, ngày bắt đầu/kết thúc, status |
| `RegisterRequest` | thông tin đăng ký và mã xác minh |
| `ResetPasswordRequest` | reset token và password mới |
| `ReviewDTO` | user, avatar, product, rating, comment, createdAt |
| `UserDTO` | profile, role, status, avatar, provider, ngày tạo |

## 15. Entity — công dụng từng file Java

Entity được JPA ánh xạ với các bảng database:

| File | Bảng/nghiệp vụ |
|---|---|
| `User` | tài khoản, password, role, avatar, email verification, reset password |
| `Role` | quyền USER/ADMIN |
| `EmailVerification` | mã xác minh và thời hạn |
| `Address` | địa chỉ giao hàng |
| `Product` | tên, giá, mô tả, ảnh chính, status, category |
| `ProductImage` | ảnh phụ sản phẩm |
| `ProductVariant` | size, màu, giá, tồn kho |
| `Category` | danh mục chung/con và parent |
| `ProductPromotion` | liên kết nhiều-nhiều product–promotion, có khóa ghép |
| `Promotion` | chương trình giảm và thời gian hiệu lực |
| `Coupon` | mã giảm giá, giá trị và hạn |
| `CartItem` | variant và số lượng trong giỏ |
| `Order` | user, coupon, địa chỉ, tổng, status |
| `OrderItem` | variant, quantity và giá tại lúc mua |
| `Payment` | phương thức, số tiền, ngày, status |
| `Review` | user, product, rating, comment, ngày tạo |

Các getter/setter của entity phục vụ JPA và controller. `Order` có thêm các hàm dẫn xuất như `getPaymentMethod()`, `getPaymentStatus()`, `getFullName()` để đọc dữ liệu từ quan hệ payment/user/address.

## 16. Repository — cách truy vấn database

Repository kế thừa Spring Data JPA. Spring phân tích tên hàm để tự tạo SQL; các hàm có `@Query` dùng truy vấn tùy chỉnh.

- `UserRepository`: `findByEmail`, `existsByEmail`, `findByResetToken`.
- `RoleRepository`: `findByRoleName`.
- `EmailVerificationRepository`: tìm mã theo email và purpose.
- `AddressRepository`: tìm địa chỉ theo user, địa chỉ mặc định và đếm địa chỉ.
- `ProductRepository`: tìm theo category, parent/con category, tên, status, kết hợp category + tên và đếm sản phẩm.
- `ProductImageRepository`: `findByProductId` lấy ảnh theo product.
- `ProductVariantRepository`: `findByProductId`, `countByStockLessThan` lấy variant và đếm hàng sắp hết.
- `CategoryRepository`: đếm tên, lấy category active và kiểm tra category có category con.
- `ProductPromotionRepository`: lấy liên kết theo product/promotion và đếm liên kết.
- `PromotionRepository`: tìm promotion theo tên và lấy danh sách mới nhất.
- `CouponRepository`: tìm coupon theo mã, lấy danh sách và đếm coupon theo promotion.
- `CartItemRepository`: tìm giỏ theo user/variant, kiểm tra tồn tại, xóa một hoặc toàn bộ giỏ.
- `OrderRepository`: lấy đơn theo user/toàn hệ thống, đếm đơn theo status hoặc coupon.
- `OrderItemRepository`: lấy item theo đơn, tổng số lượng đã bán, kiểm tra user đã mua sản phẩm.
- `PaymentRepository`: lấy payment theo ngày giảm dần.
- `ReviewRepository`: lấy review theo product, đếm, tính điểm trung bình và kiểm tra review trùng.
- `BannerRepository`: lấy banner active hoặc toàn bộ theo ngày.

## 17. Luồng giảm giá và thanh toán ở backend

Backend luôn tính lại giá, không tin giá tổng do frontend gửi:

1. `ProductController.toDTO()` tính promotion để hiển thị card sản phẩm.
2. `CartController.discountedPrice()` tính giá sale trong giỏ.
3. `OrderController.discountedPrice()` tính lại giá ở thời điểm tạo đơn.
4. `resolveCouponDiscount()` áp dụng coupon trên subtotal sau promotion sản phẩm.
5. `Order.totalAmount` và `Payment.amount` lưu tổng tiền cuối cùng sau giảm.

Luồng đặt hàng:

```text
CartItem → kiểm tra variant/tồn kho → trừ kho
         → tính giá promotion → tính subtotal
         → kiểm tra coupon → tính totalAmount
         → lưu Order + OrderItem + Payment
         → xóa giỏ hàng → trả orderId
```

## 18. Quyền truy cập API

- Public: sản phẩm, chi tiết, banner, danh mục, validate coupon.
- User: profile, địa chỉ, cart, order, review; cần JWT.
- Admin: `/api/admin/**`; cần JWT có role `ROLE_ADMIN`.
- Backend lấy user từ email trong JWT, không lấy `userId` tùy ý từ frontend cho nghiệp vụ cần bảo mật.

## 19. Cách đọc một hàm backend

Ví dụ `OrderController.placeOrder()`:

1. Annotation `@PostMapping` xác định URL HTTP.
2. `@RequestBody OrderRequest` nhận JSON từ frontend và chuyển thành Java object.
3. `getAuthenticatedUser()` lấy tài khoản từ JWT.
4. Repository đọc cart, variant, coupon và address.
5. Entity được cập nhật bằng setter.
6. `save()`/`saveAll()` ghi entity vào database.
7. `ResponseEntity` tạo HTTP status và JSON response.
8. `@Transactional` bảo đảm toàn bộ hàm được commit hoặc rollback cùng nhau.

Với các hàm CRUD admin, quy trình tương tự: đọc DTO → tìm entity → copy field → lưu/xóa → trả DTO hoặc message.

