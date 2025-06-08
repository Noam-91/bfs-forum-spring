package com.bfsforum.emailservice.dao;

import com.bfsforum.emailservice.dto.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByUserId(UUID userId);

}