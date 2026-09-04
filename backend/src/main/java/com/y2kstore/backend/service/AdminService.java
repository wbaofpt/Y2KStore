package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.*;
import com.y2kstore.backend.entity.*;
import com.y2kstore.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
    private static final int MAX_ADDRESSES_PER_USER = 4;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductPromotionRepository productPromotionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final BannerRepository bannerRepository;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;

    public AdminService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        ProductRepository productRepository,
                        ProductImageRepository productImageRepository,
                        ProductVariantRepository productVariantRepository,
                        ProductPromotionRepository productPromotionRepository,
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        CategoryRepository categoryRepository,
                        BannerRepository bannerRepository,
                        AddressRepository addressRepository,
                        PromotionRepository promotionRepository,
                        CouponRepository couponRepository,
                        ReviewRepository reviewRepository,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.productPromotionRepository = productPromotionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.bannerRepository = bannerRepository;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
        this.emailService = emailService;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin!"));
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getStats() {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long pendingOrders = orderRepository.countByStatus("PROCESSING");
        long lowStockProducts = productVariantRepository.countByStockLessThan(10);
        long totalCategories = categoryRepository.count();
        long totalBanners = bannerRepository.count();
        long totalPromotions = promotionRepository.count();
        long totalCoupons = couponRepository.count();
        long totalReviews = reviewRepository.count();
        long totalAddresses = addressRepository.count();
        long totalPayments = paymentRepository.count();

        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(order -> !"CANCELLED".equalsIgnoreCase(order.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminStatsDTO(
                totalRevenue, totalOrders, totalProducts, totalUsers,
                pendingOrders, lowStockProducts, totalCategories, totalBanners,
                totalPromotions, totalCoupons, totalReviews, totalAddresses, totalPayments
        );
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream().map(OrderDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderItemDTO> getOrderItems(Integer id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Đơn hàng không tồn tại!");
        }
        return orderItemRepository.findByOrderId(id).stream().map(OrderItemDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public void updateOrderStatus(Integer id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));
        String previousStatus = order.getStatus();
        order.setStatus(status);
        if ("DELIVERED".equalsIgnoreCase(status) && order.getPayment() != null) {
            order.getPayment().setStatus("PAID");
            paymentRepository.save(order.getPayment());
        }
        orderRepository.save(order);
        notifyCustomerWhenOrderStatusChanges(order, previousStatus, status);
    }

    private void notifyCustomerWhenOrderStatusChanges(Order order, String previousStatus, String status) {
        if (status == null || status.equalsIgnoreCase(previousStatus)) {
            return;
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!List.of("PREPARING", "SHIPPED", "DELIVERED").contains(normalizedStatus)) {
            return;
        }
        if (order.getUser() == null || order.getUser().getEmail() == null || order.getUser().getEmail().isBlank()) {
            return;
        }
        try {
            emailService.sendOrderStatusNotification(order.getUser().getEmail(), order.getId(), normalizedStatus);
        } catch (RuntimeException ex) {
            LOGGER.warn("Không thể gửi email cập nhật trạng thái đơn {} tới {}", order.getId(), order.getUser().getEmail(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImage(dto.getImage());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        product.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        applyProductCategories(product, dto);
        productRepository.save(product);
        syncProductPromotions(product, dto.getPromotionIds());
        return toDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImage(dto.getImage());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : product.getStatus());
        if (dto.getCreatedAt() != null) product.setCreatedAt(dto.getCreatedAt());
        applyProductCategories(product, dto);
        productRepository.save(product);
        syncProductPromotions(product, dto.getPromotionIds());
        return toDTO(product);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductImageDTO createProductImage(Integer id, ProductImageDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(dto.getImageUrl());
        productImageRepository.save(image);
        return new ProductImageDTO(image);
    }

    @Transactional
    public ProductImageDTO updateProductImage(Integer productId, Integer imageId, ProductImageDTO dto) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại!"));
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Hình ảnh không thuộc sản phẩm này!");
        }
        image.setImageUrl(dto.getImageUrl());
        productImageRepository.save(image);
        return new ProductImageDTO(image);
    }

    @Transactional
    public void deleteProductImage(Integer productId, Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại!"));
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Hình ảnh không thuộc sản phẩm này!");
        }
        productImageRepository.delete(image);
    }

    @Transactional
    public ProductVariantDTO createProductVariant(Integer id, ProductVariantDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        applyVariantFields(variant, dto);
        productVariantRepository.save(variant);
        return new ProductVariantDTO(variant);
    }

    @Transactional
    public ProductVariantDTO updateProductVariant(Integer productId, Integer variantId, ProductVariantDTO dto) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại!"));
        if (variant.getProduct() == null || !variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Biến thể không thuộc sản phẩm này!");
        }
        applyVariantFields(variant, dto);
        productVariantRepository.save(variant);
        return new ProductVariantDTO(variant);
    }

    @Transactional
    public void deleteProductVariant(Integer productId, Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại!"));
        if (variant.getProduct() == null || !variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Biến thể không thuộc sản phẩm này!");
        }
        try {
            productVariantRepository.delete(variant);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể xóa biến thể đã phát sinh giỏ hàng hoặc đơn hàng!");
        }
    }

    @Transactional
    public ProductDTO updateProductPromotions(Integer id, List<Integer> promotionIds) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        syncProductPromotions(product, promotionIds);
        return toDTO(product);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Integer id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (dto.getStatus() != null && !dto.getStatus() && user.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new RuntimeException("Không thể khóa tài khoản admin.");
        }

        if (dto.getRole() != null) {
            Role role = roleRepository.findByRoleName(dto.getRole())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại!"));
            user.setRole(role);
        }
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getEmailVerified() != null) user.setEmailVerified(dto.getEmailVerified());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        userRepository.save(user);
        return new UserDTO(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User current = getCurrentUser();
        if (current.getId().equals(id)) {
            throw new RuntimeException("Không thể xóa tài khoản đang đăng nhập!");
        }
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO(category);
            dto.setProductCount(productRepository.countByAnyCategoryId(category.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = new Category(dto.getName(), dto.getDescription());
        if (dto.getCategoryType() != null) {
            category.setCategoryType(normalizeCategoryType(dto.getCategoryType()));
        }
        applyCategoryParent(category, dto);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : true);
        categoryRepository.save(category);
        return new CategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        if (dto.getCategoryType() != null) {
            category.setCategoryType(normalizeCategoryType(dto.getCategoryType()));
        }
        applyCategoryParent(category, dto);
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        categoryRepository.save(category);
        return new CategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (productRepository.countByAnyCategoryId(id) > 0) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm!");
        }
        if (categoryRepository.existsByParentCategoryId(id)) {
            throw new RuntimeException("Không thể xóa danh mục chung khi còn danh mục con!");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAllByOrderByDateBannerDesc().stream().map(BannerDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public BannerDTO createBanner(BannerDTO dto) {
        Banner banner = new Banner();
        applyBannerFields(banner, dto);
        bannerRepository.save(banner);
        return new BannerDTO(banner);
    }

    @Transactional
    public BannerDTO updateBanner(Integer id, BannerDTO dto) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại!"));
        applyBannerFields(banner, dto);
        bannerRepository.save(banner);
        return new BannerDTO(banner);
    }

    @Transactional
    public void deleteBanner(Integer id) {
        bannerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAllByOrderByIdDesc().stream().map(PromotionDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public PromotionDTO createPromotion(PromotionDTO dto) {
        Promotion promotion = new Promotion();
        applyPromotionFields(promotion, dto);
        promotionRepository.save(promotion);
        return new PromotionDTO(promotion);
    }

    @Transactional
    public PromotionDTO updatePromotion(Integer id, PromotionDTO dto) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại!"));
        applyPromotionFields(promotion, dto);
        promotionRepository.save(promotion);
        return new PromotionDTO(promotion);
    }

    @Transactional
    public PromotionDTO updatePromotionProducts(Integer id, List<Integer> productIds) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại!"));
        syncPromotionProducts(promotion, productIds);
        return new PromotionDTO(promotion);
    }

    @Transactional
    public void deletePromotion(Integer id) {
        if (productPromotionRepository.countByPromotionId(id) > 0 || couponRepository.countByPromotionId(id) > 0) {
            throw new RuntimeException("Không thể xóa khuyến mãi đang được gắn với sản phẩm hoặc coupon!");
        }
        promotionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAllByOrderByIdDesc().stream().map(CouponDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        Coupon coupon = new Coupon();
        applyCouponFields(coupon, dto);
        couponRepository.save(coupon);
        return new CouponDTO(coupon);
    }

    @Transactional
    public CouponDTO updateCoupon(Integer id, CouponDTO dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));
        applyCouponFields(coupon, dto);
        couponRepository.save(coupon);
        return new CouponDTO(coupon);
    }

    @Transactional
    public void deleteCoupon(Integer id) {
        if (orderRepository.countByCouponId(id) > 0) {
            throw new RuntimeException("Không thể xóa coupon đã được dùng trong đơn hàng!");
        }
        couponRepository.deleteById(id);
    }

    // =========================================================================
    // PHẦN LOGIC XỬ LÝ REVIEW CHO ADMIN (ĐÃ TÍCH HỢP ĐẦY ĐỦ TRẠNG THÁI AI)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream().map(ReviewDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO dto) {
        Review review = new Review();
        applyReviewFields(review, dto);
        reviewRepository.save(review);
        return new ReviewDTO(review);
    }

    @Transactional
    public ReviewDTO updateReview(Integer id, ReviewDTO dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));
        applyReviewFields(review, dto); // Hàm này giúp lưu lại status (VISIBLE/FLAGGED/HIDDEN) khi Admin bấm duyệt/ẩn
        reviewRepository.save(review);
        return new ReviewDTO(review);
    }

    @Transactional
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }
    // =========================================================================

    @Transactional(readOnly = true)
    public List<AddressDTO> getAllAddresses() {
        return addressRepository.findAllByOrderByIdDesc().stream().map(AddressDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(AddressDTO dto) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("Vui lòng chọn người dùng cho địa chỉ.");
        }
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        if (addressRepository.countByUserId(user.getId()) >= MAX_ADDRESSES_PER_USER) {
            throw new RuntimeException("Mỗi tài khoản chỉ được lưu tối đa 4 địa chỉ.");
        }
        Address address = new Address();
        applyAddressFields(address, dto);
        addressRepository.save(address);
        syncDefaultAddress(address);
        return new AddressDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(Integer id, AddressDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
        applyAddressFields(address, dto);
        addressRepository.save(address);
        syncDefaultAddress(address);
        return new AddressDTO(address);
    }

    @Transactional
    public void deleteAddress(Integer id) {
        addressRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAllByOrderByPaymentDateDesc().stream().map(PaymentDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO updatePayment(Integer id, PaymentDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thanh toán không tồn tại!"));
        applyPaymentFields(payment, dto);
        paymentRepository.save(payment);
        return new PaymentDTO(payment);
    }

    // Các hàm bổ trợ (Helper methods)
    private void applyBannerFields(Banner banner, BannerDTO dto) {
        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
            banner.setProduct(product);
        } else {
            banner.setProduct(null);
        }
        banner.setImage(dto.getImage());
        banner.setTitle(dto.getTitle());
        banner.setSubtitle(dto.getSubtitle());
        banner.setButtonText(dto.getButtonText());
        if (dto.getSortOrder() != null) banner.setSortOrder(dto.getSortOrder());
        if (dto.getDateBanner() != null && !dto.getDateBanner().isBlank()) {
            banner.setDateBanner(java.time.LocalDate.parse(dto.getDateBanner()));
        }
        if (dto.getStatus() != null) banner.setStatus(dto.getStatus());
    }

    private void applyPromotionFields(Promotion promotion, PromotionDTO dto) {
        promotion.setPromotionName(dto.getPromotionName());
        promotion.setDiscountPercent(dto.getDiscountPercent());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) promotion.setStatus(dto.getStatus());
    }

    private void applyCouponFields(Coupon coupon, CouponDTO dto) {
        if (dto.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(dto.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại!"));
            coupon.setPromotion(promotion);
        } else {
            coupon.setPromotion(null);
        }
        coupon.setCouponCode(dto.getCouponCode());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setExpireDate(dto.getExpireDate());
        if (dto.getStatus() != null) coupon.setStatus(dto.getStatus());
    }

    private void applyReviewFields(Review review, ReviewDTO dto) {
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
            review.setUser(user);
        }
        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
            review.setProduct(product);
        }
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setImageUrls(ReviewDTO.toJson(dto.getImageUrls()));
        review.setVideoUrls(ReviewDTO.toJson(dto.getVideoUrls()));

        // --- CẬP NHẬT QUAN TRỌNG: Ánh xạ trạng thái và thông tin AI từ phía Admin gửi lên ---
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            review.setStatus(dto.getStatus());
        }
        if (dto.getAiChecked() != null) {
            review.setAiChecked(dto.getAiChecked());
        }
        if (dto.getAiScore() != null) {
            review.setAiScore(dto.getAiScore());
        }
        if (dto.getAiReason() != null) {
            review.setAiReason(dto.getAiReason());
        }
    }

    private void applyAddressFields(Address address, AddressDTO dto) {
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
            address.setUser(user);
        }
        address.setProvince(dto.getProvince());
        address.setDistrict(dto.getDistrict());
        address.setWard(dto.getWard());
        address.setDetail(dto.getDetail());
        if (dto.getIsDefault() != null) address.setIsDefault(dto.getIsDefault());
    }

    private void applyPaymentFields(Payment payment, PaymentDTO dto) {
        if (dto.getMethod() != null) payment.setMethod(dto.getMethod());
        if (dto.getAmount() != null) payment.setAmount(dto.getAmount());
        if (dto.getStatus() != null) payment.setStatus(dto.getStatus());
    }

    private void applyVariantFields(ProductVariant variant, ProductVariantDTO dto) {
        variant.setSize(dto.getSize());
        variant.setColor(dto.getColor());
        variant.setStock(dto.getStock() != null ? dto.getStock() : 0);
        variant.setPrice(dto.getPrice());
        variant.setStatus(dto.getStatus() != null ? dto.getStatus() : true);
    }

    private void syncDefaultAddress(Address address) {
        if (!Boolean.TRUE.equals(address.getIsDefault()) || address.getUser() == null) return;
        List<Address> addresses = addressRepository.findByUserId(address.getUser().getId());
        for (Address other : addresses) {
            if (!other.getId().equals(address.getId()) && Boolean.TRUE.equals(other.getIsDefault())) {
                other.setIsDefault(false);
            }
        }
        addressRepository.saveAll(addresses);
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO(
                product,
                productImageRepository.findByProductId(product.getId()),
                productVariantRepository.findByProductId(product.getId())
        );
        List<ProductPromotion> productPromotions = productPromotionRepository.findByProductId(product.getId());
        dto.setPromotionIds(productPromotions.stream().map(ProductPromotion::getPromotion).filter(Objects::nonNull).map(Promotion::getId).collect(Collectors.toList()));
        dto.setPromotionNames(productPromotions.stream().map(ProductPromotion::getPromotion).filter(Objects::nonNull).map(Promotion::getPromotionName).collect(Collectors.toList()));
        findActivePromotion(product, productPromotions).ifPresent(promotion -> {
            dto.setPromotionName(promotion.getPromotionName());
            dto.setPromotionDiscountPercent(promotion.getDiscountPercent());
        });
        return dto;
    }

    private void applyProductCategories(Product product, ProductDTO dto) {
        Integer generalCategoryId = dto.getCategoryId();
        if (generalCategoryId == null || generalCategoryId <= 0) {
            generalCategoryId = dto.getCategoryIds() == null ? null : dto.getCategoryIds().stream().filter(id -> id != null && id > 0).findFirst().orElse(null);
        }
        if (generalCategoryId == null) {
            product.setCategory(null);
            product.getCategories().clear();
            return;
        }
        Category generalCategory = categoryRepository.findById(generalCategoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục chung không tồn tại!"));
        if (!"GENERAL".equals(normalizeCategoryType(generalCategory.getCategoryType()))) {
            throw new RuntimeException("categoryId phải là danh mục chung (GENERAL)!");
        }
        product.setCategory(generalCategory);
        product.getCategories().clear();
        if (dto.getCategoryIds() != null) {
            for (Integer tagId : dto.getCategoryIds().stream().distinct().toList()) {
                if (tagId == null || tagId <= 0 || tagId.equals(generalCategoryId)) continue;
                Category tag = categoryRepository.findById(tagId).orElseThrow(() -> new RuntimeException("Danh mục sản phẩm không tồn tại!"));
                if (!"TAG".equals(normalizeCategoryType(tag.getCategoryType()))) {
                    throw new RuntimeException("product_categories chỉ nhận danh mục sản phẩm (TAG)!");
                }
                product.getCategories().add(tag);
            }
        }
    }

    private void applyCategoryParent(Category category, CategoryDTO dto) {
        String categoryType = normalizeCategoryType(category.getCategoryType());
        category.setCategoryType(categoryType);
        if (!"TAG".equals(categoryType)) {
            category.setParentCategory(null);
            return;
        }
        Integer parentCategoryId = dto.getParentCategoryId();
        if (parentCategoryId == null || parentCategoryId <= 0) throw new RuntimeException("Danh mục sản phẩm cần chọn danh mục cha!");
        Category parent = categoryRepository.findById(parentCategoryId).orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại!"));
        category.setParentCategory(parent);
    }

    private String normalizeCategoryType(String value) {
        return "TAG".equalsIgnoreCase(value == null ? "" : value.trim()) ? "TAG" : "GENERAL";
    }

    private void syncProductPromotions(Product product, List<Integer> promotionIds) {
        List<ProductPromotion> currentLinks = productPromotionRepository.findByProductId(product.getId());
        Set<Integer> ids = promotionIds == null ? Set.of() : promotionIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toCollection(LinkedHashSet::new));
        productPromotionRepository.deleteAll(currentLinks.stream().filter(link -> link.getPromotion() == null || !ids.contains(link.getPromotion().getId())).collect(Collectors.toList()));
        List<ProductPromotion> links = new ArrayList<>();
        for (Integer promotionId : ids) {
            if (currentLinks.stream().anyMatch(l -> l.getPromotion() != null && l.getPromotion().getId().equals(promotionId))) continue;
            Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại!"));
            ProductPromotion link = new ProductPromotion();
            link.setProduct(product);
            link.setPromotion(promotion);
            links.add(link);
        }
        productPromotionRepository.saveAll(links);
    }

    private void syncPromotionProducts(Promotion promotion, List<Integer> productIds) {
        List<ProductPromotion> currentLinks = productPromotionRepository.findByPromotionId(promotion.getId());
        Set<Integer> ids = productIds == null ? Set.of() : productIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toCollection(LinkedHashSet::new));
        productPromotionRepository.deleteAll(currentLinks.stream().filter(link -> link.getProduct() == null || !ids.contains(link.getProduct().getId())).collect(Collectors.toList()));
        List<ProductPromotion> links = new ArrayList<>();
        for (Integer productId : ids) {
            if (currentLinks.stream().anyMatch(l -> l.getProduct() != null && l.getProduct().getId().equals(productId))) continue;
            Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
            ProductPromotion link = new ProductPromotion();
            link.setProduct(product);
            link.setPromotion(promotion);
            links.add(link);
        }
        productPromotionRepository.saveAll(links);
    }

    private Optional<Promotion> findActivePromotion(Product product, List<ProductPromotion> productPromotions) {
        LocalDateTime now = LocalDateTime.now();
        return productPromotions.stream()
                .map(ProductPromotion::getPromotion)
                .filter(promotion -> promotion != null && Boolean.TRUE.equals(promotion.getStatus()))
                .filter(promotion -> promotion.getStartDate() == null || !promotion.getStartDate().isAfter(now))
                .filter(promotion -> promotion.getEndDate() == null || !promotion.getEndDate().isBefore(now))
                .findFirst();
    }
}