package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "role")
    java.util.List<User> findAll();

    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String resetToken);
}
