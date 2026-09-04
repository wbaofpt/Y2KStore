package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.OrderDTO;
import com.y2kstore.backend.dto.OrderItemDTO;
import com.y2kstore.backend.dto.OrderRequest;
import com.y2kstore.backend.dto.PaymentSessionDTO;
import com.y2kstore.backend.entity.*;
import com.y2kstore.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(499000);
    private static final BigDecimal STANDARD_SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final ProductPromotionRepository productPromotionRepository;
    private final PaymentGatewayService paymentGatewayService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        ProductVariantRepository productVariantRepository,
                        UserRepository userRepository,
                        CartItemRepository cartItemRepository,
                        AddressRepository addressRepository,
                        CouponRepository couponRepository,
                        ProductPromotionRepository productPromotionRepository,
                        PaymentGatewayService paymentGatewayService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.couponRepository = couponRepository;
        this.productPromotionRepository = productPromotionRepository;
        this.paymentGatewayService = paymentGatewayService;
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders() {
        User user = getAuthenticatedUser();
        return orderRepository.findByUserIdAndHiddenFromHistoryFalseOrderByOrderDateDesc(user.getId())
                .stream().map(OrderDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderItemDTO> getOrderItems(Integer orderId) {
        User user = getAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole() != null ? user.getRole().getRoleName() : null);
        if (!order.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new RuntimeException("Forbidden");
        }

        return orderItemRepository.findByOrderId(orderId).stream().map(OrderItemDTO::new).toList();
    }

    @Transactional
    public void deleteHistoryOrder(Integer id) {
        User user = getAuthenticatedUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa đơn hàng này.");
        }
        if (!List.of("DELIVERED", "CANCELLED").contains(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể xóa đơn đã giao hoặc đã hủy.");
        }

        order.setHiddenFromHistory(true);
        orderRepository.save(order);
    }

    @Transactional
    public Map<String, Object> placeOrder(OrderRequest orderRequest) {
        User user = getAuthenticatedUser();
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng rỗng!");
        }

        String paymentMethod = normalizePaymentMethod(orderRequest.getPaymentMethod());

        Order order = new Order();
        order.setUser(user);
        order.setStatus("COD".equals(paymentMethod) ? "PREPARING" : "PROCESSING");
        order.setShippingAddress(resolveAddress(user, orderRequest.getAddressId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại!"));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName() + " không đủ số lượng trong kho!");
            }

            variant.setStock(variant.getStock() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            BigDecimal basePrice = variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getPrice();
            BigDecimal unitPrice = discountedPrice(basePrice, variant.getProduct().getId());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            OrderItem orderItem = new OrderItem(order, variant, cartItem.getQuantity(), unitPrice);
            orderItems.add(orderItem);
        }

        Coupon coupon = resolveCoupon(orderRequest.getCouponCode());
        BigDecimal discountAmount = resolveCouponDiscount(coupon, subtotal);
        BigDecimal shippingFee = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : STANDARD_SHIPPING_FEE;
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        if (coupon != null) {
            order.setCoupon(coupon);
        }
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(paymentMethod);
        payment.setAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        payment.setStatus("COD".equals(paymentMethod) ? "UNPAID" : "PENDING");
        paymentRepository.save(payment);

        PaymentSessionDTO paymentSession = paymentGatewayService.createPaymentSession(order, payment, orderItems);

        cartItemRepository.deleteByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đặt hàng thành công!");
        response.put("orderId", order.getId());
        response.put("paymentMethod", paymentMethod);
        response.put("paymentStatus", payment.getStatus());
        response.put("paymentSession", paymentSession);
        response.put("subtotal", subtotal.setScale(2, RoundingMode.HALF_UP));
        response.put("shippingFee", shippingFee.setScale(2, RoundingMode.HALF_UP));
        response.put("discountAmount", discountAmount.setScale(2, RoundingMode.HALF_UP));
        response.put("totalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));
        response.put("couponCode", coupon != null ? coupon.getCouponCode() : null);
        return response;
    }

    @Transactional
    public PaymentSessionDTO continuePayment(Integer orderId) {
        User user = getAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền thanh toán đơn hàng này.");
        }
        if (!"PROCESSING".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Chỉ đơn chờ xử lý mới có thể tiếp tục thanh toán.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán của đơn hàng."));
        String method = normalizePaymentMethod(payment.getMethod());
        if ("COD".equals(method)) {
            throw new RuntimeException("Đơn thanh toán khi nhận hàng không cần thanh toán online.");
        }
        if ("PAID".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException("Đơn hàng đã thanh toán.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return paymentGatewayService.createPaymentSession(order, payment, orderItems);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod == null || paymentMethod.isBlank()
                ? "COD"
                : paymentMethod.trim().toUpperCase();
        if (!List.of("COD", "BANKING").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán chưa được hỗ trợ.");
        }
        return normalized;
    }

    private Address resolveAddress(User user, Integer addressId) {
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        if (addresses.isEmpty()) {
            throw new RuntimeException("Bạn cần thêm địa chỉ giao hàng trước khi đặt hàng.");
        }

        if (addressId != null) {
            return addresses.stream()
                    .filter(address -> address.getId().equals(addressId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ."));
        }

        return addresses.stream()
                .filter(address -> Boolean.TRUE.equals(address.getIsDefault()))
                .findFirst()
                .orElse(addresses.get(0));
    }

    private Coupon resolveCoupon(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return null;
        }

        Coupon coupon = couponRepository.findByCouponCode(couponCode.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá không tồn tại."));
        if (Boolean.FALSE.equals(coupon.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá đang tắt.");
        }
        if (coupon.getExpireDate() != null && coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết hạn.");
        }
        if (coupon.getPromotion() != null) {
            Promotion promotion = coupon.getPromotion();
            LocalDateTime now = LocalDateTime.now();
            if (Boolean.FALSE.equals(promotion.getStatus())
                    || (promotion.getStartDate() != null && promotion.getStartDate().isAfter(now))
                    || (promotion.getEndDate() != null && promotion.getEndDate().isBefore(now))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khuyến mãi của mã giảm giá không còn hiệu lực.");
            }
        }
        return coupon;
    }

    private BigDecimal resolveCouponDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) return BigDecimal.ZERO;
        if (coupon.getDiscountValue() != null && coupon.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
            return coupon.getDiscountValue().min(subtotal);
        }
        BigDecimal percent = coupon.getPromotion() != null ? coupon.getPromotion().getDiscountPercent() : null;
        if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return subtotal.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).min(subtotal);
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

}
