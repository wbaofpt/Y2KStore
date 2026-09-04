package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @EntityGraph(attributePaths = {"user", "product"})
    List<Review> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "product"})
    List<Review> findByProductId(Integer productId);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);

    long countByProductId(Integer productId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product.id = :productId")
    Double averageRatingByProductId(@Param("productId") Integer productId);
}
