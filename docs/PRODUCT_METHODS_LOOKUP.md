# Product Methods Lookup - Y2K Store

Tai lieu nay ghi lai cac method Java backend va frontend dang lay, tinh, sap xep va hien thi san pham ban chay, danh gia cao, hang moi, sale va san pham noi bat.

## 1. Java backend

| Muc dich | Method / code | Vi tri |
|---|---|---|
| Lay catalog dang ban | `ProductController.getProducts(categoryId, search, minPrice, maxPrice)` | `backend/src/main/java/com/y2kstore/backend/controller/ProductController.java:67` |
| Lay san pham theo ID | `ProductController.getProductById(id)` | `.../ProductController.java:100` |
| Lay san pham theo slug | `ProductController.getProductBySlug(slug)` | `.../ProductController.java:107` |
| API ban chay / rating / hang moi | `ProductController.getFeaturedProducts(type, limit)` | `.../ProductController.java:169` |
| API san pham sale | `ProductController.getSaleProducts()` | `.../ProductController.java:225` |
| Tao payload san pham | `ProductController.toDTO(product)` | `.../ProductController.java:234` |
| Tim promotion dang hieu luc | `ProductController.findActivePromotion(product)` | `.../ProductController.java:258` |
| Tinh so luong da ban | `OrderItemRepository.sumQuantityByProductId(productId)` | `backend/src/main/java/com/y2kstore/backend/repository/OrderItemRepository.java:17` |
| Dem review | `ReviewRepository.countByProductId(productId)` | `backend/src/main/java/com/y2kstore/backend/repository/ReviewRepository.java:22` |
| Tinh diem review trung binh | `ReviewRepository.averageRatingByProductId(productId)` | `.../ReviewRepository.java:27` |
| Chi lay san pham dang ban | `ProductRepository.findByStatus(status)` | `backend/src/main/java/com/y2kstore/backend/repository/ProductRepository.java:46` |

### API featured

```text
GET /api/products/featured?type=best-selling&limit=10
GET /api/products/featured?type=top-rated&limit=10
GET /api/products/featured?type=new&limit=10
GET /api/products/featured?type=combined&limit=10
```

- `best-selling`: `soldCount`, sau do `ratingAverage`, `reviewCount`, `id` giam/asc theo comparator tai `ProductController.java:178-182`.
- `top-rated`: `ratingAverage`, sau do `reviewCount`, `soldCount`, `id`; loai san pham chua co review. Code tai `ProductController.java:183-196`.
- `new`: `createdAt` trong 14 ngay gan nhat, moi nhat truoc. Code tai `ProductController.java:197-201`.
- `combined`: tron luan phien best-selling va top-rated, loai ID trung. Code tai `ProductController.java:217-223`.
- `limit` duoc gioi han tu 1 den 24 tai `ProductController.java:173`.

`ProductController.toDTO()` gan cac chi so:

```text
reviewCount   = reviewRepository.countByProductId(product.id)
ratingAverage = reviewRepository.averageRatingByProductId(product.id)
soldCount     = orderItemRepository.sumQuantityByProductId(product.id)
createdAt     = product.createdAt
```

`sumQuantityByProductId()` loai don co `order.status = 'CANCELLED'`, nen don huy khong lam tang ranking ban chay.

## 2. Frontend storefront

| Trang / muc hien thi | Method / computed | API va vi tri |
|---|---|---|
| Trang chu - Ban chay nhat | `fetchFeaturedProducts()` | `frontend/src/views/HomeView.vue:288-298`, `/api/products/featured?type=best-selling&limit=10` |
| Trang chu - Duoc danh gia cao | `fetchTopRatedProducts()` | `frontend/src/views/HomeView.vue:300-310`, `/api/products/featured?type=top-rated&limit=10` |
| Trang chu - theo danh muc | `fetchAllProducts()` + `categoryRows` | `frontend/src/views/HomeView.vue:258-262,312-319`, `/api/products` |
| Trang Hang moi | `onMounted()` | `frontend/src/views/NewArrivalsView.vue:82-95`, `/api/products/featured?type=new&limit=24` |
| Shop - lay catalog theo filter | `fetchProducts()` | `frontend/src/views/ShopView.vue:350-371`, `/api/products` voi category/search/price |
| Shop - loc sale / hang moi | `filteredProducts` | `frontend/src/views/ShopView.vue:247-256` |
| Shop - sap xep ban chay / rating | `sortedProducts` | `frontend/src/views/ShopView.vue:258-276` |
| Shop - ap dung filter khong reload | `applyFilters()` + route watcher | `frontend/src/views/ShopView.vue:242-245,373-390` |
| Chi tiet - lay theo slug | Product detail request | `frontend/src/views/ProductDetailView.vue:270-274` |
| Chi tiet - san pham lien quan | related products request | `frontend/src/views/ProductDetailView.vue:294-300` |
| Chuan hoa DTO | `normalizeProduct(raw)` | `frontend/src/composables/useCatalog.js:62-137` |
| Render card dung chung | `ProductCard` | `frontend/src/components/ProductCard.vue:1-94` |

### Shop sort

- `new`: loc `isNewProduct()` va sap xep `createdAt` moi nhat.
- `best-selling`: sap xep `soldCount`, sau do `ratingAverage`.
- `rating`: sap xep `ratingAverage`, sau do `reviewCount`, `soldCount`.
- `price-asc`, `price-desc`, `name-asc`: sap xep tai client, khong co API ranking rieng.

## 3. Frontend admin

| Muc dich | Method / computed | Vi tri |
|---|---|---|
| Lay du lieu dashboard | `fetchStats()` | `frontend/src/components/admin/AdminOverview.vue:400-429` |
| Featured ban chay toan he thong | request `type=best-selling, limit=5` | `.../AdminOverview.vue:405-407` |
| Featured danh gia cao toan he thong | request `type=top-rated, limit=5` | `.../AdminOverview.vue:405-407` |
| Render list ban chay | `featuredBestSelling` | `.../AdminOverview.vue:172-186` |
| Render list danh gia cao | `featuredTopRated` | `.../AdminOverview.vue:190-204` |
| San pham ban chay theo ngay/tuan/thang | `topProducts` | `.../AdminOverview.vue:379-397` |
| Chon ky doanh thu | `revenuePeriod`, `revenueSeries` | `.../AdminOverview.vue:235-337` |
| Quan ly toan bo catalog | `AdminController.getAllProducts()` + `AdminProducts` | `backend/src/main/java/com/y2kstore/backend/controller/AdminController.java:138-142`, `frontend/src/components/admin/AdminProducts.vue` |

Admin co hai nguon ranking:

1. `featuredBestSelling` / `featuredTopRated`: ranking toan he thong tu `/api/products/featured`.
2. `topProducts`: ranking theo order va order item trong khoang ngay/tuan/thang dang chon.

## 4. Data flow

```text
orders + order_details
        |
        +--> OrderItemRepository.sumQuantityByProductId()
        |        |
        |        +--> ProductController.toDTO().soldCount
        |
reviews +--> ReviewRepository.countByProductId()
          +--> ReviewRepository.averageRatingByProductId()
                   |
                   +--> ProductController.toDTO()
                              |
                              +--> GET /api/products/featured
                                         |
              +--------------------------+--------------------------+
              |                          |                          |
           HomeView               NewArrivalsView              AdminOverview
           Shop sort              ProductCard                   featured lists
```

## 5. Tra cuu khi can thay doi

- Doi cong thuc ban chay: sua `sumQuantityByProductId()` hoac comparator best-selling trong `ProductController`.
- Doi cong thuc rating: sua `ReviewRepository` hoac comparator top-rated trong `ProductController`.
- Doi moc 14 ngay: sua `newArrivalCutoff` backend va `isNewProduct()` / sort `new` frontend.
- Doi so luong card: sua `limit` o frontend; backend cho phep toi da 24.
- Doi giao dien card: sua `frontend/src/components/ProductCard.vue`.
