package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Order;
import com.y2kstore.backend.entity.OrderItem;
import com.y2kstore.backend.entity.Payment;
import com.y2kstore.backend.entity.Product;
import com.y2kstore.backend.entity.ProductVariant;
import com.y2kstore.backend.entity.Role;
import com.y2kstore.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:review-eligibility;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.sql.init.mode=never",
        "logging.level.org.hibernate.SQL=OFF"
})
@AutoConfigureTestDatabase(replace = NONE)
class OrderItemRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private OrderItemRepository orderItemRepository;

    @Test
    void reviewRequiresDeliveredAndPaidOrder() {
        Role role = entityManager.persist(new Role("ROLE_USER"));
        User user = new User("encoded-password", "buyer@example.com", "Buyer", null, role);
        user = entityManager.persist(user);

        Product processingPaid = persistPurchasedProduct(user, "PROCESSING", "PAID", "Processing paid");
        Product deliveredUnpaid = persistPurchasedProduct(user, "DELIVERED", "UNPAID", "Delivered unpaid");
        Product shippedPaid = persistPurchasedProduct(user, "SHIPPED", "PAID", "Shipped paid");
        Product deliveredPaid = persistPurchasedProduct(user, "DELIVERED", "PAID", "Delivered paid");
        entityManager.flush();
        entityManager.clear();

        assertFalse(orderItemRepository.existsReviewablePurchase(user.getId(), processingPaid.getId()));
        assertFalse(orderItemRepository.existsReviewablePurchase(user.getId(), deliveredUnpaid.getId()));
        assertFalse(orderItemRepository.existsReviewablePurchase(user.getId(), shippedPaid.getId()));
        assertTrue(orderItemRepository.existsReviewablePurchase(user.getId(), deliveredPaid.getId()));
    }

    private Product persistPurchasedProduct(User user, String orderStatus, String paymentStatus, String name) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name);
        product.setPrice(BigDecimal.valueOf(100_000));
        product.setStatus(1);
        product = entityManager.persist(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setStock(1);
        variant.setPrice(product.getPrice());
        variant = entityManager.persist(variant);

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(product.getPrice());
        order.setStatus(orderStatus);
        order = entityManager.persist(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod("COD");
        payment.setAmount(product.getPrice());
        payment.setStatus(paymentStatus);
        entityManager.persist(payment);

        entityManager.persist(new OrderItem(order, variant, 1, product.getPrice()));
        return product;
    }
}
