package com.bfsforum.authservice.controller;

import com.bfsforum.authservice.domain.Role;
import com.bfsforum.authservice.domain.User;
import com.bfsforum.authservice.dto.LoginRequest;
import com.bfsforum.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class // EXCLUDE SPRING SECURITY
)
class AuthControllerTest {
  @MockitoBean
  private AuthService authService;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void loginWithCorrectCredentials_shouldReturnOkAndSetCookie() throws Exception {
    // Arrange
    String expectedToken = "MOCKED_JWT_TOKEN_VALUE";
    LoginRequest testLoginRequest = new LoginRequest("testUser", "testPassword");

    when(authService.loginAndIssueToken(any(LoginRequest.class))).thenReturn(expectedToken);

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testLoginRequest)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Login Successfully"))
        .andExpect(MockMvcResultMatchers.cookie().exists("token"))
        .andExpect(MockMvcResultMatchers.cookie().httpOnly("token", true))
        .andExpect(MockMvcResultMatchers.cookie().path("token", "/"))
        .andExpect(MockMvcResultMatchers.cookie().maxAge("token", 36000))
        .andExpect(MockMvcResultMatchers.cookie().value("token", expectedToken));
  }

  @Test
  void logout_shouldClearCookie() throws Exception {
    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logout Successfully"))
        .andExpect(MockMvcResultMatchers.cookie().exists("token"))
        .andExpect(MockMvcResultMatchers.cookie().httpOnly("token", true))
        .andExpect(MockMvcResultMatchers.cookie().path("token", "/"))
        .andExpect(MockMvcResultMatchers.cookie().maxAge("token", 0));
  }

  @Test
  void checkAuthWithValidToken_shouldReturnOkAndUser() throws Exception {
    // Arrange
    String userId = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    User expectedUser = User.builder()
        .id(userId)
        .username("test@example.com")
        .password("test@example.com")
        .role(Role.USER.name())
        .isActive(true)
        .build();
    when(authService.findUserById(userId)).thenReturn(expectedUser);

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.get("/auth")
            .header("X-User-Id", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        // Assert specific fields in the JSON response using jsonPath
        .andExpect(jsonPath("$.id", is(expectedUser.getId())))
        .andExpect(jsonPath("$.username", is(expectedUser.getUsername())))
        .andExpect(jsonPath("$.role", is(expectedUser.getRole())))
        .andExpect(jsonPath("$.isActive", is(expectedUser.getIsActive())))
        // Password should NOT be returned, assert its absence or null
        .andExpect(jsonPath("$.password").doesNotExist());
  }
}