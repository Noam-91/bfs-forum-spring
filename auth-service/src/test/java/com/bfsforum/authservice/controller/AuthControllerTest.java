package com.bfsforum.authservice.controller;

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

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class // <--- EXCLUDE SPRING SECURITY
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
    mockMvc.perform(MockMvcRequestBuilders.post("/login")
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
}