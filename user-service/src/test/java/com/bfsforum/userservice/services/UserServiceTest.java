package com.bfsforum.userservice.services;

import com.bfsforum.userservice.dto.UserProfileDto;
import com.bfsforum.userservice.dto.UserRegisterMessage;
import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.entity.UserProfile;
import com.bfsforum.userservice.repository.UserProfileRepository;
import com.bfsforum.userservice.repository.UserRepository;
import com.bfsforum.userservice.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should return true if username exists")
    void testUsernameExists_returnsTrue() {
        when(userRepository.existsByUsername("zhijun")).thenReturn(true);
        boolean result = userService.usernameExists("zhijun");
        assertTrue(result);
        verify(userRepository).existsByUsername("zhijun");
    }

    @Test
    void testRegister_success() throws Exception {
        // Arrange
        UserRegisterMessage dto = new UserRegisterMessage("test", "test123", "admin", "admin", "default.png");

        when(passwordEncoder.encode("test123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // 👇 用反射注入 @Value 字段（解决 bindingName 为 null 问题）
        Field field = UserService.class.getDeclaredField("userRegisterBinding");
        field.setAccessible(true);
        field.set(userService, "mock-binding");

        // Act
        User result = userService.register(dto);

        // Assert
        assertEquals("test", result.getUsername());
        verify(streamBridge).send(eq("mock-binding"), any());
    }

    @Test
    @DisplayName("Should return user if found by ID")
    void testFindById_userExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("zhijun").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals("zhijun", result.get().getUsername());
    }

    @Test
    @DisplayName("Should return empty if user not found by ID")
    void testFindById_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateProfile_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .profile(UserProfile.builder().firstName("Old").build())
                .build();

        UserProfileDto dto = new UserProfileDto("New", "Name", "updated.png");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateProfile(userId, dto);

        assertEquals("New", user.getProfile().getFirstName());
        assertEquals("Name", user.getProfile().getLastName());
        assertEquals("updated.png", user.getProfile().getImgUrl());
        verify(userProfileRepository).save(user.getProfile());
    }

    @Test
    @DisplayName("Should update user role successfully")
    void testUpdateUserRole_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).role(Role.USER).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserRole(userId, Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should activate user successfully")
    void testSetUserActivation_activate() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(false).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.setUserActivation(userId, true);

        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void testSetUserActivation_deactivate() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(true).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.setUserActivation(userId, false);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }
}
