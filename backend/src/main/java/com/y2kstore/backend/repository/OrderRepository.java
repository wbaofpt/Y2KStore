package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @EntityGraph(attributePaths = {"user", "coupon", "coupon.promotion", "payment", "shippingAddress"})
    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId);

    @EntityGraph(attributePaths = {"user", "coupon", "coupon.promotion", "payment", "shippingAddress"})
    List<Order> findByUserIdAndHiddenFromHistoryFalseOrderByOrderDateDesc(Integer userId);

    @EntityGraph(attributePaths = {"user", "coupon", "coupon.promotion", "payment", "shippingAddress"})
    List<Order> findAllByOrderByOrderDateDesc();
    long countByStatus(String status);
    long countByCouponId(Integer couponId);
}
