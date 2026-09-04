package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, ProductPromotion.ProductPromotionId> {
    List<ProductPromotion> findByProductId(Integer productId);
    List<ProductPromotion> findByPromotionId(Integer promotionId);
    long countByPromotionId(Integer promotionId);
}
