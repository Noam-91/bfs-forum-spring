package com.bfsforum.userservice.service;

import com.bfsforum.userservice.dto.*;
import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.entity.UserProfile;
import com.bfsforum.userservice.exceptions.UserNotFoundException;
import com.bfsforum.userservice.exceptions.UserProfileNotFoundException;
import com.bfsforum.userservice.repository.UserProfileRepository;
import com.bfsforum.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Checks whether a given username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Registers a new user and profile in the system.
     *
     * @param dto The registration request payload.
     * @return The created User entity.
     */
    public User register(UserRegisterRequest dto) {


        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .isActive(false)
                .role(Role.UNVERIFIED)
                .build();

        UserProfile profile = UserProfile.builder()
                .isActive(true)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .createdAt(LocalDateTime.now())
                .imgUrl(dto.getImgUrl())
                .user(user)
                .build();

        user.setProfile(profile);
        return userRepository.save(user);
    }

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
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves all users in a paginated format.
     *
     * @param page Page number (0-based).
     * @param size Number of users per page.
     * @return Page of users sorted by createdAt (desc).
     */
    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable);
    }

    /**
     * Updates the profile of a user.
     *
     * @param userId The user's ID whose profile is being updated.
     * @param dto    The new profile information.
     * @throws UserNotFoundException         if user not found.
     * @throws UserProfileNotFoundException if user profile not found.
     */
    public void updateProfile(UUID userId, UserProfileDto dto)
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
    public void updateUserRole(UUID userId, Role newRole) throws UserNotFoundException {
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
    public void setUserActivation(UUID userId, boolean isActive) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setActive(isActive);
        userRepository.save(user);
    }

}
