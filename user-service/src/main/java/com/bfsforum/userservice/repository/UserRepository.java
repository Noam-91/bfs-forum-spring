package com.bfsforum.userservice.repository;

import com.bfsforum.userservice.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String username);
    Optional<User> findById(UUID id);
    boolean existsByUsername(String username);
    Page<User> findAll(Pageable pageable);
}
