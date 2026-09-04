package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    long countByNameContainingIgnoreCase(String name);
    List<Category> findByStatusTrue();
    boolean existsByParentCategoryId(Integer parentCategoryId);
}
