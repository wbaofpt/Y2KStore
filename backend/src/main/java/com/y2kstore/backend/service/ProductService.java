package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.ProductDTO;
import com.y2kstore.backend.dto.ReviewDTO;
import com.y2kstore.backend.entity.*;
import com.y2kstore.backend.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final int PRODUCT_STATUS_SELLING = 1;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductPromotionRepository productPromotionRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          ProductVariantRepository productVariantRepository,
                          ProductPromotionRepository productPromotionRepository,
                          OrderItemRepository orderItemRepository,
                          ReviewRepository reviewRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.productPromotionRepository = productPromotionRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProducts(Integer categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products;
        if (categoryId != null && search != null && !search.trim().isEmpty()) {
            products = productRepository.findByAnyCategoryIdAndNameContainingIgnoreCaseAndStatus(categoryId, search, PRODUCT_STATUS_SELLING);
        } else if (categoryId != null) {
            products = productRepository.findByAnyCategoryIdAndStatus(categoryId, PRODUCT_STATUS_SELLING);
        } else if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseAndStatus(search, PRODUCT_STATUS_SELLING);
        } else {
            products = productRepository.findByStatus(PRODUCT_STATUS_SELLING);
        }

        List<ProductDTO> dtos = products.stream().map(this::toDTO).collect(Collectors.toList());
        if (minPrice != null) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getMinVariantPrice() != null ? dto.getMinVariantPrice().compareTo(minPrice) >= 0 : dto.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getMinVariantPrice() != null ? dto.getMinVariantPrice().compareTo(maxPrice) <= 0 : dto.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        return productRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySlug(String slug) {
        return productRepository.findByStatus(PRODUCT_STATUS_SELLING).stream()
                .filter(product -> slugify(product.getName()).equalsIgnoreCase(slug))
                .findFirst()
                .map(this::toDTO)
                .orElse(null);
    }

    private String slugify(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "d")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getProductReviews(Integer id) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(id).stream()
                .map(ReviewDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewEligibility(Integer id) {
        User user = getAuthenticatedUser();
        boolean purchased = orderItemRepository.existsReviewablePurchase(user.getId(), id);
        boolean reviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), id);

        return Map.of(
                "canReview", purchased && !reviewed,
                "purchased", purchased,
                "reviewed", reviewed
        );
    }

    @Transactional
    public ReviewDTO createProductReview(Integer id, ReviewDTO dto) {
        User user = getAuthenticatedUser();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (!orderItemRepository.existsReviewablePurchase(user.getId(), id)) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao và thanh toán.");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), id)) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        int rating = dto.getRating() == null ? 5 : Math.max(1, Math.min(dto.getRating(), 5));
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(dto.getComment());
        review.setImageUrls(ReviewDTO.toJson(dto.getImageUrls()));
        review.setVideoUrls(ReviewDTO.toJson(dto.getVideoUrls()));
        reviewRepository.save(review);

        return new ReviewDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts(String type, Integer limit) {
        int safeLimit = Math.min(Math.max(limit == null ? 10 : limit, 1), 24);
        List<ProductDTO> products = productRepository.findByStatus(PRODUCT_STATUS_SELLING).stream()
                .map(this::toDTO)
                .toList();

        Comparator<ProductDTO> bestSellingOrder = Comparator
                .comparingLong(this::soldCount).reversed()
                .thenComparing(Comparator.comparingDouble(this::ratingAverage).reversed())
                .thenComparing(Comparator.comparingLong(this::reviewCount).reversed())
                .thenComparing(ProductDTO::getId);
        Comparator<ProductDTO> topRatedOrder = Comparator
                .comparingDouble(this::ratingAverage).reversed()
                .thenComparing(Comparator.comparingLong(this::reviewCount).reversed())
                .thenComparing(Comparator.comparingLong(this::soldCount).reversed())
                .thenComparing(ProductDTO::getId);
        Comparator<ProductDTO> newestOrder = Comparator
                .comparing(ProductDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                .thenComparing(Comparator.comparing(ProductDTO::getId).reversed());

        List<ProductDTO> bestSelling = products.stream().sorted(bestSellingOrder).toList();
        List<ProductDTO> topRated = products.stream()
                .filter(product -> reviewCount(product) > 0)
                .sorted(topRatedOrder)
                .toList();
        LocalDateTime newArrivalCutoff = LocalDateTime.now().minusDays(14);
        List<ProductDTO> newArrivals = products.stream()
                .filter(product -> product.getCreatedAt() != null && !product.getCreatedAt().isBefore(newArrivalCutoff))
                .sorted(newestOrder)
                .toList();

        String mode = type == null ? "combined" : type.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if ("best".equals(mode)) mode = "best-selling";
        if ("rated".equals(mode)) mode = "top-rated";
        if ("newest".equals(mode) || "new-arrivals".equals(mode)) mode = "new";

        if ("best-selling".equals(mode)) {
            return bestSelling.stream().limit(safeLimit).toList();
        }
        if ("top-rated".equals(mode)) {
            return topRated.stream().limit(safeLimit).toList();
        }
        if ("new".equals(mode)) {
            return newArrivals.stream().limit(safeLimit).toList();
        }

        LinkedHashMap<Integer, ProductDTO> combined = new LinkedHashMap<>();
        for (int index = 0; combined.size() < safeLimit && index < Math.max(bestSelling.size(), topRated.size()); index++) {
            if (index < bestSelling.size()) combined.putIfAbsent(bestSelling.get(index).getId(), bestSelling.get(index));
            if (index < topRated.size() && combined.size() < safeLimit) combined.putIfAbsent(topRated.get(index).getId(), topRated.get(index));
        }
        return new ArrayList<>(combined.values());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getSaleProducts() {
        return productRepository.findByStatus(PRODUCT_STATUS_SELLING).stream()
                .map(this::toDTO)
                .filter(dto -> dto.getPromotionDiscountPercent() != null && dto.getPromotionDiscountPercent().compareTo(BigDecimal.ZERO) > 0)
                .limit(6)
                .collect(Collectors.toList());
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO(
                product,
                productImageRepository.findByProductId(product.getId()),
                productVariantRepository.findByProductId(product.getId())
        );
        findActivePromotion(product).ifPresent(promotion -> {
            dto.setPromotionName(promotion.getPromotionName());
            dto.setPromotionDiscountPercent(promotion.getDiscountPercent());
        });
        long reviewCount = reviewRepository.countByProductId(product.getId());
        dto.setReviewCount(reviewCount);
        double averageRating = reviewCount > 0 ? reviewRepository.averageRatingByProductId(product.getId()) : 0D;
        dto.setRatingAverage(BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP));
        dto.setSoldCount(orderItemRepository.sumQuantityByProductId(product.getId()));
        return dto;
    }

    private Optional<Promotion> findActivePromotion(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return productPromotionRepository.findByProductId(product.getId()).stream()
                .map(ProductPromotion::getPromotion)
                .filter(promotion -> promotion != null && Boolean.TRUE.equals(promotion.getStatus()))
                .filter(promotion -> promotion.getStartDate() == null || !promotion.getStartDate().isAfter(now))
                .filter(promotion -> promotion.getEndDate() == null || !promotion.getEndDate().isBefore(now))
                .findFirst();
    }

    private long soldCount(ProductDTO product) {
        return product.getSoldCount() == null ? 0L : product.getSoldCount();
    }

    private long reviewCount(ProductDTO product) {
        return product.getReviewCount() == null ? 0L : product.getReviewCount();
    }

    private double ratingAverage(ProductDTO product) {
        return product.getRatingAverage() == null ? 0D : product.getRatingAverage().doubleValue();
    }
}
