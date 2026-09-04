# Phương thức hiển thị sản phẩm và method trong code Y2K Store

Tài liệu này mô tả các nguồn dữ liệu, quy tắc xếp hạng và vị trí hiển thị sản phẩm ở trang mua hàng và trang admin.

## 1. Nguyên tắc dữ liệu

- Chỉ sản phẩm có `products.status = 1` mới được đưa vào các khu sản phẩm công khai.
- Sản phẩm bán chạy được tính từ `order_details.quantity`.
- Các chi tiết thuộc đơn có `orders.status = 'CANCELLED'` không được tính vào số lượt mua.
- Điểm đánh giá trung bình lấy từ `reviews.rating`.
- Số lượng đánh giá lấy từ số bản ghi trong `reviews` của từng sản phẩm.
- Chế độ `top-rated` chỉ lấy sản phẩm đã có ít nhất một đánh giá, không đưa sản phẩm chưa có review vào bảng xếp hạng.
- Dữ liệu sản phẩm vẫn trả về `soldCount`, `reviewCount` và `ratingAverage` để frontend có thể hiển thị nhãn, sao và bộ lọc.

## 2. API xếp hạng sản phẩm

### `GET /api/products/featured`

Tham số:

| Tham số | Giá trị | Ý nghĩa |
|---|---|---|
| `type` | `best-selling` | Xếp theo số lượng đã mua giảm dần |
| `type` | `top-rated` | Xếp theo điểm trung bình giảm dần |
| `type` | `new` | Chỉ lấy sản phẩm có `createdAt` trong 14 ngày gần nhất, mới nhất trước |
| `type` | `combined` | Trộn luân phiên danh sách bán chạy và đánh giá cao |
| `limit` | `1` đến `24` | Số sản phẩm tối đa trả về, mặc định `10` |

Ví dụ:

```text
/api/products/featured?type=best-selling&limit=10
/api/products/featured?type=top-rated&limit=10
/api/products/featured?type=new&limit=10
/api/products/featured?type=combined&limit=10
```

### Quy tắc sắp xếp

#### `best-selling`

1. `soldCount` giảm dần.
2. `ratingAverage` giảm dần.
3. `reviewCount` giảm dần.
4. `id` tăng dần để kết quả ổn định khi các chỉ số bằng nhau.

#### `top-rated`

1. `ratingAverage` giảm dần.
2. `reviewCount` giảm dần, giúp ưu tiên sản phẩm có nhiều phản hồi hơn khi cùng điểm.
3. `soldCount` giảm dần.
4. `id` tăng dần để kết quả ổn định.

#### `new`

1. Chỉ lấy sản phẩm có `products.created_at >= hiện tại - 14 ngày`.
2. `createdAt` giảm dần.
3. `id` giảm dần để kết quả ổn định khi thời gian tạo bằng nhau.

#### `combined`

Danh sách lấy luân phiên một sản phẩm từ `best-selling` và một sản phẩm từ `top-rated`, loại trùng theo `product.id`. Chế độ này dùng cho các vị trí liên quan hoặc tương thích ngược với frontend cũ.

## 3. Sản phẩm hiển thị ở trang mua hàng

### Trang chủ - `frontend/src/views/HomeView.vue`

| Khu vực | API | Số lượng | Sản phẩm hiển thị |
|---|---|---:|---|
| **Bán chạy nhất** | `/api/products/featured?type=best-selling&limit=10` | 10 | Các sản phẩm có tổng số lượng mua cao nhất, không tính đơn hủy |
| **Được đánh giá cao** | `/api/products/featured?type=top-rated&limit=10` | 10 | Các sản phẩm có điểm đánh giá trung bình cao nhất và nhiều đánh giá hơn khi đồng điểm |
| **Hàng mới** | `/api/products/featured?type=new&limit=10` | 10 | Các sản phẩm được tạo trong 14 ngày gần nhất, có nhãn `NEW` trên card |
| Hàng theo danh mục | `/api/products` | Tối đa 5 mỗi danh mục | Sản phẩm đang bán thuộc từng danh mục |
| Sale off | `/api/products` + lọc khuyến mãi | Theo catalog | Sản phẩm đang có phần trăm khuyến mãi |

Cả hai khu nổi bật dùng chung `ProductCard`, nên người dùng thấy ảnh, giá, sao, số lượt đánh giá và nút thêm vào giỏ theo cùng một kiểu giao diện.

### Trang shop - `frontend/src/views/ShopView.vue`

Shop vẫn lấy catalog từ `/api/products` để giữ bộ lọc không tải lại toàn trang. Các lựa chọn sắp xếp hiện có:

- `Mặc định`: giữ thứ tự backend trả về.
- `Hàng mới`: chỉ hiện sản phẩm được tạo trong 14 ngày gần nhất, ưu tiên `createdAt` mới hơn.
- `Bán chạy nhất`: ưu tiên `soldCount`, sau đó `ratingAverage`.
- `Đánh giá cao`: ưu tiên `ratingAverage`, sau đó `reviewCount` và `soldCount`.
- `Giá tăng dần` và `Giá giảm dần`.
- `Tên A-Z`.

### Trang chi tiết sản phẩm

`ProductDetailView.vue` dùng `/api/products/featured` ở phần sản phẩm liên quan. Khi không truyền `type`, backend dùng `combined` để không chỉ lặp lại các sản phẩm bán chạy.

## 4. Sản phẩm hiển thị ở trang admin

### Dashboard admin - `frontend/src/components/admin/AdminOverview.vue`

| Khu vực | API / nguồn | Sản phẩm hiển thị |
|---|---|---|
| Biểu đồ **Sản phẩm bán chạy** | `/api/admin/orders` và `/api/admin/orders/{id}/items` | Top 5 theo số lượng trong các đơn thuộc khoảng ngày/tuần/tháng đang chọn; đơn hủy bị loại |
| Bảng **Sản phẩm mua nhiều nhất** | `/api/products/featured?type=best-selling&limit=5` | Top 5 toàn hệ thống theo số lượng mua không hủy |
| Bảng **Sản phẩm được đánh giá cao** | `/api/products/featured?type=top-rated&limit=5` | Top 5 toàn hệ thống theo điểm trung bình, số đánh giá và số lượt mua |

Biểu đồ theo khoảng thời gian trả lời câu hỏi “đang bán tốt trong kỳ nào”; hai bảng featured trả lời câu hỏi “toàn hệ thống sản phẩm nào đang có vị trí nổi bật”. Hai loại dữ liệu được giữ riêng để admin không nhầm ranking theo kỳ với ranking toàn thời gian.

### Quản lý sản phẩm admin - `frontend/src/components/admin/AdminProducts.vue`

Màn hình này hiển thị toàn bộ sản phẩm từ `/api/admin/products`, gồm cả nháp, hết hàng, sắp về và ẩn để admin quản trị dữ liệu. Đây không phải danh sách featured công khai. Admin có thể xem và chỉnh:

- `products` và danh mục.
- `product_variants` và tồn kho.
- `product_images`.
- `product_promotions`.
- Trạng thái sản phẩm.

## 5. Xóa đơn trong lịch sử người dùng

### API `DELETE /api/orders/{id}`

- Chỉ chủ sở hữu đơn mới được xóa.
- Chỉ chấp nhận trạng thái `DELIVERED` hoặc `CANCELLED`.
- Đơn `PENDING`, `PROCESSING`, `SHIPPED` không có nút xóa và API cũng từ chối.
- Khi xóa, backend không xóa vật lý dữ liệu. API đặt `orders.hidden_from_history = true` để ẩn đơn khỏi lịch sử của người dùng.
- Chi tiết đơn, thanh toán và trạng thái vẫn được giữ lại để admin, doanh thu và ranking sản phẩm không bị sai lệch.
- Đây là xóa khỏi lịch sử tài khoản, không phải xóa sản phẩm khỏi catalog hay xóa dữ liệu nghiệp vụ.

### Giao diện `frontend/src/views/OrderHistoryView.vue`

- Nút **Xóa khỏi lịch sử** chỉ xuất hiện ở đơn đã giao hoặc đã hủy.
- Có modal xác nhận trước thao tác.
- Nút được khóa trong lúc gọi API.
- Danh sách, bộ đếm trạng thái và dữ liệu chi tiết cập nhật tại chỗ, không tải lại trang.

## 6. Tóm tắt phương thức và vị trí

| Phương thức | Nguồn chính | Vị trí storefront | Vị trí admin |
|---|---|---|---|
| Bán chạy toàn thời gian | `OrderItemRepository.sumQuantityByProductId` | Trang chủ, shop sort | Dashboard featured best sellers |
| Đánh giá cao | `ReviewRepository.averageRatingByProductId` + `countByProductId` | Trang chủ, shop sort | Dashboard featured top rated |
| Bán chạy theo kỳ | Order admin + order items | Không hiển thị mặc định | Dashboard Product pulse |
| Catalog theo bộ lọc | `/api/products` | Shop | Admin Products dùng endpoint admin riêng |
| Sản phẩm sale | Promotion fields trong `ProductDTO` | Shop sale và sidebar | Admin Products / Promotions |
| Sản phẩm liên quan | Featured `combined` | Trang chi tiết | Không dùng làm dữ liệu quản trị |

## 7. Các method có thể tra trong code repository

Phần này là bản đồ từ method/API trong source code đến nơi hiển thị. Đây là cách kiểm tra chính xác một sản phẩm được lấy từ đâu, được tính chỉ số nào và được render ở màn hình nào.

### 7.1 Backend: method lấy và tính dữ liệu sản phẩm

| Method | File | Dữ liệu trả về / tác dụng | Nơi dùng |
|---|---|---|---|
| `ProductController.getProducts(categoryId, search, minPrice, maxPrice)` | `backend/src/main/java/com/y2kstore/backend/controller/ProductController.java` | Catalog sản phẩm đang bán; lọc category, keyword và khoảng giá trước khi trả `ProductDTO` | Shop, sản phẩm theo danh mục |
| `ProductController.getFeaturedProducts(type, limit)` | Cùng file | Xếp hạng `best-selling`, `top-rated`, `new` hoặc `combined`; giới hạn từ 1 đến 24 | Trang chủ, sản phẩm liên quan, dashboard admin |
| `ProductController.getSaleProducts()` | Cùng file | Lọc sản phẩm đang bán có `promotionDiscountPercent > 0` | Khu sale storefront |
| `ProductController.toDTO(product)` | Cùng file | Ghép ảnh, biến thể, khuyến mãi, `soldCount`, `reviewCount`, `ratingAverage`, `createdAt` | Tất cả API sản phẩm của controller |
| `ProductController.findActivePromotion(product)` | Cùng file | Chỉ lấy promotion đang bật và nằm trong thời gian hiệu lực | Giá sale và thông tin promotion |
| `OrderItemRepository.sumQuantityByProductId(productId)` | `backend/src/main/java/com/y2kstore/backend/repository/OrderItemRepository.java` | Tổng `order_details.quantity` theo product; loại đơn `CANCELLED` | `toDTO`, ranking bán chạy |
| `ReviewRepository.countByProductId(productId)` | `backend/src/main/java/com/y2kstore/backend/repository/ReviewRepository.java` | Số review của product | `toDTO`, tie-break top-rated |
| `ReviewRepository.averageRatingByProductId(productId)` | Cùng file | Điểm trung bình từ `reviews.rating` | `toDTO`, ranking top-rated |
| `ProductRepository.findByStatus(1)` | `backend/src/main/java/com/y2kstore/backend/repository/ProductRepository.java` | Chỉ lấy product đang bán để không hiện sản phẩm ẩn/ngừng bán ở storefront | Catalog và featured |

`getFeaturedProducts` hiện lấy toàn bộ product đang bán, gọi `toDTO` để có đủ chỉ số, rồi sắp xếp. Vì vậy `top-rated` không bị giới hạn nhầm trong nhóm bán chạy. Sản phẩm chưa có review bị loại khỏi `top-rated`.

### 7.2 Backend: method liên quan lịch sử đơn

| Method | File | Vai trò |
|---|---|---|
| `OrderController.getMyOrders()` | `backend/src/main/java/com/y2kstore/backend/controller/OrderController.java` | Gọi `findByUserIdAndHiddenFromHistoryFalseOrderByOrderDateDesc`, chỉ trả các đơn chưa bị người dùng ẩn |
| `OrderController.getOrderItems(orderId)` | Cùng file | Lấy chi tiết sản phẩm, biến thể, số lượng và giá của một đơn |
| `OrderController.deleteHistoryOrder(orderId)` | Cùng file | Chỉ cho chủ đơn ẩn đơn `DELIVERED`/`CANCELLED`; cập nhật `hidden_from_history`, không xóa dữ liệu nghiệp vụ |
| `OrderRepository.findByUserIdAndHiddenFromHistoryFalseOrderByOrderDateDesc(userId)` | `backend/src/main/java/com/y2kstore/backend/repository/OrderRepository.java` | Bộ lọc server cho lịch sử người dùng |
| `AdminController.getAllOrders()` | `backend/src/main/java/com/y2kstore/backend/controller/AdminController.java` | Admin vẫn lấy toàn bộ đơn, kể cả đơn người dùng đã ẩn khỏi lịch sử |
| `AdminController.getOrderItems(orderId)` | Cùng file | Admin xem lại toàn bộ item của đơn để dashboard, chi tiết và hóa đơn |

### 7.3 Frontend storefront: method gọi API và render

| Method / computed | File | Hiển thị |
|---|---|---|
| `fetchFeaturedProducts()` | `frontend/src/views/HomeView.vue` | Gọi `GET /api/products/featured?type=best-selling&limit=10`, render section **Bán chạy nhất** bằng `ProductCard` |
| `fetchTopRatedProducts()` | Cùng file | Gọi `GET /api/products/featured?type=top-rated&limit=10`, render section **Được đánh giá cao** |
| `fetchAllProducts()` + `categoryRows` | Cùng file | Lấy catalog và chia tối đa 5 sản phẩm cho từng category row |
| `fetchProducts()` | `frontend/src/views/ShopView.vue` | Gọi `/api/products` theo bộ lọc đã áp dụng; không reload toàn trang |
| `applyFilters()` | Cùng file | Đưa draft filter vào URL bằng `router.push`, sau đó watcher gọi lại API |
| `filteredProducts` | Cùng file | Lọc sale tại client sau khi dữ liệu catalog trả về |
| `sortedProducts` | Cùng file | Sắp xếp mặc định, hàng mới, bán chạy, rating, giá và tên; không gọi API thêm |
| `ProductCard` | `frontend/src/components/ProductCard.vue` | Component hiển thị ảnh, tên, giá, promotion, sao/review và thao tác giỏ hàng dùng chung |
| `ProductDetailView` related products | `frontend/src/views/ProductDetailView.vue` | Gọi `/api/products/featured` ở chế độ mặc định `combined` để hiển thị sản phẩm liên quan |

### 7.4 Frontend admin: method và vị trí hiển thị

| Method / computed | File | Hiển thị |
|---|---|---|
| `fetchStats()` | `frontend/src/components/admin/AdminOverview.vue` | Lấy stats, orders, top 5 best-selling và top 5 top-rated cho dashboard |
| `statusChart` | Cùng file | Biểu đồ số đơn theo `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`; click mỗi dòng mở màn quản lý đơn với status tương ứng |
| `revenuePeriod`, `revenueSeries`, `revenueTrend` | Cùng file | Doanh thu theo ngày/tuần/tháng, chỉ loại đơn hủy khỏi doanh thu |
| `topProducts` | Cùng file | Top sản phẩm theo item của các đơn trong kỳ đang chọn |
| `featuredBestSelling`, `featuredTopRated` | Cùng file | Hai danh sách featured toàn thời gian, tách khỏi ranking theo kỳ |
| `handleNavigate(payload)` | `frontend/src/views/AdminDashboardView.vue` | Nhận `{ tab: 'orders', status }` từ biểu đồ và truyền `initialStatus` cho `AdminOrders` |
| `filteredOrders` | `frontend/src/components/admin/AdminOrders.vue` | Lọc danh sách admin theo status và keyword |
| `getAllProducts()` | `AdminController.java` + `AdminProducts.vue` | Hiển thị toàn bộ catalog quản trị, gồm sản phẩm ẩn/hết hàng; không phải danh sách featured |

### 7.5 Chuỗi dữ liệu thực tế

```text
orders + order_details + reviews
        ↓
OrderItemRepository.sumQuantityByProductId
ReviewRepository.countByProductId / averageRatingByProductId
        ↓
ProductController.toDTO
        ↓
GET /api/products/featured
        ↓
HomeView / ShopView / ProductDetailView / AdminOverview
```

Đối với lịch sử đơn:

```text
OrderHistoryView.fetchOrders
        ↓ GET /api/orders
OrderHistoryView.fetchOrderItems
        ↓ GET /api/orders/{id}/items
Card chi tiết: item + thanh toán + người nhận + địa chỉ
        ↓ DELETE khi đã giao/đã hủy
hidden_from_history = true, không reload trang
```

## 8. Lưu ý khi mở rộng

- Nếu cần chống việc một sản phẩm chỉ có một đánh giá 5 sao đứng quá cao, có thể thêm ngưỡng tối thiểu `reviewCount` cho `top-rated` hoặc dùng Bayesian rating.
- Nếu cần phân biệt “đã mua” với “đã giao thành công”, thay điều kiện loại đơn hủy bằng chỉ tính `DELIVERED` ở truy vấn sold count.
- Khi thêm cache cho featured, cần làm mới cache sau khi tạo đơn, hủy đơn hoặc tạo/xóa đánh giá.
