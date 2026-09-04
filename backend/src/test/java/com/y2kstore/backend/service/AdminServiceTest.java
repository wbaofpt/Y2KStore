package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.UserDTO;
import com.y2kstore.backend.entity.Order;
import com.y2kstore.backend.entity.Payment;
import com.y2kstore.backend.entity.Role;
import com.y2kstore.backend.entity.User;
import com.y2kstore.backend.repository.AddressRepository;
import com.y2kstore.backend.repository.BannerRepository;
import com.y2kstore.backend.repository.CategoryRepository;
import com.y2kstore.backend.repository.CouponRepository;
import com.y2kstore.backend.repository.OrderItemRepository;
import com.y2kstore.backend.repository.OrderRepository;
import com.y2kstore.backend.repository.PaymentRepository;
import com.y2kstore.backend.repository.ProductImageRepository;
import com.y2kstore.backend.repository.ProductPromotionRepository;
import com.y2kstore.backend.repository.ProductRepository;
import com.y2kstore.backend.repository.ProductVariantRepository;
import com.y2kstore.backend.repository.PromotionRepository;
import com.y2kstore.backend.repository.ReviewRepository;
import com.y2kstore.backend.repository.RoleRepository;
import com.y2kstore.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private ProductPromotionRepository productPromotionRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BannerRepository bannerRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private ReviewRepository reviewRepository;

    private RecordingEmailService emailService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        emailService = new RecordingEmailService();
        adminService = new AdminService(
                orderRepository,
                orderItemRepository,
                paymentRepository,
                productRepository,
                productImageRepository,
                productVariantRepository,
                productPromotionRepository,
                userRepository,
                roleRepository,
                categoryRepository,
                bannerRepository,
                addressRepository,
                promotionRepository,
                couponRepository,
                reviewRepository,
                emailService
        );
    }

    @Test
    void updateUserRejectsLockingAnAdminAccount() {
        Role adminRole = new Role("ROLE_ADMIN");
        User admin = new User("encoded-password", "admin@example.com", "Admin", null, adminRole);
        admin.setId(42);
        admin.setStatus(true);

        UserDTO update = new UserDTO();
        update.setStatus(false);
        when(userRepository.findById(42)).thenReturn(Optional.of(admin));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> adminService.updateUser(42, update)
        );

        assertEquals("Không thể khóa tài khoản admin.", error.getMessage());
        verify(userRepository, never()).save(admin);
    }

    @Test
    void updateOrderStatusSendsCustomerEmailForTrackedStatus() {
        Role customerRole = new Role("ROLE_USER");
        User customer = new User("encoded-password", "buyer@example.com", "Buyer", null, customerRole);
        customer.setId(7);

        Order order = new Order();
        order.setId(99);
        order.setUser(customer);
        order.setStatus("PROCESSING");

        when(orderRepository.findById(99)).thenReturn(Optional.of(order));

        adminService.updateOrderStatus(99, "PREPARING");

        verify(orderRepository).save(order);
        assertEquals("buyer@example.com", emailService.lastRecipient);
        assertEquals(99, emailService.lastOrderId);
        assertEquals("PREPARING", emailService.lastStatus);
    }

    @Test
    void updateOrderStatusMarksDeliveredOrderPaidAndSendsEmail() {
        Role customerRole = new Role("ROLE_USER");
        User customer = new User("encoded-password", "buyer@example.com", "Buyer", null, customerRole);
        customer.setId(7);

        Order order = new Order();
        order.setId(100);
        order.setUser(customer);
        order.setStatus("SHIPPED");

        Payment payment = new Payment();
        payment.setStatus("UNPAID");
        order.setPayment(payment);

        when(orderRepository.findById(100)).thenReturn(Optional.of(order));

        adminService.updateOrderStatus(100, "DELIVERED");

        assertEquals("PAID", payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        assertEquals("buyer@example.com", emailService.lastRecipient);
        assertEquals(100, emailService.lastOrderId);
        assertEquals("DELIVERED", emailService.lastStatus);
    }

    private static class RecordingEmailService extends EmailService {
        String lastRecipient;
        Integer lastOrderId;
        String lastStatus;

        RecordingEmailService() {
            super(null, null, null, "");
        }

        @Override
        public void sendOrderStatusNotification(String recipient, Integer orderId, String status) {
            this.lastRecipient = recipient;
            this.lastOrderId = orderId;
            this.lastStatus = status;
        }
    }
}
