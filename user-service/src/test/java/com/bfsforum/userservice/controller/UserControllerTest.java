package com.bfsforum.userservice.controller;

import com.bfsforum.userservice.config.KafkaConsumerConfig;
import com.bfsforum.userservice.dto.*;
import com.bfsforum.userservice.entity.*;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import com.bfsforum.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @MockitoBean
    private KafkaConsumerConfig kafkaConsumerConfig;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private StreamBridge streamBridge;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_success() throws Exception {
        String userId = UUID.randomUUID().toString(); //  To String
        UserRegisterMessage req = new UserRegisterMessage("test", "test123","John", "Doe", "img.png");

        User user = User.builder().id(userId).username("test").build();
        when(userService.usernameExists("test")).thenReturn(false);
        when(userService.register(any())).thenReturn(user);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void getProfile_success() throws Exception {
        String id = UUID.randomUUID().toString();

        UserProfile profile = UserProfile.builder()
                .firstName("John")
                .lastName("Doe")
                .imgUrl("img.png")
                .isActive(true)
                .build();

        User user = User.builder()
                .id(id)
                .username("test")
                .role(Role.USER)
                .isActive(true)
                .profile(profile)
                .build();

        when(userService.findById(id)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/{id}/profile", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void updateProfile_success() throws Exception {
        UUID id = UUID.randomUUID();
        UserProfileDto dto = new UserProfileDto("New", "Name", "new.png");

        mockMvc.perform(put("/users/{id}/profile", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile updated successfully"));
    }
    @Test
    @DisplayName("Admin toggles activation - isActive true")
    void testToggleUserActivation_activate() throws Exception {
        String userId = UUID.randomUUID().toString();

        String body = "{ \"isActive\": true }";

        mockMvc.perform(put("/users/" + userId + "/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User activated"));

        verify(userService).setUserActivation(userId, true);
    }

    @Test
    @DisplayName("Admin toggles activation - isActive false")
    void testToggleUserActivation_deactivate() throws Exception {
        String userId = UUID.randomUUID().toString();

        String body = "{ \"isActive\": false }";

        mockMvc.perform(put("/users/" + userId + "/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated"));

        verify(userService).setUserActivation(userId, false);
    }

    @Test
    void updateUserRole_success() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/users/{id}/role?role=ADMIN", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated to ADMIN"));
    }


    @Test
    void getProfile_notFound() throws Exception {
        String id = UUID.randomUUID().toString();

        when(userService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}/profile", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

}
