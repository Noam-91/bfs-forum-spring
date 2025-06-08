package com.bfsforum.userservice.services;

import com.bfsforum.userservice.dto.UserProfileDto;
import com.bfsforum.userservice.dto.UserRegisterRequest;
import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.entity.UserProfile;
import com.bfsforum.userservice.repository.UserProfileRepository;
import com.bfsforum.userservice.repository.UserRepository;
import com.bfsforum.userservice.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testUsernameExists_returnsTrue() {
        when(userRepository.existsByUsername("zhijun")).thenReturn(true);
        assertTrue(userService.usernameExists("zhijun"));
    }

    @Test
    void testRegister_success() {
        UserRegisterRequest dto = new UserRegisterRequest("test", "test123", "test@example.com","admin", "admin", "default.png");

        when(passwordEncoder.encode("test123")).thenReturn("$2a$11$ifPUemlX2TYtI9NVn9tnr.sZ2DyyMQaJW3DqnRVszm0oKyg.Q.FbG");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register(dto);

        assertEquals("test", user.getUsername());
        assertEquals("$2a$11$ifPUemlX2TYtI9NVn9tnr.sZ2DyyMQaJW3DqnRVszm0oKyg.Q.FbG", user.getPassword());
        assertFalse(user.isActive());
        assertNotNull(user.getProfile());
        assertEquals("admin", user.getProfile().getFirstName());
    }

    @Test
    void testFindById_userExists() {
        UUID id = UUID.randomUUID();
        User mockUser = User.builder().id(id).username("zhijun").build();
        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.findById(id);
        assertTrue(result.isPresent());
        assertEquals("zhijun", result.get().getUsername());
    }

    @Test
    void testFindById_userNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(id);
        assertFalse(result.isPresent());
    }

    @Test
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
    void testUpdateUserRole_success() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).role(Role.USER).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserRole(id, Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testSetUserActivation_activate() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).isActive(false).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.setUserActivation(id, true);
        assertTrue(user.isActive());
    }

    @Test
    void testSetUserActivation_deactivate() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).isActive(true).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.setUserActivation(id, false);
        assertFalse(user.isActive());
    }

}
