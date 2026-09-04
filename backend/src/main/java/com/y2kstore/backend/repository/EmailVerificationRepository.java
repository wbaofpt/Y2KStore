package com.y2kstore.backend.repository;

import com.y2kstore.backend.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findByEmailAndPurpose(String email, String purpose);
}
