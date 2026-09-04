package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategoryId(Integer categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryIdAndNameContainingIgnoreCase(Integer categoryId, String name);
    @Query("""
            select distinct p from Product p
            left join p.categories c
            left join c.parentCategory parent
            where p.status = :status
              and (p.category.id = :categoryId or c.id = :categoryId or parent.id = :categoryId)
            """)
    List<Product> findByAnyCategoryIdAndStatus(@Param("categoryId") Integer categoryId, @Param("status") Integer status);

    List<Product> findByNameContainingIgnoreCaseAndStatus(String name, Integer status);

    @Query("""
            select distinct p from Product p
            left join p.categories c
            left join c.parentCategory parent
            where p.status = :status
              and lower(p.name) like lower(concat('%', :name, '%'))
              and (p.category.id = :categoryId or c.id = :categoryId or parent.id = :categoryId)
            """)
    List<Product> findByAnyCategoryIdAndNameContainingIgnoreCaseAndStatus(@Param("categoryId") Integer categoryId,
                                                                          @Param("name") String name,
                                                                          @Param("status") Integer status);

    @Query("""
            select count(distinct p.id) from Product p
            left join p.categories c
            left join c.parentCategory parent
            where p.category.id = :categoryId or c.id = :categoryId or parent.id = :categoryId
            """)
    long countByAnyCategoryId(@Param("categoryId") Integer categoryId);

    List<Product> findByStatus(Integer status);
    long countByStatus(Integer status);
}
