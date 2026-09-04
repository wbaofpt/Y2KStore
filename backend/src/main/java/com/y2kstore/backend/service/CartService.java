package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.CartAddRequest;
import com.y2kstore.backend.dto.CartItemDTO;
import com.y2kstore.backend.dto.CartUpdateRequest;
import com.y2kstore.backend.entity.CartItem;
import com.y2kstore.backend.entity.ProductPromotion;
import com.y2kstore.backend.entity.ProductVariant;
import com.y2kstore.backend.entity.Promotion;
import com.y2kstore.backend.entity.User;
import com.y2kstore.backend.repository.CartItemRepository;
import com.y2kstore.backend.repository.ProductPromotionRepository;
import com.y2kstore.backend.repository.ProductVariantRepository;
import com.y2kstore.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductPromotionRepository productPromotionRepository;

    public CartService(CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductVariantRepository productVariantRepository,
                       ProductPromotionRepository productPromotionRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
        this.productPromotionRepository = productPromotionRepository;
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập hoặc tài khoản không tồn tại!"));
    }

    @Transactional(readOnly = true)
    public List<CartItemDTO> getCart() {
        User user = getAuthenticatedUser();
        return cartItemRepository.findByUserId(user.getId()).stream()
                .map(this::toDiscountedCartItem)
                .collect(Collectors.toList());
    }

    private CartItemDTO toDiscountedCartItem(CartItem item) {
        CartItemDTO dto = new CartItemDTO(item);
        ProductVariant variant = item.getVariant();
        BigDecimal basePrice = variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getPrice();
        BigDecimal unitPrice = discountedPrice(basePrice, variant.getProduct().getId());
        dto.setUnitPrice(unitPrice);
        dto.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
        return dto;
    }

    private BigDecimal discountedPrice(BigDecimal basePrice, Integer productId) {
        if (basePrice == null || productId == null) return BigDecimal.ZERO;
        Optional<Promotion> promotion = productPromotionRepository.findByProductId(productId).stream()
                .map(ProductPromotion::getPromotion)
                .filter(item -> item != null && Boolean.TRUE.equals(item.getStatus()))
                .filter(item -> item.getStartDate() == null || !item.getStartDate().isAfter(LocalDateTime.now()))
                .filter(item -> item.getEndDate() == null || !item.getEndDate().isBefore(LocalDateTime.now()))
                .filter(item -> item.getDiscountPercent() != null && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0)
                .findFirst();
        return promotion.map(item -> basePrice
                        .multiply(BigDecimal.valueOf(100).subtract(item.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .orElse(basePrice.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional
    public CartItemDTO addToCart(CartAddRequest request) {
        User user = getAuthenticatedUser();
        if (request.getVariantId() == null) {
            throw new RuntimeException("Thiếu variantId!");
        }

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại!"));

        int qty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        if (variant.getStock() < qty) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho!");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndVariantId(user.getId(), variant.getId()).orElse(null);
        if (cartItem != null) {
            int newQty = cartItem.getQuantity() + qty;
            if (newQty > variant.getStock()) {
                throw new RuntimeException("Vượt quá số lượng tồn kho!");
            }
            cartItem.setQuantity(newQty);
        } else {
            cartItem = new CartItem(user, variant, qty);
        }

        cartItemRepository.save(cartItem);
        return new CartItemDTO(cartItem);
    }

    @Transactional
    public CartItemDTO updateQuantity(Integer variantId, CartUpdateRequest request) {
        User user = getAuthenticatedUser();
        CartItem cartItem = cartItemRepository.findByUserIdAndVariantId(user.getId(), variantId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng!"));

        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        if (qty < 1) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }
        if (cartItem.getVariant().getStock() < qty) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho!");
        }

        cartItem.setQuantity(qty);
        cartItemRepository.save(cartItem);
        return new CartItemDTO(cartItem);
    }

    @Transactional
    public void removeFromCart(Integer variantId) {
        User user = getAuthenticatedUser();
        cartItemRepository.deleteByUserIdAndVariantId(user.getId(), variantId);
    }

    @Transactional
    public void clearCart() {
        User user = getAuthenticatedUser();
        cartItemRepository.deleteByUserId(user.getId());
    }
}