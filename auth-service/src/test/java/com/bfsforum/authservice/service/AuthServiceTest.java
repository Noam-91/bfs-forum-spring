package com.bfsforum.authservice.service;

import com.bfsforum.authservice.domain.Role;
import com.bfsforum.authservice.domain.User;
import com.bfsforum.authservice.dto.LoginRequest;
import com.bfsforum.authservice.exception.InvalidCredentialsException;
import com.bfsforum.authservice.repository.AuthDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock
  private AuthDao authDao;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtEncoder jwtEncoder;

  @InjectMocks
  private AuthService authServiceTest;

  private static final long TOKEN_EXPIRE = 36000L;        // 10 hours

  @Test
  void loginAndIssueTokenWithNonExistingUsername() {
    LoginRequest loginRequest = new LoginRequest("nonExistingUser", "password");

    Mockito.when(authDao.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());
    assertThrows(InvalidCredentialsException.class, () -> {
      authServiceTest.loginAndIssueToken(loginRequest);
    });
  }

  @Test
  void loginAndIssueTokenWithWrongPassword() {
    LoginRequest loginRequest = new LoginRequest("existingUser", "password");
    User user = User.builder()
        .username("existingUser")
        .password("wrong")
        .build();
    Mockito.when(authDao.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
    Mockito.when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);
    assertThrows(InvalidCredentialsException.class, () -> {
      authServiceTest.loginAndIssueToken(loginRequest);
    });
  }

  @Test
  void loginAndIssueTokenWithCorrectCredentials() {
    LoginRequest loginRequest = new LoginRequest("existingUser", "password");
    User user = User.builder()
        .id("6a5ds1fg6a5s1")
        .username("existingUser")
        .password("encodedPasswordFromDb")
        .role(Role.USER.name())
        .build();
    Mockito.when(authDao.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
    Mockito.when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
    Jwt expectedJwt = Jwt.withTokenValue("MOCKED_JWT_TOKEN_VALUE")
        .header("typ", "JWT")
        .claim("sub", user.getUsername())
        .claim("userId", user.getId())
        .claim("role", user.getRole())
        .build();
    Mockito.when(jwtEncoder.encode(Mockito.any())).thenReturn(expectedJwt);
    String issuedToken = authServiceTest.loginAndIssueToken(loginRequest);

    assertEquals("MOCKED_JWT_TOKEN_VALUE", issuedToken, "The issued token should match the expected value.");
    Mockito.verify(authDao).findByUsername(loginRequest.getUsername());
    Mockito.verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    Mockito.verify(jwtEncoder).encode(Mockito.any());
    Mockito.verifyNoMoreInteractions(authDao, passwordEncoder, jwtEncoder);
  }
}