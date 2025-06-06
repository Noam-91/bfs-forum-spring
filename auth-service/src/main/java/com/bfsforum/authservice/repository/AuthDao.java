package com.bfsforum.authservice.repository;

import java.util.Optional;
import com.bfsforum.authservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthDao extends JpaRepository<User, String> {
  Optional<User> findByUsername(String username);
  Optional<User> findById(String id);
}
