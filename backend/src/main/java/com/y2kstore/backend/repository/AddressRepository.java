package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.Address;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    @EntityGraph(attributePaths = "user")
    List<Address> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "user")
    List<Address> findByUserId(Integer userId);

    long countByUserId(Integer userId);

    @EntityGraph(attributePaths = "user")
    List<Address> findByUserIdAndIsDefaultTrue(Integer userId);
}
