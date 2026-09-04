package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @EntityGraph(attributePaths = {"variant", "variant.product"})
    List<OrderItem> findByOrderId(Integer orderId);

    @Query("select coalesce(sum(oi.quantity), 0) from OrderItem oi where oi.variant.product.id = :productId and oi.order.status <> 'CANCELLED'")
    Long sumQuantityByProductId(@Param("productId") Integer productId);

    @Query("""
            select count(oi) > 0
            from OrderItem oi
            where oi.order.user.id = :userId
              and oi.variant.product.id = :productId
              and upper(oi.order.status) = 'DELIVERED'
              and upper(oi.order.payment.status) = 'PAID'
            """)
    boolean existsReviewablePurchase(@Param("userId") Integer userId, @Param("productId") Integer productId);
}
