package com.bfsforum.userservice.service;

import com.bfsforum.userservice.dto.*;
import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.entity.UserProfile;
import com.bfsforum.userservice.exceptions.UserAlreadyExistsException;
import com.bfsforum.userservice.exceptions.UserNotFoundException;
import com.bfsforum.userservice.exceptions.UserProfileNotFoundException;
import com.bfsforum.userservice.repository.UserProfileRepository;
import com.bfsforum.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final StreamBridge streamBridge;
    private final RequestReplyManager<EmailVerificationReply> requestReplyManager;


    /**
     * Checks whether a given username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Value("${bfs-forum.kafka.user-register-binding-name}")
    private String userRegisterBinding;
    /**
     * Registers a new user and profile in the system.
     *
     * @param dto The registration request payload.
     * @return The created User entity.
     */
    public User register(UserRegisterMessage dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("User with username '" + dto.getUsername() + "' already exists");
        }

        // 1. 构建并保存用户
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isActive(true)
                .role(Role.UNVERIFIED)
                .build();

        UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID().toString())
                .isActive(true)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .createdAt(Instant.now())
                .user(user)
                .build();

        user.setProfile(profile);

        User saved = userRepository.save(user); // 保存后得到 string

        // 2. 构建 Kafka dto（不含 password）
        UserRegisterRequest message = UserRegisterRequest.builder()
                .userId(saved.getId())
                .email(saved.getUsername())  // username 即 email
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();

        streamBridge.send(userRegisterBinding, message);
        log.info("The registration info has been sent to EmailService via Kafka: {}", message);
        return saved;
    }


    @Value("${bfs-forum.kafka.token-verify-binding-name}")
    private String tokenVerifyBinding;

    public EmailVerificationReply verifyToken(String token) {
        String correlationId = UUID.randomUUID().toString();

        CompletableFuture<EmailVerificationReply> future = requestReplyManager.createAndStoreFuture(correlationId);

        Message<String> message = MessageBuilder.withPayload(token)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();

        log.info("Send token verification request：correlationId={}, token={}", correlationId, token);
        streamBridge.send(tokenVerifyBinding, message);

        return requestReplyManager.awaitFuture(correlationId, future);
    }

    @Value("${bfs-forum.kafka.user-info-reply-binding-name}")
    private String userInfoReplyBinding;

    /**
     * Retrieves a user by username.
     *
     * @param username The username to search for.
     * @return Optional containing the User if found.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Retrieves a user by UUID.
     *
     * @param id The user's UUID.
     * @return Optional containing the User if found.
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves all users in a paginated format.
     *
     * @param page Page number (0-based).
     * @param size Number of users per page.
     * @return Page of users sorted by createdAt (desc).
     */
    public Page<User> getAllUsers(int page, int size, String username, String role) {
        Pageable pageable = PageRequest.of(page, size);

        if (username != null && !username.isEmpty() && role != null && !role.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseAndRole(
                    username, Role.valueOf(role), pageable);
        } else if (username != null && !username.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else if (role != null && !role.isEmpty()) {
            return userRepository.findByRole(Role.valueOf(role), pageable);
        } else {
            return userRepository.findAllOrderByCreatedAt(pageable);
        }
    }

    /**
     * Updates the profile of a user.
     *
     * @param userId The user's ID whose profile is being updated.
     * @param dto    The new profile information.
     * @throws UserNotFoundException         if user not found.
     * @throws UserProfileNotFoundException if user profile not found.
     */
    public void updateProfile(String userId, UserProfileDto dto)
            throws UserNotFoundException, UserProfileNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            throw new UserProfileNotFoundException("User profile not found for user ID: " + userId);
        }

        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setImgUrl(dto.getImgUrl());

        userProfileRepository.save(profile);
    }

    /**
     * Updates a user's role (e.g., promote to ADMIN).
     *
     * @param userId  The user's UUID.
     * @param newRole The new role to assign.
     * @throws UserNotFoundException if user not found.
     */
    public void updateUserRole(String userId, Role newRole) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * Activates or deactivate a user (sets isActive = true or false).
     *
     * @param userId The user's UUID.
     * @throws UserNotFoundException if user not found.
     */
    public void setUserActivation(String userId, boolean isActive) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setActive(isActive);
        userRepository.save(user);
    }

    /**
     * Activates a user after email verification.
     * Sets the user as active and upgrades role to USER.
     *
     * @param userId the UUID of the user
     * @param expiresAt the expiration time of the token
     */
    public void activateVerifiedUser(String userId, Instant expiresAt) {
        if (expiresAt.isBefore(Instant.now())) {
            throw new RuntimeException("Token has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User does not exist, ID: " + userId));

        user.setActive(true);
        user.setRole(Role.USER);

        userRepository.save(user);
        log.info("User successfully activated:: {}", userId);
    }

    public List<User> getUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }
}
