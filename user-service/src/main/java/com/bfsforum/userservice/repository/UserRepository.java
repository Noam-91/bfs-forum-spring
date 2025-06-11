package com.bfsforum.userservice.repository;

import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
    boolean existsByUsername(String username);
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.profile p ORDER BY p.createdAt DESC")
    Page<User> findAllOrderByCreatedAt(Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseAndRole(String username, Role role, Pageable pageable);
}
