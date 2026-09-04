package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @EntityGraph(attributePaths = {"order", "order.user", "order.coupon", "order.coupon.promotion"})
    List<Payment> findAllByOrderByPaymentDateDesc();

    @EntityGraph(attributePaths = {"order", "order.user", "order.coupon", "order.coupon.promotion"})
    Optional<Payment> findByOrderId(Integer orderId);
}
