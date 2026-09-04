package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductId(Integer productId);
    long countByStockLessThan(Integer stock);
}
