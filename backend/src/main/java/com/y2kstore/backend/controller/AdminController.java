package com.y2kstore.backend.controller;

import com.y2kstore.backend.dto.*;
import com.y2kstore.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/orders/{id}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(adminService.getOrderItems(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            adminService.updateOrderStatus(id, body.get("status"));
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái đơn hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createProduct(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateProduct(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            adminService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/products/{id}/images")
    public ResponseEntity<?> createProductImage(@PathVariable Integer id, @RequestBody ProductImageDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createProductImage(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/products/{productId}/images/{imageId}")
    public ResponseEntity<?> updateProductImage(@PathVariable Integer productId, @PathVariable Integer imageId, @RequestBody ProductImageDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateProductImage(productId, imageId, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{productId}/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Integer productId, @PathVariable Integer imageId) {
        try {
            adminService.deleteProductImage(productId, imageId);
            return ResponseEntity.ok(Map.of("message", "Xóa hình ảnh thành công!"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/products/{id}/variants")
    public ResponseEntity<?> createProductVariant(@PathVariable Integer id, @RequestBody ProductVariantDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createProductVariant(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<?> updateProductVariant(@PathVariable Integer productId, @PathVariable Integer variantId, @RequestBody ProductVariantDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateProductVariant(productId, variantId, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<?> deleteProductVariant(@PathVariable Integer productId, @PathVariable Integer variantId) {
        try {
            adminService.deleteProductVariant(productId, variantId);
            return ResponseEntity.ok(Map.of("message", "Xóa biến thể thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/products/{id}/promotions")
    public ResponseEntity<?> updateProductPromotions(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        try {
            return ResponseEntity.ok(adminService.updateProductPromotions(id, body.getOrDefault("promotionIds", List.of())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateUser(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createCategory(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateCategory(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            adminService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/banners")
    public ResponseEntity<List<BannerDTO>> getAllBanners() {
        return ResponseEntity.ok(adminService.getAllBanners());
    }

    @PostMapping("/banners")
    public ResponseEntity<?> createBanner(@RequestBody BannerDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createBanner(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Integer id, @RequestBody BannerDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateBanner(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Integer id) {
        adminService.deleteBanner(id);
        return ResponseEntity.ok(Map.of("message", "Xóa banner thành công!"));
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        return ResponseEntity.ok(adminService.getAllPromotions());
    }

    @PostMapping("/promotions")
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createPromotion(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Integer id, @RequestBody PromotionDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updatePromotion(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/promotions/{id}/products")
    public ResponseEntity<?> updatePromotionProducts(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        try {
            return ResponseEntity.ok(adminService.updatePromotionProducts(id, body.getOrDefault("productIds", List.of())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Integer id) {
        try {
            adminService.deletePromotion(id);
            return ResponseEntity.ok(Map.of("message", "Xóa khuyến mãi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        return ResponseEntity.ok(adminService.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody CouponDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createCoupon(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Integer id, @RequestBody CouponDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateCoupon(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Integer id) {
        try {
            adminService.deleteCoupon(id);
            return ResponseEntity.ok(Map.of("message", "Xóa coupon thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- CÁC ENDPOINT QUẢN LÝ ĐÁNH GIÁ (REVIEW) CHO ADMIN ---
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(adminService.getAllReviews());
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody ReviewDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createReview(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Integer id, @RequestBody ReviewDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateReview(id, dto)); // Dùng để đổi status (VISIBLE, FLAGGED, HIDDEN)
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        adminService.deleteReview(id);
        return ResponseEntity.ok(Map.of("message", "Xóa đánh giá thành công!"));
    }
    // --------------------------------------------------------

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        return ResponseEntity.ok(adminService.getAllAddresses());
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(@RequestBody AddressDTO dto) {
        try {
            return ResponseEntity.ok(adminService.createAddress(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer id, @RequestBody AddressDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updateAddress(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer id) {
        adminService.deleteAddress(id);
        return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công!"));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @PutMapping("/payments/{id}")
    public ResponseEntity<?> updatePayment(@PathVariable Integer id, @RequestBody PaymentDTO dto) {
        try {
            return ResponseEntity.ok(adminService.updatePayment(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }



}