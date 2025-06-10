package com.bfsforum.emailservice.dao;

import com.bfsforum.emailservice.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
  Optional<VerificationToken> findByUserId(String userId);

  Optional<VerificationToken> findByToken(String token);
}