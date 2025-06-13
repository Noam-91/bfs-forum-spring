package com.bfsforum.authservice.repository;
import com.bfsforum.authservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthDao extends JpaRepository<User, String> {
  Optional<User> findByUsername(String username);
}
